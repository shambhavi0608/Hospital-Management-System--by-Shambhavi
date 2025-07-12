package hospital.management.system.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/hospital_management_system";

    private static final String USER = "root";

    private static final String PASSWORD =
            System.getenv("HMS_DB_PASSWORD");

    private DatabaseConnection() {
        // Object creation prevent karne ke liye
    }

    public static Connection getConnection() throws SQLException {

        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                    "HMS_DB_PASSWORD environment variable set nahi hai."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}