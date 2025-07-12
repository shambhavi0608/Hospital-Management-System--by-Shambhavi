package hospital.management.system.dao;

import hospital.management.system.config.DatabaseConnection;
import hospital.management.system.model.Patient;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * Patient database operations.
 *
 * INTERVIEW:
 * PreparedStatement SQL injection prevent karta hai.
 * Transactions patient aur room status ko synchronized rakhti hain.
 */
public class PatientDAO {

    public void addPatient(Patient patient)
            throws SQLException {

        String occupyRoomSql = """
                UPDATE room
                SET Availability = 'Occupied'
                WHERE Room_no = ?
                  AND Availability = 'Available'
                """;

        String insertPatientSql = """
                INSERT INTO Patient_Info
                (
                    ID,
                    Number,
                    Name,
                    Gender,
                    Patient_Disease,
                    Room_Number,
                    Time,
                    Deposit,
                    Status,
                    Discharge_Time
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Admitted', NULL)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (
                    PreparedStatement roomStatement =
                            connection.prepareStatement(occupyRoomSql);

                    PreparedStatement patientStatement =
                            connection.prepareStatement(insertPatientSql)
            ) {
                roomStatement.setString(
                        1,
                        patient.getRoomNumber()
                );

                if (roomStatement.executeUpdate() != 1) {
                    throw new SQLException(
                            "Selected room available nahi hai."
                    );
                }

                setPatientInsertParameters(
                        patientStatement,
                        patient
                );

                if (patientStatement.executeUpdate() != 1) {
                    throw new SQLException(
                            "Patient insert nahi hua."
                    );
                }

                connection.commit();

            } catch (Exception exception) {

                connection.rollback();

                if (exception instanceof SQLException sqlException) {
                    throw sqlException;
                }

                throw new SQLException(
                        exception.getMessage(),
                        exception
                );

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Patient> getAllPatients()
            throws SQLException {

        return executePatientListQuery(
                """
                SELECT *
                FROM Patient_Info
                ORDER BY
                    CASE
                        WHEN Status = 'Admitted' THEN 0
                        ELSE 1
                    END,
                    Name
                """,
                null
        );
    }

    public List<Patient> searchPatients(
            String keyword
    ) throws SQLException {

        String sql = """
                SELECT *
                FROM Patient_Info
                WHERE Number LIKE ?
                   OR Name LIKE ?
                   OR Gender LIKE ?
                   OR Patient_Disease LIKE ?
                   OR Room_Number LIKE ?
                   OR Status LIKE ?
                ORDER BY Name
                """;

        List<Patient> patients = new ArrayList<>();
        String searchValue = "%" + keyword.trim() + "%";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            for (int index = 1; index <= 6; index++) {
                statement.setString(index, searchValue);
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    patients.add(mapPatient(resultSet));
                }
            }
        }

        return patients;
    }

    public Optional<Patient> findByIdNumber(
            String idNumber
    ) throws SQLException {

        String sql = """
                SELECT *
                FROM Patient_Info
                WHERE Number = ?
                LIMIT 1
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, idNumber);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapPatient(resultSet)
                    );
                }
            }
        }

        return Optional.empty();
    }

    public boolean existsByIdNumber(
            String idNumber
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM Patient_Info
                WHERE Number = ?
                LIMIT 1
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, idNumber);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                return resultSet.next();
            }
        }
    }

    public List<String> getAvailableRoomNumbers()
            throws SQLException {

        List<String> rooms = new ArrayList<>();

        String sql = """
                SELECT Room_no
                FROM room
                WHERE Availability = 'Available'
                ORDER BY CAST(Room_no AS UNSIGNED)
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            while (resultSet.next()) {
                rooms.add(
                        resultSet.getString("Room_no")
                );
            }
        }

        return rooms;
    }

    public void updatePatient(Patient patient)
            throws SQLException {

        String currentRoomSql = """
                SELECT Room_Number
                FROM Patient_Info
                WHERE Number = ?
                  AND Status = 'Admitted'
                FOR UPDATE
                """;

        String occupyNewRoomSql = """
                UPDATE room
                SET Availability = 'Occupied'
                WHERE Room_no = ?
                  AND Availability = 'Available'
                """;

        String releaseOldRoomSql = """
                UPDATE room
                SET Availability = 'Available'
                WHERE Room_no = ?
                """;

        String updatePatientSql = """
                UPDATE Patient_Info
                SET ID = ?,
                    Name = ?,
                    Gender = ?,
                    Patient_Disease = ?,
                    Room_Number = ?,
                    Time = ?,
                    Deposit = ?
                WHERE Number = ?
                  AND Status = 'Admitted'
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                String currentRoom;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     currentRoomSql
                             )) {

                    statement.setString(
                            1,
                            patient.getIdNumber()
                    );

                    try (ResultSet resultSet =
                                 statement.executeQuery()) {

                        if (!resultSet.next()) {
                            throw new SQLException(
                                    "Active patient nahi mila."
                            );
                        }

                        currentRoom = resultSet.getString(
                                "Room_Number"
                        );
                    }
                }

                boolean roomChanged =
                        !currentRoom.equals(
                                patient.getRoomNumber()
                        );

                if (roomChanged) {

                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         occupyNewRoomSql
                                 )) {

                        statement.setString(
                                1,
                                patient.getRoomNumber()
                        );

                        if (statement.executeUpdate() != 1) {
                            throw new SQLException(
                                    "New room available nahi hai."
                            );
                        }
                    }

                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         releaseOldRoomSql
                                 )) {

                        statement.setString(1, currentRoom);
                        statement.executeUpdate();
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     updatePatientSql
                             )) {

                    statement.setString(
                            1,
                            patient.getIdType()
                    );

                    statement.setString(
                            2,
                            patient.getName()
                    );

                    statement.setString(
                            3,
                            patient.getGender()
                    );

                    statement.setString(
                            4,
                            patient.getDisease()
                    );

                    statement.setString(
                            5,
                            patient.getRoomNumber()
                    );

                    statement.setString(
                            6,
                            patient.getAdmissionTime()
                    );

                    statement.setBigDecimal(
                            7,
                            patient.getDeposit()
                    );

                    statement.setString(
                            8,
                            patient.getIdNumber()
                    );

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Patient update nahi hua."
                        );
                    }
                }

                connection.commit();

            } catch (Exception exception) {

                connection.rollback();

                if (exception instanceof SQLException sqlException) {
                    throw sqlException;
                }

                throw new SQLException(
                        exception.getMessage(),
                        exception
                );

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void dischargePatient(
            String idNumber,
            LocalDateTime dischargeTime
    ) throws SQLException {

        String patientRoomSql = """
                SELECT Room_Number
                FROM Patient_Info
                WHERE Number = ?
                  AND Status = 'Admitted'
                FOR UPDATE
                """;

        String dischargePatientSql = """
                UPDATE Patient_Info
                SET Status = 'Discharged',
                    Discharge_Time = ?
                WHERE Number = ?
                  AND Status = 'Admitted'
                """;

        String releaseRoomSql = """
                UPDATE room
                SET Availability = 'Available'
                WHERE Room_no = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                String roomNumber;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     patientRoomSql
                             )) {

                    statement.setString(1, idNumber);

                    try (ResultSet resultSet =
                                 statement.executeQuery()) {

                        if (!resultSet.next()) {
                            throw new SQLException(
                                    "Admitted patient nahi mila."
                            );
                        }

                        roomNumber = resultSet.getString(
                                "Room_Number"
                        );
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     dischargePatientSql
                             )) {

                    statement.setTimestamp(
                            1,
                            Timestamp.valueOf(dischargeTime)
                    );

                    statement.setString(2, idNumber);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Patient discharge nahi hua."
                        );
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     releaseRoomSql
                             )) {

                    statement.setString(1, roomNumber);
                    statement.executeUpdate();
                }

                connection.commit();

            } catch (Exception exception) {

                connection.rollback();

                if (exception instanceof SQLException sqlException) {
                    throw sqlException;
                }

                throw new SQLException(
                        exception.getMessage(),
                        exception
                );

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void setPatientInsertParameters(
            PreparedStatement statement,
            Patient patient
    ) throws SQLException {

        statement.setString(1, patient.getIdType());
        statement.setString(2, patient.getIdNumber());
        statement.setString(3, patient.getName());
        statement.setString(4, patient.getGender());
        statement.setString(5, patient.getDisease());
        statement.setString(6, patient.getRoomNumber());
        statement.setString(7, patient.getAdmissionTime());
        statement.setBigDecimal(8, patient.getDeposit());
    }

    private List<Patient> executePatientListQuery(
            String sql,
            String unused
    ) throws SQLException {

        List<Patient> patients =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            while (resultSet.next()) {
                patients.add(mapPatient(resultSet));
            }
        }

        return patients;
    }

    private Patient mapPatient(
            ResultSet resultSet
    ) throws SQLException {

        BigDecimal deposit =
                resultSet.getBigDecimal("Deposit");

        if (deposit == null) {
            deposit = BigDecimal.ZERO;
        }

        Timestamp dischargeTimestamp =
                resultSet.getTimestamp(
                        "Discharge_Time"
                );

        LocalDateTime dischargeTime =
                dischargeTimestamp == null
                        ? null
                        : dischargeTimestamp.toLocalDateTime();

        return new Patient(
                resultSet.getString("ID"),
                resultSet.getString("Number"),
                resultSet.getString("Name"),
                resultSet.getString("Gender"),
                resultSet.getString("Patient_Disease"),
                resultSet.getString("Room_Number"),
                resultSet.getString("Time"),
                deposit,
                resultSet.getString("Status"),
                dischargeTime
        );
    }
}