package hospital.management.system.config;

import hospital.management.system.dao.DashboardDAO;

import java.sql.Connection;
import java.sql.SQLException;

/*
 * Pehle ye class sirf database connection test karti thi.
 * Ab ye dashboard ki live SQL queries bhi test karegi.
 */
public class DatabaseConnectionTest {

    public static void main(String[] args) {

        /*
         * Part 1: Database connection verify karna.
         */
        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            if (connection != null && !connection.isClosed()) {
                System.out.println(
                        "Database connected successfully!"
                );
            }

        } catch (SQLException exception) {
            System.out.println(
                    "Database connection failed: "
                            + exception.getMessage()
            );

            /*
             * Agar connection fail ho gaya toh aage ki
             * dashboard queries execute nahi karenge.
             */
            return;
        }

        /*
         * Part 2: DashboardDAO ki queries verify karna.
         */
        DashboardDAO dashboardDAO = new DashboardDAO();

        try {
            System.out.println(
                    "Total Patients: "
                            + dashboardDAO.getTotalPatients()
            );

            System.out.println(
                    "Available Rooms: "
                            + dashboardDAO.getAvailableRooms()
            );

            System.out.println(
                    "Occupied Rooms: "
                            + dashboardDAO.getOccupiedRooms()
            );

        } catch (SQLException exception) {
            System.out.println(
                    "Dashboard data fetch failed: "
                            + exception.getMessage()
            );
        }
    }
}