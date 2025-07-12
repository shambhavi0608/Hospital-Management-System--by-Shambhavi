package hospital.management.system.ui;

import hospital.management.system.dao.AnalyticsDAO;
import hospital.management.system.dao.AnalyticsDAO.AnalyticsSnapshot;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reference-matched, live hospital analytics dashboard. */
public final class AnalyticsPanel extends JPanel {

    private static final Color PAGE = new Color(246, 249, 253);
    private static final Color CARD = Color.WHITE;
    private static final Color TEXT = new Color(9, 36, 79);
    private static final Color MUTED = new Color(79, 98, 128);
    private static final Color LINE = new Color(218, 227, 239);
    private static final Color BLUE = new Color(16, 99, 230);
    private static final Color TEAL = new Color(12, 174, 175);
    private static final Color GREEN = new Color(30, 168, 78);
    private static final Color ORANGE = new Color(255, 139, 7);
    private static final Color SLATE = new Color(118, 151, 188);

    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    private final MetricCard totalAdmissionsCard = new MetricCard(
            "Total Admissions", BLUE, IconType.PATIENT, "+ live records");
    private final MetricCard admittedCard = new MetricCard(
            "Currently Admitted", TEAL, IconType.BED, "Live count");
    private final MetricCard availableCard = new MetricCard(
            "Available Rooms", GREEN, IconType.BED, "Live count");
    private final MetricCard occupancyCard = new MetricCard(
            "Occupancy Rate", ORANGE, IconType.PIE, "Live utilization");

    private final DonutChart patientStatusChart = new DonutChart(
            "Total", TEAL, SLATE);
    private final DonutChart roomOccupancyChart = new DonutChart(
            "Occupied", TEAL, ORANGE);
    private final TrendChart trendChart = new TrendChart();
    private final HorizontalBarChart roomTypeChart = new HorizontalBarChart();

    private final JComboBox<String> periodBox = new JComboBox<>(new String[]{
            "Last 7 Days", "Last 30 Days", "Last 90 Days", "Last 1 Year"
    });
    private final ActionButton refreshButton = new ActionButton(
            "REFRESH ANALYTICS", IconType.REFRESH);
    private final JLabel insightLabel = new JLabel(
            "Analytics insight will appear after live data loads.");
    private final JLabel connectionLabel = new JLabel("Loading database...");
    private final JLabel updatedLabel = new JLabel("Waiting for refresh");

    public AnalyticsPanel() {
        setLayout(new BorderLayout());
        setBackground(PAGE);

        JPanel canvas = new JPanel();
        canvas.setBackground(PAGE);
        canvas.setLayout(new BoxLayout(canvas, BoxLayout.Y_AXIS));
        canvas.setBorder(new EmptyBorder(24, 28, 24, 28));

        canvas.add(createHeader());
        canvas.add(Box.createVerticalStrut(18));
        canvas.add(createMetricRow());
        canvas.add(Box.createVerticalStrut(18));
        canvas.add(createDonutRow());
        canvas.add(Box.createVerticalStrut(18));
        canvas.add(createGraphRow());
        canvas.add(Box.createVerticalStrut(18));
        canvas.add(createFooter());
        canvas.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PAGE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);
        add(scrollPane, BorderLayout.CENTER);

        periodBox.setSelectedIndex(1);
        periodBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        periodBox.setBackground(Color.WHITE);
        periodBox.setFocusable(false);

        refreshButton.addActionListener(event -> loadAnalytics());
        periodBox.addActionListener(event -> loadAnalytics());

        SwingUtilities.invokeLater(this::loadAnalytics);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Hospital Analytics");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 30));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel("Live patient and room performance insights");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(MUTED);

        titles.add(heading);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 7));
        controls.setOpaque(false);
        periodBox.setPreferredSize(new Dimension(165, 43));
        refreshButton.setPreferredSize(new Dimension(230, 43));
        controls.add(periodBox);
        controls.add(refreshButton);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JPanel createMetricRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 18, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 124));
        row.setPreferredSize(new Dimension(1100, 124));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(totalAdmissionsCard);
        row.add(admittedCard);
        row.add(availableCard);
        row.add(occupancyCard);
        return row;
    }

    private JPanel createDonutRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 18, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 292));
        row.setPreferredSize(new Dimension(1100, 292));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel patientCard = createChartCard("Patient Status");
        patientCard.add(patientStatusChart, BorderLayout.CENTER);

        RoundedPanel roomCard = createChartCard("Room Occupancy");
        roomCard.add(roomOccupancyChart, BorderLayout.CENTER);

        row.add(patientCard);
        row.add(roomCard);
        return row;
    }

    private JPanel createGraphRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 18, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));
        row.setPreferredSize(new Dimension(1100, 310));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel trendCard = createChartCard("Admissions Overview");
        trendCard.add(trendChart, BorderLayout.CENTER);

        RoundedPanel typeCard = createChartCard("Room Type Distribution");
        typeCard.add(roomTypeChart, BorderLayout.CENTER);

        row.add(trendCard);
        row.add(typeCard);
        return row;
    }

    private RoundedPanel createChartCard(String title) {
        RoundedPanel card = new RoundedPanel(CARD, 16, true);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(TEXT);
        label.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(label, BorderLayout.NORTH);
        return card;
    }

    private JPanel createFooter() {
        RoundedPanel footer = new RoundedPanel(Color.WHITE, 14, false);
        footer.setLayout(new BorderLayout(18, 0));
        footer.setBorder(new EmptyBorder(13, 16, 13, 16));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        footer.setPreferredSize(new Dimension(1100, 58));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel insight = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        insight.setOpaque(false);
        insight.add(new JLabel(new VectorIcon(IconType.INSIGHT, 30, BLUE)));
        insightLabel.setForeground(BLUE);
        insightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        insight.add(insightLabel);
        footer.add(insight, BorderLayout.CENTER);

        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        status.setOpaque(false);
        JLabel dot = new JLabel("\u25CF");
        dot.setForeground(new Color(17, 157, 91));
        dot.setFont(new Font("Dialog", Font.BOLD, 15));
        connectionLabel.setForeground(new Color(12, 125, 70));
        connectionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        updatedLabel.setForeground(MUTED);
        updatedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        status.add(dot);
        status.add(connectionLabel);
        status.add(new JSeparator(SwingConstants.VERTICAL) {{
            setPreferredSize(new Dimension(1, 22));
        }});
        status.add(updatedLabel);
        footer.add(status, BorderLayout.EAST);
        return footer;
    }

    private int selectedDays() {
        return switch (periodBox.getSelectedIndex()) {
            case 0 -> 7;
            case 2 -> 90;
            case 3 -> 365;
            default -> 30;
        };
    }

    public void refreshAnalytics() {
        loadAnalytics();
    }

    private void loadAnalytics() {
        if (!refreshButton.isEnabled()) {
            return;
        }

        int days = selectedDays();
        refreshButton.setEnabled(false);
        refreshButton.setText("LOADING...");
        connectionLabel.setText("Refreshing live data...");
        connectionLabel.setForeground(ORANGE.darker());

        new SwingWorker<AnalyticsSnapshot, Void>() {
            @Override
            protected AnalyticsSnapshot doInBackground() throws Exception {
                return analyticsDAO.loadAnalytics(days);
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                refreshButton.setText("REFRESH ANALYTICS");

                try {
                    AnalyticsSnapshot snapshot = get();
                    applySnapshot(snapshot);
                    connectionLabel.setText("Database Connected");
                    connectionLabel.setForeground(new Color(12, 125, 70));
                    updatedLabel.setText("Updated " + LocalTime.now().format(
                            DateTimeFormatter.ofPattern("hh:mm:ss a")));
                } catch (Exception exception) {
                    Throwable cause = exception.getCause();
                    String message = cause == null
                            ? exception.getMessage() : cause.getMessage();
                    connectionLabel.setText("Analytics unavailable");
                    connectionLabel.setForeground(new Color(202, 50, 60));
                    updatedLabel.setText("Check database");
                    insightLabel.setText("Could not load analytics: " + safe(message));
                }
            }
        }.execute();
    }

    private void applySnapshot(AnalyticsSnapshot data) {
        totalAdmissionsCard.setValue(String.valueOf(data.totalAdmissions()));
        admittedCard.setValue(String.valueOf(data.admittedPatients()));
        availableCard.setValue(String.valueOf(data.availableRooms()));
        occupancyCard.setValue(String.format("%.0f%%", data.getOccupancyPercentage()));

        LinkedHashMap<String, Integer> patientStatus = new LinkedHashMap<>();
        patientStatus.put("Admitted", data.admittedPatients());
        patientStatus.put("Discharged", data.dischargedPatients());
        patientStatusChart.setData(patientStatus, data.totalAdmissions());

        LinkedHashMap<String, Integer> rooms = new LinkedHashMap<>();
        rooms.put("Available", data.availableRooms());
        rooms.put("Occupied", data.occupiedRooms());
        roomOccupancyChart.setData(rooms,
                (int) Math.round(data.getOccupancyPercentage()));

        trendChart.setData(data.admissionTrend(), data.dischargeTrend());
        roomTypeChart.setData(data.roomTypeCounts());
        insightLabel.setText(createInsight(data.admissionTrend()));
    }

    private String createInsight(Map<String, Integer> admissions) {
        String peakLabel = "selected period";
        int peak = 0;
        for (Map.Entry<String, Integer> entry : admissions.entrySet()) {
            if (entry.getValue() >= peak) {
                peak = entry.getValue();
                peakLabel = entry.getKey();
            }
        }
        return peak == 0
                ? "No admissions were recorded in the selected period."
                : "Peak admissions: " + peak + " around " + peakLabel + ".";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Unknown database error" : value;
    }

    private static final class MetricCard extends RoundedPanel {
        private final JLabel valueLabel = new JLabel("--");

        private MetricCard(
                String title,
                Color accent,
                IconType icon,
                String note
        ) {
            super(Color.WHITE, 16, true);
            setLayout(new BorderLayout(14, 0));
            setBorder(new EmptyBorder(18, 18, 18, 18));

            JPanel iconCircle = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics graphics) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(withAlpha(accent, 24));
                    g.fill(new Ellipse2D.Double(0, 0, getWidth() - 1, getHeight() - 1));
                    g.setColor(withAlpha(accent, 65));
                    g.draw(new Ellipse2D.Double(0.5, 0.5,
                            getWidth() - 2.0, getHeight() - 2.0));
                    g.dispose();
                }
            };
            iconCircle.setOpaque(false);
            iconCircle.setPreferredSize(new Dimension(62, 62));
            iconCircle.add(new JLabel(new VectorIcon(icon, 34, accent)));
            add(iconCircle, BorderLayout.WEST);

            JPanel details = new JPanel();
            details.setOpaque(false);
            details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setForeground(TEXT);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            valueLabel.setForeground(accent);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            JLabel noteLabel = new JLabel(note);
            noteLabel.setForeground(accent.equals(BLUE) ? GREEN.darker() : MUTED);
            noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            details.add(titleLabel);
            details.add(Box.createVerticalStrut(3));
            details.add(valueLabel);
            details.add(Box.createVerticalStrut(2));
            details.add(noteLabel);
            add(details, BorderLayout.CENTER);
        }

        private void setValue(String value) {
            valueLabel.setText(value);
        }
    }

    private static final class DonutChart extends JPanel {
        private final String centerCaption;
        private final Color firstColor;
        private final Color secondColor;
        private Map<String, Integer> data = new LinkedHashMap<>();
        private int centerValue;

        private DonutChart(String centerCaption, Color firstColor, Color secondColor) {
            this.centerCaption = centerCaption;
            this.firstColor = firstColor;
            this.secondColor = secondColor;
            setOpaque(false);
        }

        private void setData(Map<String, Integer> data, int centerValue) {
            this.data = new LinkedHashMap<>(data);
            this.centerValue = centerValue;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = prepare(graphics);

            int diameter = Math.max(120, Math.min(190, getHeight() - 28));
            int x = 12;
            int y = Math.max(4, (getHeight() - diameter) / 2);
            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(data.entrySet());

            if (total == 0) {
                g.setColor(new Color(231, 237, 245));
                g.fill(new Ellipse2D.Double(x, y, diameter, diameter));
            } else {
                double startAngle = 90.0;
                Color[] colors = {firstColor, secondColor};
                for (int index = 0; index < entries.size(); index++) {
                    double extent = -360.0 * entries.get(index).getValue() / total;
                    g.setColor(colors[index % colors.length]);
                    g.fill(new Arc2D.Double(x, y, diameter, diameter,
                            startAngle, extent, Arc2D.PIE));
                    startAngle += extent;
                }
            }

            int hole = (int) (diameter * 0.56);
            int holeX = x + (diameter - hole) / 2;
            int holeY = y + (diameter - hole) / 2;
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(holeX, holeY, hole, hole));

            String centerText = "Total".equals(centerCaption)
                    ? String.valueOf(centerValue) : centerValue + "%";
            drawCentered(g, centerText, x, y + diameter / 2 - 9, diameter,
                    new Font("Segoe UI", Font.BOLD, 24), TEXT);
            drawCentered(g, centerCaption, x, y + diameter / 2 + 17, diameter,
                    new Font("Segoe UI", Font.PLAIN, 13), TEXT);

            int legendX = Math.min(getWidth() - 175, x + diameter + 44);
            int legendY = Math.max(42, getHeight() / 2 - 34);
            Color[] colors = {firstColor, secondColor};
            for (int index = 0; index < entries.size(); index++) {
                Map.Entry<String, Integer> entry = entries.get(index);
                g.setColor(colors[index % colors.length]);
                g.fillRoundRect(legendX, legendY + index * 48, 20, 20, 5, 5);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g.setColor(TEXT);
                g.drawString(entry.getKey(), legendX + 32, legendY + 15 + index * 48);
                g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g.drawString(String.valueOf(entry.getValue()),
                        Math.max(legendX + 130, getWidth() - 55),
                        legendY + 15 + index * 48);
            }
            g.dispose();
        }
    }

    private static final class TrendChart extends JPanel {
        private Map<String, Integer> admissions = new LinkedHashMap<>();
        private Map<String, Integer> discharges = new LinkedHashMap<>();

        private TrendChart() {
            setOpaque(false);
        }

        private void setData(
                Map<String, Integer> admissions,
                Map<String, Integer> discharges
        ) {
            this.admissions = new LinkedHashMap<>(admissions);
            this.discharges = new LinkedHashMap<>(discharges);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = prepare(graphics);
            int left = 42;
            int right = getWidth() - 20;
            int top = 42;
            int bottom = getHeight() - 34;

            drawLegend(g, Math.max(left, getWidth() / 2 - 105), 13,
                    BLUE, "Admissions");
            drawLegend(g, Math.max(left + 110, getWidth() / 2 + 12), 13,
                    GREEN.darker(), "Discharges");

            int maximum = Math.max(
                    admissions.values().stream().max(Integer::compareTo).orElse(0),
                    discharges.values().stream().max(Integer::compareTo).orElse(0));
            maximum = Math.max(5, maximum);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int lineIndex = 0; lineIndex <= 4; lineIndex++) {
                int y = bottom - (bottom - top) * lineIndex / 4;
                g.setColor(new Color(220, 228, 239));
                g.drawLine(left, y, right, y);
                g.setColor(MUTED);
                g.drawString(String.valueOf(maximum * lineIndex / 4), 8, y + 4);
            }

            List<String> labels = new ArrayList<>(admissions.keySet());
            if (labels.isEmpty()) {
                g.dispose();
                return;
            }

            int count = labels.size();
            for (int index = 0; index < count; index++) {
                int x = count == 1 ? left : left + (right - left) * index / (count - 1);
                g.setColor(MUTED);
                FontMetrics metrics = g.getFontMetrics();
                g.drawString(labels.get(index),
                        x - metrics.stringWidth(labels.get(index)) / 2,
                        bottom + 21);
            }

            drawSeries(g, labels, admissions, maximum, left, right, top, bottom, BLUE);
            drawSeries(g, labels, discharges, maximum, left, right, top, bottom,
                    GREEN.darker());
            g.dispose();
        }

        private void drawSeries(
                Graphics2D g,
                List<String> labels,
                Map<String, Integer> values,
                int maximum,
                int left,
                int right,
                int top,
                int bottom,
                Color color
        ) {
            int count = labels.size();
            Path2D path = new Path2D.Double();
            List<Point> points = new ArrayList<>();

            for (int index = 0; index < count; index++) {
                int value = values.getOrDefault(labels.get(index), 0);
                int x = count == 1 ? left : left + (right - left) * index / (count - 1);
                int y = bottom - (bottom - top) * value / maximum;
                points.add(new Point(x, y));
                if (index == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }

            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            g.setColor(color);
            g.draw(path);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            for (int index = 0; index < points.size(); index++) {
                Point point = points.get(index);
                g.setColor(Color.WHITE);
                g.fillOval(point.x - 5, point.y - 5, 10, 10);
                g.setColor(color);
                g.fillOval(point.x - 3, point.y - 3, 6, 6);
                String value = String.valueOf(values.getOrDefault(labels.get(index), 0));
                g.drawString(value, point.x - g.getFontMetrics().stringWidth(value) / 2,
                        point.y - 10);
            }
        }
    }

    private static final class HorizontalBarChart extends JPanel {
        private Map<String, Integer> data = new LinkedHashMap<>();

        private HorizontalBarChart() {
            setOpaque(false);
        }

        private void setData(Map<String, Integer> data) {
            this.data = new LinkedHashMap<>(data);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = prepare(graphics);
            if (data.isEmpty()) {
                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g.drawString("No room distribution data", 20, 45);
                g.dispose();
                return;
            }

            int left = 126;
            int right = getWidth() - 45;
            int top = 22;
            int maximum = Math.max(1,
                    data.values().stream().max(Integer::compareTo).orElse(1));
            int rowHeight = Math.max(38,
                    Math.min(52, (getHeight() - top - 18) / Math.max(1, data.size())));
            Color[] colors = {BLUE, TEAL, new Color(119, 83, 232), ORANGE, GREEN};

            int index = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int y = top + index * rowHeight;
                int width = Math.max(3,
                        (right - left) * entry.getValue() / maximum);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g.setColor(TEXT);
                FontMetrics fm = g.getFontMetrics();
                String label = entry.getKey();
                g.drawString(label, left - fm.stringWidth(label) - 12, y + 19);

                g.setColor(new Color(236, 241, 248));
                g.fillRoundRect(left, y, right - left, 24, 6, 6);
                g.setPaint(new GradientPaint(left, y,
                        colors[index % colors.length], left + width, y,
                        colors[index % colors.length].brighter()));
                g.fillRoundRect(left, y, width, 24, 6, 6);
                g.setColor(TEXT);

                g.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g.setColor(TEXT);
                g.drawString(String.valueOf(entry.getValue()),
                        Math.min(right + 8, left + width + 9), y + 18);
                index++;
            }
            g.dispose();
        }
    }

    private static final class ActionButton extends JButton {
        private final IconType iconType;
        private boolean hover;

        private ActionButton(String text, IconType iconType) {
            super(text);
            this.iconType = iconType;
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(new Color(0, 126, 151));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setIconTextGap(10);
            setIcon(new VectorIcon(iconType, 19, new Color(9, 161, 181)));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = prepare(graphics);
            Color border = isEnabled()
                    ? new Color(9, 161, 181) : new Color(177, 193, 208);
            Color fill = !isEnabled()
                    ? new Color(243, 246, 249)
                    : hover ? new Color(226, 249, 251) : Color.WHITE;
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Double(0.5, 0.5,
                    getWidth() - 1.0, getHeight() - 1.0, 12, 12));
            g.setColor(border);
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new RoundRectangle2D.Double(1, 1,
                    getWidth() - 2.0, getHeight() - 2.0, 12, 12));
            g.dispose();

            setForeground(isEnabled() ? new Color(0, 119, 145) : MUTED);
            super.paintComponent(graphics);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color fill;
        private final int radius;
        private final boolean shadow;

        private RoundedPanel(Color fill, int radius, boolean shadow) {
            this.fill = fill;
            this.radius = radius;
            this.shadow = shadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = prepare(graphics);
            if (shadow) {
                g.setColor(new Color(18, 52, 91, 18));
                g.fill(new RoundRectangle2D.Double(2, 4,
                        getWidth() - 4.0, getHeight() - 5.0, radius, radius));
            }
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Double(0.5, 0.5,
                    getWidth() - 2.0,
                    getHeight() - (shadow ? 4.0 : 2.0), radius, radius));
            g.setColor(LINE);
            g.draw(new RoundRectangle2D.Double(0.5, 0.5,
                    getWidth() - 2.0,
                    getHeight() - (shadow ? 4.0 : 2.0), radius, radius));
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private enum IconType {
        PATIENT, BED, PIE, REFRESH, INSIGHT
    }

    private static final class VectorIcon implements Icon {
        private final IconType type;
        private final int size;
        private final Color color;

        private VectorIcon(IconType type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            float scale = size / 32f;
            g.setStroke(new BasicStroke(Math.max(1.6f, 2.1f * scale),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (type) {
                case PATIENT -> {
                    g.draw(new Ellipse2D.Float(11 * scale, 3 * scale,
                            10 * scale, 10 * scale));
                    g.draw(new Arc2D.Float(5 * scale, 13 * scale,
                            22 * scale, 17 * scale, 0, 180, Arc2D.OPEN));
                    g.drawLine(Math.round(11 * scale), Math.round(23 * scale),
                            Math.round(11 * scale), Math.round(29 * scale));
                    g.drawLine(Math.round(21 * scale), Math.round(23 * scale),
                            Math.round(21 * scale), Math.round(29 * scale));
                }
                case BED -> {
                    g.drawLine(Math.round(4 * scale), Math.round(5 * scale),
                            Math.round(4 * scale), Math.round(28 * scale));
                    g.drawLine(Math.round(4 * scale), Math.round(22 * scale),
                            Math.round(28 * scale), Math.round(22 * scale));
                    g.draw(new RoundRectangle2D.Float(10 * scale, 13 * scale,
                            18 * scale, 9 * scale, 4 * scale, 4 * scale));
                    g.draw(new Ellipse2D.Float(6 * scale, 15 * scale,
                            6 * scale, 6 * scale));
                    g.drawLine(Math.round(27 * scale), Math.round(22 * scale),
                            Math.round(27 * scale), Math.round(28 * scale));
                }
                case PIE -> {
                    g.draw(new Arc2D.Float(4 * scale, 6 * scale,
                            23 * scale, 23 * scale, 0, 270, Arc2D.PIE));
                    g.drawLine(Math.round(16 * scale), Math.round(5 * scale),
                            Math.round(16 * scale), Math.round(16 * scale));
                    g.drawLine(Math.round(16 * scale), Math.round(16 * scale),
                            Math.round(28 * scale), Math.round(16 * scale));
                }
                case REFRESH -> {
                    g.draw(new Arc2D.Float(5 * scale, 5 * scale,
                            22 * scale, 22 * scale, 35, 270, Arc2D.OPEN));
                    Path2D arrow = new Path2D.Float();
                    arrow.moveTo(25 * scale, 4 * scale);
                    arrow.lineTo(27 * scale, 12 * scale);
                    arrow.lineTo(19 * scale, 10 * scale);
                    g.fill(arrow);
                }
                case INSIGHT -> {
                    g.draw(new Ellipse2D.Float(8 * scale, 4 * scale,
                            16 * scale, 18 * scale));
                    g.drawLine(Math.round(12 * scale), Math.round(24 * scale),
                            Math.round(20 * scale), Math.round(24 * scale));
                    g.drawLine(Math.round(13 * scale), Math.round(28 * scale),
                            Math.round(19 * scale), Math.round(28 * scale));
                    g.drawLine(Math.round(16 * scale), Math.round(0 * scale),
                            Math.round(16 * scale), Math.round(2 * scale));
                    g.drawLine(Math.round(3 * scale), Math.round(13 * scale),
                            Math.round(6 * scale), Math.round(13 * scale));
                    g.drawLine(Math.round(26 * scale), Math.round(13 * scale),
                            Math.round(29 * scale), Math.round(13 * scale));
                }
            }
            g.dispose();
        }
    }

    private static Graphics2D prepare(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private static void drawCentered(
            Graphics2D g,
            String text,
            int x,
            int baseline,
            int width,
            Font font,
            Color color
    ) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, x + (width - metrics.stringWidth(text)) / 2, baseline);
    }

    private static void drawLegend(
            Graphics2D g,
            int x,
            int y,
            Color color,
            String label
    ) {
        g.setColor(color);
        g.fillOval(x, y, 8, 8);
        g.drawLine(x - 8, y + 4, x + 16, y + 4);
        g.setColor(TEXT);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.drawString(label, x + 22, y + 8);
    }
}

