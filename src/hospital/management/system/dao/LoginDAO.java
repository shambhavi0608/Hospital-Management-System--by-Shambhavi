package hospital.management.system.dao;

import hospital.management.system.config.DatabaseConnection;
import hospital.management.system.model.LoggedInUser;
import hospital.management.system.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/*
 * Login authentication aur logged-in user ki role information
 * database se load karta hai.
 *
 * INTERVIEW:
 * 1. PreparedStatement SQL injection prevent karta hai.
 * 2. DAO pattern database logic ko Swing UI se separate rakhta hai.
 * 3. Authentication ke baad user ka role database se milta hai.
 * 4. Role manually select karke privilege gain nahi kiya ja sakta.
 */
public class LoginDAO {

    /*
     * Username/password verify karke complete LoggedInUser return karta hai.
     */
    public Optional<LoggedInUser> authenticateUser(
            String username,
            char[] password
    ) throws SQLException {

        return authenticateUser(username, password, null);
    }

    /**
     * Username, password aur selected role tino verify karta hai.
     *
     * Role database se compare hota hai, isliye sirf dropdown change
     * karke kisi doosre role ke privileges nahi mil sakte.
     */
    public Optional<LoggedInUser> authenticateUser(
            String username,
            char[] password,
            String expectedRole
    ) throws SQLException {

        if (username == null ||
                username.isBlank() ||
                password == null ||
                password.length == 0) {

            return Optional.empty();
        }

        String sql = """
                SELECT l.ID, l.Role, l.Patient_Number
                FROM login l
                LEFT JOIN staff_info s
                  ON s.Login_ID = l.ID
                WHERE l.ID = ?
                  AND l.PW = ?
                  AND (
                        ? IS NULL
                        OR UPPER(l.Role) = UPPER(?)
                  )
                  AND (
                        UPPER(l.Role) NOT IN ('DOCTOR', 'NURSE')
                        OR UPPER(COALESCE(s.Status, 'INACTIVE')) = 'ACTIVE'
                  )
                LIMIT 1
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, username.trim());
            statement.setString(2, new String(password));

            if (expectedRole == null || expectedRole.isBlank()) {
                statement.setNull(3, java.sql.Types.VARCHAR);
                statement.setNull(4, java.sql.Types.VARCHAR);
            } else {
                statement.setString(3, expectedRole.trim());
                statement.setString(4, expectedRole.trim());
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                String databaseUsername =
                        resultSet.getString("ID");

                String databaseRole =
                        resultSet.getString("Role");

                String patientNumber =
                        resultSet.getString("Patient_Number");

                UserRole role =
                        UserRole.fromDatabaseValue(databaseRole);

                return Optional.of(
                        new LoggedInUser(
                                databaseUsername,
                                role,
                                patientNumber
                        )
                );
            }
        }
    }

    /*
     * Purane code ke saath compatibility ke liye boolean method.
     */
    public boolean authenticate(
            String username,
            char[] password
    ) throws SQLException {

        return authenticateUser(
                username,
                password
        ).isPresent();
    }
}
