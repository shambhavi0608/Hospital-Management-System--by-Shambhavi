package hospital.management.system.service;

import hospital.management.system.dao.PatientDAO;
import hospital.management.system.model.Patient;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Patient validation aur business rules.
 */
public final class PatientService {

    private final PatientDAO patientDAO;

    public PatientService() {

        patientDAO = new PatientDAO();
    }

    public void admitPatient(
            Patient patient
    ) throws SQLException {

        validatePatient(patient);
        normalizePatient(patient);

        if (
                patientDAO.existsByIdNumber(
                        patient.getIdNumber()
                )
        ) {

            throw new IllegalArgumentException(
                    "This ID number is already registered."
            );
        }

        patientDAO.addPatient(patient);
    }

    public void updatePatient(
            Patient patient
    ) throws SQLException {

        validatePatient(patient);
        normalizePatient(patient);

        patientDAO.updatePatient(patient);
    }

    public void dischargePatient(
            Patient patient
    ) throws SQLException {

        if (patient == null) {

            throw new IllegalArgumentException(
                    "Please select a patient."
            );
        }

        if (
                !"Admitted".equalsIgnoreCase(
                        patient.getStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "Patient is already discharged."
            );
        }

        patientDAO.dischargePatient(
                patient.getIdNumber(),
                LocalDateTime.now()
        );
    }

    public List<Patient> getAllPatients()
            throws SQLException {

        return patientDAO.getAllPatients();
    }

    public List<Patient> searchPatients(
            String keyword
    ) throws SQLException {

        if (
                keyword == null
                        ||
                        keyword.isBlank()
        ) {

            return getAllPatients();
        }

        return patientDAO.searchPatients(
                keyword.trim()
        );
    }

    public List<String> getAvailableRoomNumbers()
            throws SQLException {

        return patientDAO
                .getAvailableRoomNumbers();
    }

    private void validatePatient(
            Patient patient
    ) {

        if (patient == null) {

            throw new IllegalArgumentException(
                    "Patient information is required."
            );
        }

        require(
                patient.getIdType(),
                "ID Type"
        );

        require(
                patient.getIdNumber(),
                "ID Number"
        );

        require(
                patient.getName(),
                "Patient Name"
        );

        require(
                patient.getGender(),
                "Gender"
        );

        require(
                patient.getDisease(),
                "Disease / Admission Reason"
        );

        require(
                patient.getRoomNumber(),
                "Room Number"
        );

        require(
                patient.getAdmissionTime(),
                "Admission Time"
        );

        validateIdentity(
                patient.getIdType(),
                patient.getIdNumber()
        );

        validateName(
                patient.getName()
        );

        validateGender(
                patient.getGender()
        );

        validateDisease(
                patient.getDisease()
        );

        validateDeposit(
                patient.getDeposit()
        );
    }

    private void validateIdentity(
            String idType,
            String idNumber
    ) {

        String normalizedType =
                idType.trim();

        String normalizedNumber =
                idNumber
                        .trim()
                        .toUpperCase();

        switch (normalizedType) {

            case "Aadhar Card":
            case "Aadhaar Card":

                if (
                        !normalizedNumber.matches(
                                "[0-9]{12}"
                        )
                ) {

                    throw new IllegalArgumentException(
                            "Aadhaar must contain exactly 12 digits."
                    );
                }

                break;

            case "Voter Id":
            case "Voter ID":

                if (
                        !normalizedNumber.matches(
                                "[A-Z0-9]{10}"
                        )
                ) {

                    throw new IllegalArgumentException(
                            "Voter ID must contain exactly "
                                    + "10 letters or numbers."
                    );
                }

                break;

            case "Driving License":

                if (
                        !normalizedNumber.matches(
                                "[A-Z0-9]{15,16}"
                        )
                ) {

                    throw new IllegalArgumentException(
                            "Driving Licence must contain "
                                    + "15 to 16 letters or numbers."
                    );
                }

                break;

            default:

                throw new IllegalArgumentException(
                        "Unsupported identity type: "
                                + normalizedType
                );
        }
    }

    private void validateName(
            String name
    ) {

        String normalizedName =
                name.trim();

        if (
                !normalizedName.matches(
                        "[\\p{L} .'-]{2,60}"
                )
        ) {

            throw new IllegalArgumentException(
                    "Enter a valid patient name."
            );
        }
    }

    private void validateGender(
            String gender
    ) {

        String normalizedGender =
                gender.trim();

        boolean validGender =
                "Male".equalsIgnoreCase(
                        normalizedGender
                )
                        ||
                        "Female".equalsIgnoreCase(
                                normalizedGender
                        )
                        ||
                        "Other".equalsIgnoreCase(
                                normalizedGender
                        );

        if (!validGender) {

            throw new IllegalArgumentException(
                    "Select a valid gender."
            );
        }
    }

    private void validateDisease(
            String disease
    ) {

        String normalizedDisease =
                disease.trim();

        if (
                normalizedDisease.length() < 2
                        ||
                        normalizedDisease.length() > 100
        ) {

            throw new IllegalArgumentException(
                    "Disease or admission reason must "
                            + "contain 2 to 100 characters."
            );
        }

        if (
                !normalizedDisease.matches(
                        "[\\p{L}0-9 ,.'()/&+\\-]{2,100}"
                )
        ) {

            throw new IllegalArgumentException(
                    "Disease or admission reason contains "
                            + "unsupported characters."
            );
        }
    }

    private void validateDeposit(
            BigDecimal deposit
    ) {

        if (deposit == null) {

            throw new IllegalArgumentException(
                    "Deposit amount is required."
            );
        }

        if (
                deposit.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {

            throw new IllegalArgumentException(
                    "Deposit cannot be negative."
            );
        }

        if (deposit.scale() > 2) {

            throw new IllegalArgumentException(
                    "Deposit can contain a maximum "
                            + "of 2 decimal places."
            );
        }

        BigDecimal maximumDeposit =
                new BigDecimal(
                        "9999999999.99"
                );

        if (
                deposit.compareTo(
                        maximumDeposit
                ) > 0
        ) {

            throw new IllegalArgumentException(
                    "Deposit amount is too large."
            );
        }
    }

    private void normalizePatient(
            Patient patient
    ) {

        patient.setIdType(
                normalizeIdType(
                        patient.getIdType()
                )
        );

        patient.setIdNumber(
                patient.getIdNumber()
                        .trim()
                        .toUpperCase()
        );

        patient.setName(
                patient.getName()
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        )
        );

        patient.setGender(
                capitalize(
                        patient.getGender()
                )
        );

        patient.setDisease(
                patient.getDisease()
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        )
        );

        patient.setRoomNumber(
                patient.getRoomNumber()
                        .trim()
        );

        patient.setAdmissionTime(
                patient.getAdmissionTime()
                        .trim()
        );
    }

    private String normalizeIdType(
            String idType
    ) {

        String normalizedType =
                idType.trim();

        if (
                "Aadhar Card".equalsIgnoreCase(
                        normalizedType
                )
                        ||
                        "Aadhaar Card".equalsIgnoreCase(
                                normalizedType
                        )
        ) {

            return "Aadhar Card";
        }

        if (
                "Voter Id".equalsIgnoreCase(
                        normalizedType
                )
                        ||
                        "Voter ID".equalsIgnoreCase(
                                normalizedType
                        )
        ) {

            return "Voter Id";
        }

        if (
                "Driving License".equalsIgnoreCase(
                        normalizedType
                )
        ) {

            return "Driving License";
        }

        throw new IllegalArgumentException(
                "Unsupported identity type: "
                        + normalizedType
        );
    }

    private String capitalize(
            String value
    ) {

        String cleaned =
                value.trim();

        if (cleaned.isEmpty()) {
            return cleaned;
        }

        return cleaned
                .substring(0, 1)
                .toUpperCase()
                + cleaned
                .substring(1)
                .toLowerCase();
    }

    private void require(
            String value,
            String fieldName
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    fieldName
                            + " is required."
            );
        }
    }
}