package hospital.management.system.model;

/*
 * Successfully authenticated user ki session information.
 *
 * INTERVIEW:
 * Password is class mein store nahi hota.
 * Sirf application ko required identity aur authorization
 * information maintain hoti hai.
 */
public final class LoggedInUser {

    private final String username;
    private final UserRole role;
    private final String patientNumber;

    public LoggedInUser(
            String username,
            UserRole role,
            String patientNumber
    ) {

        if (username == null ||
                username.isBlank()) {

            throw new IllegalArgumentException(
                    "Username required hai."
            );
        }

        if (role == null) {

            throw new IllegalArgumentException(
                    "User role required hai."
            );
        }

        this.username = username.trim();
        this.role = role;

        if (patientNumber == null ||
                patientNumber.isBlank()) {

            this.patientNumber = null;

        } else {

            this.patientNumber =
                    patientNumber.trim();
        }
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public boolean hasPatientNumber() {

        return patientNumber != null &&
                !patientNumber.isBlank();
    }

    public boolean hasRole(UserRole requiredRole) {

        return role == requiredRole;
    }

    public boolean isAdmin() {

        return role == UserRole.ADMIN;
    }

    public boolean isDoctor() {

        return role == UserRole.DOCTOR;
    }

    public boolean isNurse() {

        return role == UserRole.NURSE;
    }

    public boolean isPatient() {

        return role == UserRole.PATIENT;
    }

    public String getDisplayRole() {

        return role.getDisplayName();
    }

    @Override
    public String toString() {

        return "LoggedInUser{" +
                "username='" + username + '\'' +
                ", role=" + role +
                ", patientNumber='" +
                patientNumber + '\'' +
                '}';
    }
}