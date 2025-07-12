package hospital.management.system.util;

import hospital.management.system.model.LoggedInUser;
import hospital.management.system.model.UserRole;

import java.util.Optional;

/*
 * Current logged-in user ki session manage karta hai.
 *
 * INTERVIEW:
 * SessionManager central location provide karta hai jahan se
 * application ke different Swing panels current user aur uski
 * permissions check kar sakte hain.
 *
 * Password kabhi session mein store nahi hota.
 */
public final class SessionManager {

    /*
     * volatile ensure karta hai ki different threads ko
     * current updated value visible rahe.
     */
    private static volatile LoggedInUser currentUser;

    /*
     * Utility class ka object create nahi hona chahiye.
     */
    private SessionManager() {
    }

    /*
     * Successful authentication ke baad session start karta hai.
     */
    public static synchronized void startSession(
            LoggedInUser user
    ) {

        if (user == null) {

            throw new IllegalArgumentException(
                    "Session start karne ke liye user required hai."
            );
        }

        currentUser = user;
    }

    /*
     * Logout ke waqt current session remove karta hai.
     */
    public static synchronized void endSession() {

        currentUser = null;
    }

    public static boolean isLoggedIn() {

        return currentUser != null;
    }

    /*
     * Optional use karne se null handling safer hoti hai.
     */
    public static Optional<LoggedInUser> getCurrentUser() {

        return Optional.ofNullable(currentUser);
    }

    /*
     * Protected screen ke liye logged-in user required hota hai.
     */
    public static LoggedInUser requireCurrentUser() {

        LoggedInUser user = currentUser;

        if (user == null) {

            throw new IllegalStateException(
                    "Koi user login nahi hai."
            );
        }

        return user;
    }

    public static boolean currentUserHasRole(
            UserRole requiredRole
    ) {

        LoggedInUser user = currentUser;

        return user != null &&
                user.hasRole(requiredRole);
    }

    public static boolean isCurrentUserAdmin() {

        return currentUserHasRole(
                UserRole.ADMIN
        );
    }

    public static boolean isCurrentUserDoctor() {

        return currentUserHasRole(
                UserRole.DOCTOR
        );
    }

    public static boolean isCurrentUserNurse() {

        return currentUserHasRole(
                UserRole.NURSE
        );
    }

    public static boolean isCurrentUserPatient() {

        return currentUserHasRole(
                UserRole.PATIENT
        );
    }
}