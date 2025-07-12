package hospital.management.system.dao;

import hospital.management.system.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
 * PURPOSE:
 * Ye DAO database se available rooms find karta hai.
 *
 * INTERVIEW EXPLANATION:
 * DAO pattern use karne se database code Swing UI se separate rehta hai.
 * PreparedStatement SQL injection ko prevent karta hai.
 */
public class RoomDAO {

    /*
     * Record ek room suggestion ka required data store karta hai.
     * Immutable hone ki wajah se result accidentally modify nahi hota.
     */
    public record RoomSuggestion(
            String roomNumber,
            double price
    ) {
    }

    /*
     * User ke maximum budget ke andar available rooms return karta hai.
     */
    public List<RoomSuggestion> findAvailableRoomsWithinBudget(
            double maximumBudget
    ) throws SQLException {

        List<RoomSuggestion> suggestions = new ArrayList<>();

        /*
         * Database mein Price VARCHAR hai, isliye numerical comparison ke
         * liye CAST use kiya gaya hai.
         */
        String sql = """
                SELECT Room_no, Price
                FROM room
                WHERE Availability = 'Available'
                  AND CAST(Price AS DECIMAL(10,2)) <= ?
                ORDER BY CAST(Price AS DECIMAL(10,2)) ASC
                """;

        /*
         * try-with-resources connection, statement aur result automatically
         * close karta hai. Isse resource leak nahi hota.
         */
        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setDouble(1, maximumBudget);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    String roomNumber =
                            resultSet.getString("Room_no");

                    double price =
                            resultSet.getDouble("Price");

                    RoomSuggestion suggestion =
                            new RoomSuggestion(roomNumber, price);

                    suggestions.add(suggestion);
                }
            }
        }

        return suggestions;
    }
}