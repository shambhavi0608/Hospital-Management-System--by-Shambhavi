package hospital.management.system.model;

/**
 * Application ke supported roles.
 *
 * RBAC:
 * ADMIN  -> Full administrative access
 * DOCTOR -> Clinical patient access
 * NURSE  -> Read-only clinical access
 * PATIENT -> Sirf apna portal data
 */
public enum UserRole {

    ADMIN,
    DOCTOR,
    NURSE,
    PATIENT;

    public static UserRole fromDatabaseValue(String databaseRole) {

        if (databaseRole == null || databaseRole.isBlank()) {
            throw new IllegalArgumentException(
                    "User role database mein missing hai."
            );
        }

        try {
            return UserRole.valueOf(
                    databaseRole.trim().toUpperCase()
            );

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported user role: " + databaseRole
            );
        }
    }

    public String getDisplayName() {

        return switch (this) {
            case ADMIN -> "Administrator";
            case DOCTOR -> "Doctor";
            case NURSE -> "Nurse";
            case PATIENT -> "Patient";
        };
    }

    public boolean isStaff() {
        return this == ADMIN ||
                this == DOCTOR ||
                this == NURSE;
    }

    public boolean canViewPatients() {
        return this == ADMIN ||
                this == DOCTOR ||
                this == NURSE;
    }

    public boolean canModifyPatients() {
        return this == ADMIN ||
                this == DOCTOR;
    }

    public boolean canManageStaff() {
        return this == ADMIN;
    }

    public boolean canViewAnalytics() {
        return this == ADMIN ||
                this == DOCTOR;
    }

    public boolean canUseAssistant() {
        return true;
    }

    public boolean canAccessPatientPortal() {
        return this == PATIENT;
    }
}