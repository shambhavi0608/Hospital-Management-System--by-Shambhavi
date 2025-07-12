package hospital.management.system.model;

/*
 * Doctor ya Nurse ki database information represent karta hai.
 *
 * Password is model mein store nahi hota.
 */
public final class StaffMember {

    private final int staffId;
    private final String name;
    private final String role;
    private final String department;
    private final String specialization;
    private final String phone;
    private final String email;
    private final String shift;
    private final String loginId;
    private final String status;

    public StaffMember(
            int staffId,
            String name,
            String role,
            String department,
            String specialization,
            String phone,
            String email,
            String shift,
            String loginId,
            String status
    ) {
        this.staffId = staffId;
        this.name = name;
        this.role = role;
        this.department = department;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.shift = shift;
        this.loginId = loginId;
        this.status = status;
    }

    public int getStaffId() {
        return staffId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getShift() {
        return shift;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getStatus() {
        return status;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public boolean isDoctor() {
        return "DOCTOR".equalsIgnoreCase(role);
    }

    public boolean isNurse() {
        return "NURSE".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "StaffMember{" +
                "staffId=" + staffId +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", department='" + department + '\'' +
                ", specialization='" + specialization + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", shift='" + shift + '\'' +
                ", loginId='" + loginId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}