package hospital.management.system.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized MySQL database connection provider.
 *
 * Configuration environment variables se li ja sakti hai:
 *
 * HMS_DB_HOST
 * HMS_DB_PORT
 * HMS_DB_NAME
 * HMS_DB_USER
 * HMS_DB_PASSWORD
 *
 * Defaults:
 * localhost
 * 3306
 * hospital_management_system
 * root
 */
public final class DatabaseConnection {

    private static final String DEFAULT_HOST =
            "localhost";

    private static final String DEFAULT_PORT =
            "3306";

    private static final String DEFAULT_DATABASE =
            "hospital_management_system";

    private static final String DEFAULT_USER =
            "root";

    private static final String HOST =
            getEnvironmentOrDefault(
                    "HMS_DB_HOST",
                    DEFAULT_HOST
            );

    private static final String PORT =
            getEnvironmentOrDefault(
                    "HMS_DB_PORT",
                    DEFAULT_PORT
            );

    private static final String DATABASE =
            getEnvironmentOrDefault(
                    "HMS_DB_NAME",
                    DEFAULT_DATABASE
            );

    private static final String USER =
            getEnvironmentOrDefault(
                    "HMS_DB_USER",
                    DEFAULT_USER
            );

    private static final String PASSWORD =
            System.getenv("HMS_DB_PASSWORD");

    private static final String URL =
            "jdbc:mysql://" +
                    HOST +
                    ":" +
                    PORT +
                    "/" +
                    DATABASE +
                    "?useSSL=false" +
                    "&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=Asia/Kolkata";

    private DatabaseConnection() {
    }

    public static Connection getConnection()
            throws SQLException {

        if (PASSWORD == null ||
                PASSWORD.isBlank()) {

            throw new SQLException(
                    "HMS_DB_PASSWORD environment variable " +
                            "set nahi hai."
            );
        }

        try {

            return DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

        } catch (SQLException exception) {

            throw new SQLException(
                    "Hospital database se connection fail hua. " +
                            "MySQL running hai aur database " +
                            DATABASE +
                            " available hai ya nahi check karein.",
                    exception
            );
        }
    }

    private static String getEnvironmentOrDefault(
            String variable,
            String defaultValue
    ) {

        String value =
                System.getenv(variable);

        if (value == null ||
                value.isBlank()) {

            return defaultValue;
        }

        return value.trim();
    }

    public static String getDatabaseName() {
        return DATABASE;
    }

    public static String getDatabaseUser() {
        return USER;
    }

    /**
     * Password intentionally expose nahi kiya gaya.
     */
    public static String getConnectionDescription() {

        return "MySQL " +
                HOST +
                ":" +
                PORT +
                "/" +
                DATABASE;
    }
}