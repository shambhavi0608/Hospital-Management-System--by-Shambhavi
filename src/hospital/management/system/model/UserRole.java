package hospital.management.system.model;

/*
 * Application ke supported user roles.
 *
 * INTERVIEW:
 * Enum fixed role values provide karta hai, jisse spelling mistakes
 * aur unauthorized random role values avoid hoti hain.
 */
public enum UserRole {

    ADMIN,
    DOCTOR,
    NURSE,
    PATIENT;

    /*
     * Database ke String role ko safe enum value mein convert karta hai.
     */
    public static UserRole fromDatabaseValue(
            String databaseRole
    ) {

        if (databaseRole == null ||
                databaseRole.isBlank()) {

            throw new IllegalArgumentException(
                    "User role database mein missing hai."
            );
        }

        try {

            return UserRole.valueOf(
                    databaseRole
                            .trim()
                            .toUpperCase()
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Unsupported user role: "
                            + databaseRole
            );
        }
    }

    /*
     * UI par readable role name show karne ke liye.
     */
    public String getDisplayName() {

        return switch (this) {

            case ADMIN -> "Administrator";
            case DOCTOR -> "Doctor";
            case NURSE -> "Nurse";
            case PATIENT -> "Patient";
        };
    }
}