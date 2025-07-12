package hospital.management.system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Patient_Info table ka Java model.
 *
 * INTERVIEW:
 * Model class database record ko Java object ke form mein represent karti hai.
 * BigDecimal monetary values ke rounding errors prevent karta hai.
 */
public class Patient {

    private String idType;
    private String idNumber;
    private String name;
    private String gender;
    private String disease;
    private String roomNumber;
    private String admissionTime;
    private BigDecimal deposit;
    private String status;
    private LocalDateTime dischargeTime;

    public Patient() {
    }

    /*
     * New patient admission constructor.
     */
    public Patient(
            String idType,
            String idNumber,
            String name,
            String gender,
            String disease,
            String roomNumber,
            String admissionTime,
            BigDecimal deposit
    ) {
        this(
                idType,
                idNumber,
                name,
                gender,
                disease,
                roomNumber,
                admissionTime,
                deposit,
                "Admitted",
                null
        );
    }

    /*
     * Complete constructor database records load karne ke liye.
     */
    public Patient(
            String idType,
            String idNumber,
            String name,
            String gender,
            String disease,
            String roomNumber,
            String admissionTime,
            BigDecimal deposit,
            String status,
            LocalDateTime dischargeTime
    ) {
        this.idType = idType;
        this.idNumber = idNumber;
        this.name = name;
        this.gender = gender;
        this.disease = disease;
        this.roomNumber = roomNumber;
        this.admissionTime = admissionTime;
        this.deposit = deposit;
        this.status = status;
        this.dischargeTime = dischargeTime;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getAdmissionTime() {
        return admissionTime;
    }

    public void setAdmissionTime(String admissionTime) {
        this.admissionTime = admissionTime;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDischargeTime() {
        return dischargeTime;
    }

    public void setDischargeTime(
            LocalDateTime dischargeTime
    ) {
        this.dischargeTime = dischargeTime;
    }

    @Override
    public String toString() {

        return "Patient{" +
                "name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", disease='" + disease + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}