package hospital.management.system.dao;

import hospital.management.system.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Supplies every live counter displayed on the dashboard. */
public class DashboardDAO {

    public int getTotalPatients() throws SQLException {
        return executeCountQuery("""
                SELECT COUNT(*)
                FROM Patient_Info
                WHERE UPPER(Status) = 'ADMITTED'
                """);
    }

    public int getAvailableRooms() throws SQLException {
        return executeCountQuery("""
                SELECT COUNT(*)
                FROM room
                WHERE UPPER(Availability) = 'AVAILABLE'
                """);
    }

    public int getOccupiedRooms() throws SQLException {
        return executeCountQuery("""
                SELECT COUNT(*)
                FROM room
                WHERE UPPER(Availability) = 'OCCUPIED'
                """);
    }

    public int getActiveDoctors() throws SQLException {
        return executeCountQuery("""
                SELECT COUNT(*)
                FROM staff_info
                WHERE UPPER(Role) = 'DOCTOR'
                  AND UPPER(Status) = 'ACTIVE'
                """);
    }

    public int getActiveNurses() throws SQLException {
        return executeCountQuery("""
                SELECT COUNT(*)
                FROM staff_info
                WHERE UPPER(Role) = 'NURSE'
                  AND UPPER(Status) = 'ACTIVE'
                """);
    }

    private int executeCountQuery(String sql) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }
}
