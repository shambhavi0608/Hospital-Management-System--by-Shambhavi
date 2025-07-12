package hospital.management.system.dao;

import hospital.management.system.config.DatabaseConnection;
import hospital.management.system.model.StaffMember;
import hospital.management.system.util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Staff database operations.
 *
 * Admin is DAO ke through doctors aur nurses ko:
 * - Database mein add kar sakta hai
 * - Search kar sakta hai
 * - Active/Inactive kar sakta hai
 */
public final class StaffDAO {

    /**
     * Saare staff members load karta hai.
     * Keyword empty ho to complete staff list milegi.
     */
    public List<StaffMember> findAll(String keyword)
            throws SQLException {

        String filter = keyword == null
                ? ""
                : keyword.trim();

        String sql =
                "SELECT Staff_ID, Name, Role, Department, " +
                        "Specialization, Phone, Email, Shift, " +
                        "Login_ID, Status " +
                        "FROM staff_info " +
                        "WHERE (? = '' " +
                        "OR Name LIKE ? " +
                        "OR Role LIKE ? " +
                        "OR Department LIKE ? " +
                        "OR Specialization LIKE ? " +
                        "OR Login_ID LIKE ?) " +
                        "ORDER BY Staff_ID DESC";

        List<StaffMember> staffMembers =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            String likeFilter = "%" + filter + "%";

            statement.setString(1, filter);
            statement.setString(2, likeFilter);
            statement.setString(3, likeFilter);
            statement.setString(4, likeFilter);
            statement.setString(5, likeFilter);
            statement.setString(6, likeFilter);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    StaffMember staffMember =
                            new StaffMember(
                                    resultSet.getInt(
                                            "Staff_ID"
                                    ),
                                    resultSet.getString(
                                            "Name"
                                    ),
                                    resultSet.getString(
                                            "Role"
                                    ),
                                    resultSet.getString(
                                            "Department"
                                    ),
                                    resultSet.getString(
                                            "Specialization"
                                    ),
                                    resultSet.getString(
                                            "Phone"
                                    ),
                                    resultSet.getString(
                                            "Email"
                                    ),
                                    resultSet.getString(
                                            "Shift"
                                    ),
                                    resultSet.getString(
                                            "Login_ID"
                                    ),
                                    resultSet.getString(
                                            "Status"
                                    )
                            );

                    staffMembers.add(staffMember);
                }
            }
        }

        return staffMembers;
    }

    /**
     * Naya doctor ya nurse add karta hai.
     *
     * login aur staff_info dono tables ko ek hi
     * transaction mein update karta hai.
     */
    public void addStaff(
            String name,
            String role,
            String department,
            String specialization,
            String phone,
            String email,
            String shift,
            String loginId,
            char[] password
    ) throws SQLException {

        requireAdministrator();

        String normalizedRole = role == null
                ? ""
                : role.trim().toUpperCase();

        if (!normalizedRole.equals("DOCTOR")
                && !normalizedRole.equals("NURSE")) {
            throw new IllegalArgumentException(
                    "Only DOCTOR or NURSE staff accounts can be created."
            );
        }

        String loginSql =
                "INSERT INTO login " +
                        "(ID, PW, Role, Patient_Number) " +
                        "VALUES (?, ?, ?, NULL)";

        String staffSql =
                "INSERT INTO staff_info " +
                        "(Name, Role, Department, Specialization, " +
                        "Phone, Email, Shift, Login_ID, Status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";

        Connection connection = null;

        try {
            connection =
                    DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            try (
                    PreparedStatement loginStatement =
                            connection.prepareStatement(
                                    loginSql
                            );

                    PreparedStatement staffStatement =
                            connection.prepareStatement(
                                    staffSql
                            )
            ) {

                loginStatement.setString(
                        1,
                        loginId.trim()
                );

                loginStatement.setString(
                        2,
                        new String(password)
                );

                loginStatement.setString(
                        3,
                        normalizedRole
                );

                loginStatement.executeUpdate();

                staffStatement.setString(
                        1,
                        name.trim()
                );

                staffStatement.setString(
                        2,
                        normalizedRole
                );

                staffStatement.setString(
                        3,
                        cleanValue(department)
                );

                staffStatement.setString(
                        4,
                        cleanValue(specialization)
                );

                staffStatement.setString(
                        5,
                        cleanValue(phone)
                );

                staffStatement.setString(
                        6,
                        cleanValue(email)
                );

                staffStatement.setString(
                        7,
                        cleanValue(shift)
                );

                staffStatement.setString(
                        8,
                        loginId.trim()
                );

                staffStatement.executeUpdate();

                connection.commit();
            }

        } catch (SQLException exception) {

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(
                            rollbackException
                    );
                }
            }

            throw exception;

        } finally {

            if (connection != null) {

                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Connection close hone wali hai.
                }

                try {
                    connection.close();
                } catch (SQLException ignored) {
                    // Main database exception ko preserve karna hai.
                }
            }
        }
    }

    /**
     * Staff member ko ACTIVE ya INACTIVE karta hai.
     */
    public boolean setStatus(
            int staffId,
            String status
    ) throws SQLException {

        requireAdministrator();

        String normalizedStatus =
                status == null
                        ? ""
                        : status.trim().toUpperCase();

        if (
                !normalizedStatus.equals("ACTIVE")
                        &&
                        !normalizedStatus.equals("INACTIVE")
        ) {
            throw new IllegalArgumentException(
                    "Status must be ACTIVE or INACTIVE."
            );
        }

        String sql =
                "UPDATE staff_info " +
                        "SET Status = ? " +
                        "WHERE Staff_ID = ?";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    normalizedStatus
            );

            statement.setInt(
                    2,
                    staffId
            );

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Kisi Login ID ka pehle se present hona check karta hai.
     */
    public boolean loginIdExists(
            String loginId
    ) throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM login " +
                        "WHERE ID = ?";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    loginId.trim()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next()
                        && resultSet.getInt(1) > 0;
            }
        }
    }

    /**
     * Dashboard ke liye active doctors count.
     */
    public int countActiveDoctors()
            throws SQLException {

        return countActiveStaffByRole("DOCTOR");
    }

    /**
     * Dashboard ke liye active nurses count.
     */
    public int countActiveNurses()
            throws SQLException {

        return countActiveStaffByRole("NURSE");
    }

    private int countActiveStaffByRole(
            String role
    ) throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM staff_info " +
                        "WHERE UPPER(Role) = ? " +
                        "AND UPPER(Status) = 'ACTIVE'";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    role.toUpperCase()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }

                return 0;
            }
        }
    }

    private String cleanValue(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    private void requireAdministrator() {

        if (!SessionManager.isCurrentUserAdmin()) {
            throw new SecurityException(
                    "Only an administrator can modify staff records."
            );
        }
    }
}
