package hospital.management.system.dao;

import hospital.management.system.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Collects every live value used by the hospital analytics screen. */
public final class AnalyticsDAO {

    private static final DateTimeFormatter CHART_LABEL =
            DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    public record AnalyticsSnapshot(
            int totalAdmissions,
            int admittedPatients,
            int dischargedPatients,
            int availableRooms,
            int occupiedRooms,
            Map<String, Integer> admissionTrend,
            Map<String, Integer> dischargeTrend,
            Map<String, Integer> roomTypeCounts
    ) {
        public double getOccupancyPercentage() {
            int totalRooms = availableRooms + occupiedRooms;
            return totalRooms == 0 ? 0.0 : occupiedRooms * 100.0 / totalRooms;
        }
    }

    public AnalyticsSnapshot loadAnalytics() throws SQLException {
        return loadAnalytics(30);
    }

    public AnalyticsSnapshot loadAnalytics(int days) throws SQLException {
        int safeDays = Math.max(7, Math.min(days, 3650));

        try (Connection connection = DatabaseConnection.getConnection()) {
            int totalAdmissions = count(connection,
                    "SELECT COUNT(*) FROM Patient_Info");
            int admittedPatients = count(connection,
                    "SELECT COUNT(*) FROM Patient_Info " +
                            "WHERE UPPER(COALESCE(Status, '')) = 'ADMITTED'");
            int dischargedPatients = count(connection,
                    "SELECT COUNT(*) FROM Patient_Info " +
                            "WHERE UPPER(COALESCE(Status, '')) = 'DISCHARGED'");
            int availableRooms = count(connection,
                    "SELECT COUNT(*) FROM room " +
                            "WHERE UPPER(COALESCE(Availability, '')) = 'AVAILABLE'");
            int occupiedRooms = count(connection,
                    "SELECT COUNT(*) FROM room " +
                            "WHERE UPPER(COALESCE(Availability, '')) = 'OCCUPIED'");

            TrendData trends = loadTrends(connection, safeDays);
            Map<String, Integer> roomTypes = loadRoomTypeCounts(connection);

            return new AnalyticsSnapshot(
                    totalAdmissions,
                    admittedPatients,
                    dischargedPatients,
                    availableRooms,
                    occupiedRooms,
                    trends.admissions,
                    trends.discharges,
                    roomTypes
            );
        }
    }

    private int count(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private TrendData loadTrends(Connection connection, int days)
            throws SQLException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);
        List<LocalDate> bucketDates = createBucketDates(start, end, 5);

        LinkedHashMap<String, Integer> admissions = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> discharges = new LinkedHashMap<>();
        for (LocalDate date : bucketDates) {
            admissions.put(CHART_LABEL.format(date), 0);
            discharges.put(CHART_LABEL.format(date), 0);
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT Time, Discharge_Time FROM Patient_Info");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                incrementBucket(admissions, bucketDates, start, end,
                        readDate(resultSet, "Time"));
                incrementBucket(discharges, bucketDates, start, end,
                        readDate(resultSet, "Discharge_Time"));
            }
        }

        return new TrendData(admissions, discharges);
    }

    private List<LocalDate> createBucketDates(
            LocalDate start,
            LocalDate end,
            int bucketCount
    ) {
        List<LocalDate> dates = new ArrayList<>();
        long span = Math.max(1L,
                java.time.temporal.ChronoUnit.DAYS.between(start, end));
        for (int index = 0; index < bucketCount; index++) {
            long offset = Math.round(span * index / (double) (bucketCount - 1));
            dates.add(start.plusDays(offset));
        }
        return dates;
    }

    private void incrementBucket(
            LinkedHashMap<String, Integer> target,
            List<LocalDate> bucketDates,
            LocalDate start,
            LocalDate end,
            LocalDate value
    ) {
        if (value == null || value.isBefore(start) || value.isAfter(end)) {
            return;
        }

        int nearest = 0;
        long smallestDistance = Long.MAX_VALUE;
        for (int index = 0; index < bucketDates.size(); index++) {
            long distance = Math.abs(
                    java.time.temporal.ChronoUnit.DAYS.between(
                            bucketDates.get(index), value));
            if (distance < smallestDistance) {
                smallestDistance = distance;
                nearest = index;
            }
        }

        String key = CHART_LABEL.format(bucketDates.get(nearest));
        target.put(key, target.getOrDefault(key, 0) + 1);
    }

    private LocalDate readDate(ResultSet resultSet, String column) {
        try {
            Timestamp timestamp = resultSet.getTimestamp(column);
            if (timestamp != null) {
                return timestamp.toLocalDateTime().toLocalDate();
            }
        } catch (SQLException ignored) {
            // Older HMS databases store the admission time as text.
        }

        try {
            String text = resultSet.getString(column);
            if (text == null || text.isBlank()) {
                return null;
            }
            return parseDate(text.trim());
        } catch (SQLException ignored) {
            return null;
        }
    }

    private LocalDate parseDate(String text) {
        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        );

        for (DateTimeFormatter format : formats) {
            try {
                return LocalDateTime.parse(text, format).toLocalDate();
            } catch (DateTimeParseException ignored) {
                // Try next format.
            }
        }

        try {
            return ZonedDateTime.parse(
                    text,
                    DateTimeFormatter.ofPattern(
                            "EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            ).toLocalDate();
        } catch (DateTimeParseException ignored) {
            // Try date-only ISO format.
        }

        try {
            return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private Map<String, Integer> loadRoomTypeCounts(Connection connection)
            throws SQLException {
        String typeColumn = findColumn(connection, "room",
                "Room_Type", "RoomType", "Bed_Type", "Category", "Type");

        if (typeColumn != null) {
            LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
            String sql = "SELECT `" + typeColumn + "`, COUNT(*) AS total " +
                    "FROM room GROUP BY `" + typeColumn + "` ORDER BY total DESC";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String type = resultSet.getString(1);
                    if (type == null || type.isBlank()) {
                        type = "General";
                    }
                    counts.put(type.trim(), resultSet.getInt("total"));
                }
            }
            if (!counts.isEmpty()) {
                return counts;
            }
        }

        return classifyRoomsByPrice(connection);
    }

    private String findColumn(
            Connection connection,
            String table,
            String... candidates
    ) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        List<String> columns = new ArrayList<>();

        try (ResultSet resultSet = metadata.getColumns(
                connection.getCatalog(), null, table, null)) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("COLUMN_NAME"));
            }
        }

        if (columns.isEmpty()) {
            try (ResultSet resultSet = metadata.getColumns(
                    connection.getCatalog(), null,
                    table.toUpperCase(Locale.ROOT), null)) {
                while (resultSet.next()) {
                    columns.add(resultSet.getString("COLUMN_NAME"));
                }
            }
        }

        for (String candidate : candidates) {
            for (String column : columns) {
                if (candidate.equalsIgnoreCase(column)) {
                    return column;
                }
            }
        }
        return null;
    }

    private Map<String, Integer> classifyRoomsByPrice(Connection connection)
            throws SQLException {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        counts.put("General", 0);
        counts.put("Semi-Private", 0);
        counts.put("Private", 0);
        counts.put("ICU / Premium", 0);

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT Price FROM room");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                double price = parsePrice(resultSet.getString("Price"));
                String category;
                if (price <= 1000) {
                    category = "General";
                } else if (price <= 2000) {
                    category = "Semi-Private";
                } else if (price <= 3500) {
                    category = "Private";
                } else {
                    category = "ICU / Premium";
                }
                counts.put(category, counts.get(category) + 1);
            }
        }
        return counts;
    }

    private double parsePrice(String value) {
        if (value == null) {
            return 0.0;
        }
        String normalized = value.replaceAll("[^0-9.]", "");
        if (normalized.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    private static final class TrendData {
        private final LinkedHashMap<String, Integer> admissions;
        private final LinkedHashMap<String, Integer> discharges;

        private TrendData(
                LinkedHashMap<String, Integer> admissions,
                LinkedHashMap<String, Integer> discharges
        ) {
            this.admissions = admissions;
            this.discharges = discharges;
        }
    }
}

