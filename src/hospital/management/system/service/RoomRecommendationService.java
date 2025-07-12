package hospital.management.system.service;

import hospital.management.system.dao.RoomDAO;
import hospital.management.system.dao.RoomDAO.RoomSuggestion;

import java.sql.SQLException;
import java.util.List;

/*
 * PURPOSE:
 * Ye service user ke budget ko validate karke RoomDAO se suitable
 * available rooms mangati hai.
 *
 * INTERVIEW EXPLANATION:
 * Service layer business rules handle karti hai.
 * DAO sirf database operations handle karta hai.
 */
public class RoomRecommendationService {

    private final RoomDAO roomDAO;

    /*
     * Constructor mein DAO initialize kiya hai.
     */
    public RoomRecommendationService() {
        this.roomDAO = new RoomDAO();
    }

    /*
     * Budget validate karke available rooms return karta hai.
     */
    public List<RoomSuggestion> recommendRooms(double maximumBudget)
            throws SQLException {

        /*
         * Negative ya zero budget valid nahi hai.
         */
        if (maximumBudget <= 0) {
            throw new IllegalArgumentException(
                    "Budget zero se greater hona chahiye."
            );
        }

        /*
         * Database operation RoomDAO ko delegate kiya gaya hai.
         */
        return roomDAO.findAvailableRoomsWithinBudget(maximumBudget);
    }
}