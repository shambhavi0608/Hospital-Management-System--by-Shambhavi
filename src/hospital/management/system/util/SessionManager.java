package hospital.management.system.util;

import hospital.management.system.model.LoggedInUser;
import hospital.management.system.model.UserRole;

import java.util.Optional;

/**
 * Application-wide authenticated session manager.
 *
 * Ye class:
 * - current logged-in user maintain karti hai
 * - role-based authorization helpers provide karti hai
 * - logout par session clear karti hai
 *
 * Password yahan kabhi store nahi hota.
 */
public final class SessionManager {

    private static volatile LoggedInUser currentUser;

    private SessionManager() {
    }

    /**
     * Successful login ke baad session create karta hai.
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

    /**
     * Current session completely clear karta hai.
     */
    public static synchronized void endSession() {

        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static Optional<LoggedInUser> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    /**
     * Login required screens/services ke liye.
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

    public static UserRole getCurrentRole() {

        return requireCurrentUser().getRole();
    }

    public static String getCurrentUsername() {

        return requireCurrentUser().getUsername();
    }

    public static boolean currentUserHasRole(
            UserRole requiredRole
    ) {

        if (requiredRole == null) {
            return false;
        }

        LoggedInUser user = currentUser;

        return user != null &&
                user.hasRole(requiredRole);
    }

    public static boolean isCurrentUserAdmin() {
        return currentUserHasRole(UserRole.ADMIN);
    }

    public static boolean isCurrentUserDoctor() {
        return currentUserHasRole(UserRole.DOCTOR);
    }

    public static boolean isCurrentUserNurse() {
        return currentUserHasRole(UserRole.NURSE);
    }

    public static boolean isCurrentUserPatient() {
        return currentUserHasRole(UserRole.PATIENT);
    }

    /**
     * Patient records view karne ki permission.
     */
    public static boolean canViewPatients() {

        LoggedInUser user = currentUser;

        return user != null &&
                user.canViewPatients();
    }

    /**
     * Patient records modify karne ki permission.
     *
     * ADMIN + DOCTOR allowed.
     * NURSE/PATIENT denied.
     */
    public static boolean canModifyPatients() {

        LoggedInUser user = currentUser;

        return user != null &&
                user.canModifyPatients();
    }

    /**
     * Staff management sirf ADMIN ke liye.
     */
    public static boolean canManageStaff() {

        LoggedInUser user = currentUser;

        return user != null &&
                user.canManageStaff();
    }

    /**
     * Analytics access.
     */
    public static boolean canViewAnalytics() {

        LoggedInUser user = currentUser;

        return user != null &&
                user.canViewAnalytics();
    }

    /**
     * Current logged-in patient's linked patient number.
     */
    public static Optional<String> getCurrentPatientNumber() {

        LoggedInUser user = currentUser;

        if (user == null ||
                !user.hasPatientNumber()) {

            return Optional.empty();
        }

        return Optional.of(
                user.getPatientNumber()
        );
    }

    /**
     * Authorization failure ko standardize karta hai.
     */
    public static void requireRole(
            UserRole requiredRole
    ) {

        if (!currentUserHasRole(requiredRole)) {

            throw new SecurityException(
                    "Access denied. Required role: "
                            + requiredRole.getDisplayName()
            );
        }
    }

    /**
     * Multiple roles mein se koi ek required ho.
     */
    public static void requireAnyRole(
            UserRole... allowedRoles
    ) {

        if (allowedRoles == null ||
                allowedRoles.length == 0) {

            throw new SecurityException(
                    "No authorized roles configured."
            );
        }

        LoggedInUser user = currentUser;

        if (user == null) {

            throw new SecurityException(
                    "Access denied. Login required."
            );
        }

        for (UserRole role : allowedRoles) {

            if (role != null &&
                    user.hasRole(role)) {

                return;
            }
        }

        throw new SecurityException(
                "Access denied for role: "
                        + user.getDisplayRole()
        );
    }
}