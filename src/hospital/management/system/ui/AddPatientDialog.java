package hospital.management.system.ui;

import hospital.management.system.model.Patient;
import hospital.management.system.service.PatientService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * New patient admission form with live ID validation.
 */
public final class AddPatientDialog extends JDialog {

    private static final Color NAVY = new Color(6, 45, 91);
    private static final Color BLUE = new Color(23, 104, 218);
    private static final Color PAGE = new Color(245, 248, 252);
    private static final Color MUTED = new Color(88, 104, 130);
    private static final Color ERROR = new Color(200, 55, 65);
    private static final Color SUCCESS = new Color(25, 135, 84);

    private final PatientService patientService;
    private final Runnable successAction;

    private final JComboBox<String> idTypeBox =
            new JComboBox<>(new String[]{
                    "Aadhar Card",
                    "Voter Id",
                    "Driving License"
            });

    private final JTextField idNumberField = new JTextField();
    private final JTextField nameField = new JTextField();

    private final JComboBox<String> genderBox =
            new JComboBox<>(new String[]{
                    "Male",
                    "Female",
                    "Other"
            });

    private final JComboBox<String> diseaseBox =
            new JComboBox<>(new String[]{
                    "General Check-up",
                    "Fever",
                    "Infection",
                    "Cardiac Problem",
                    "Respiratory Problem",
                    "Neurological Problem",
                    "Orthopaedic Injury",
                    "Maternity Care",
                    "Paediatric Care",
                    "Surgery",
                    "Emergency",
                    "Other"
            });

    private final JTextField otherDiseaseField =
            new JTextField();

    private final JComboBox<String> roomBox =
            new JComboBox<>();

    private final JTextField depositField =
            new JTextField();

    private final JLabel idHintLabel =
            new JLabel();

    private final JLabel statusLabel =
            new JLabel("Loading available rooms...");

    private final JButton saveButton =
            new JButton("ADMIT PATIENT");

    public AddPatientDialog(
            Window owner,
            PatientService patientService,
            Runnable successAction
    ) {
        super(
                owner,
                "Add New Patient",
                ModalityType.APPLICATION_MODAL
        );

        if (patientService == null) {
            throw new IllegalArgumentException(
                    "PatientService cannot be null."
            );
        }

        this.patientService = patientService;
        this.successAction = successAction;

        configureFields();
        configureDialog();
        createInterface();
        loadAvailableRooms();
    }

    private void configureFields() {

        diseaseBox.setEditable(false);
        otherDiseaseField.setEnabled(false);

        idTypeBox.addActionListener(event -> {
            idNumberField.setText("");
            configureIdField();
        });

        diseaseBox.addActionListener(event -> {
            boolean otherSelected =
                    "Other".equals(
                            diseaseBox.getSelectedItem()
                    );

            otherDiseaseField.setEnabled(otherSelected);

            if (otherSelected) {
                otherDiseaseField.requestFocusInWindow();
            } else {
                otherDiseaseField.setText("");
            }
        });

        configureIdField();

        ((AbstractDocument) depositField.getDocument())
                .setDocumentFilter(
                        new DecimalFilter(10, 2)
                );
    }

    private void configureIdField() {

        String idType =
                String.valueOf(
                        idTypeBox.getSelectedItem()
                );

        AbstractDocument document =
                (AbstractDocument)
                        idNumberField.getDocument();

        if ("Aadhar Card".equals(idType)) {

            document.setDocumentFilter(
                    new IdFilter(12, true)
            );

            idHintLabel.setText(
                    "Aadhaar must contain exactly 12 digits."
            );

            idNumberField.setToolTipText(
                    "Example: 123456789012"
            );

        } else if ("Voter Id".equals(idType)) {

            document.setDocumentFilter(
                    new IdFilter(10, false)
            );

            idHintLabel.setText(
                    "Voter ID must contain exactly 10 letters/numbers."
            );

            idNumberField.setToolTipText(
                    "Example: ABC1234567"
            );

        } else {

            document.setDocumentFilter(
                    new IdFilter(16, false)
            );

            idHintLabel.setText(
                    "Driving Licence must contain 15–16 letters/numbers."
            );

            idNumberField.setToolTipText(
                    "Enter without spaces."
            );
        }
    }

    private void configureDialog() {

        setSize(680, 730);
        setMinimumSize(new Dimension(630, 680));
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
    }

    private void createInterface() {

        JPanel root =
                new JPanel(new BorderLayout());

        root.setBackground(PAGE);
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createFormContainer(), BorderLayout.CENTER);

        setContentPane(root);
        getRootPane().setDefaultButton(saveButton);
    }

    private JPanel createHeader() {

        JPanel header = new JPanel();
        header.setBackground(NAVY);

        header.setLayout(
                new BoxLayout(
                        header,
                        BoxLayout.Y_AXIS
                )
        );

        header.setBorder(
                new EmptyBorder(23, 30, 21, 30)
        );

        JLabel title =
                new JLabel("New Patient Admission");

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        25
                )
        );

        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle =
                new JLabel(
                        "Enter verified identity and admission information"
                );

        subtitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        subtitle.setForeground(
                new Color(186, 214, 242)
        );

        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        return header;
    }

    private JPanel createFormContainer() {

        JPanel outer =
                new JPanel(new BorderLayout());

        outer.setBackground(PAGE);

        outer.setBorder(
                new EmptyBorder(20, 28, 20, 28)
        );

        JPanel form =
                new JPanel(new GridBagLayout());

        form.setBackground(Color.WHITE);

        form.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 228, 238)
                        ),
                        new EmptyBorder(22, 26, 22, 26)
                )
        );

        styleInput(idTypeBox);
        styleInput(idNumberField);
        styleInput(nameField);
        styleInput(genderBox);
        styleInput(diseaseBox);
        styleInput(otherDiseaseField);
        styleInput(roomBox);
        styleInput(depositField);

        GridBagConstraints constraints =
                new GridBagConstraints();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(6, 8, 6, 8);

        int row = 0;

        row = addRow(
                form,
                constraints,
                row,
                "ID Type *",
                idTypeBox
        );

        row = addRow(
                form,
                constraints,
                row,
                "ID Number *",
                idNumberField
        );

        idHintLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        idHintLabel.setForeground(MUTED);

        constraints.gridx = 1;
        constraints.gridy = row;
        constraints.insets = new Insets(0, 8, 6, 8);
        form.add(idHintLabel, constraints);
        row++;

        constraints.insets = new Insets(6, 8, 6, 8);

        row = addRow(
                form,
                constraints,
                row,
                "Patient Name *",
                nameField
        );

        row = addRow(
                form,
                constraints,
                row,
                "Gender *",
                genderBox
        );

        row = addRow(
                form,
                constraints,
                row,
                "Disease / Reason *",
                diseaseBox
        );

        row = addRow(
                form,
                constraints,
                row,
                "Other Disease",
                otherDiseaseField
        );

        row = addRow(
                form,
                constraints,
                row,
                "Available Room *",
                roomBox
        );

        row = addRow(
                form,
                constraints,
                row,
                "Deposit (₹) *",
                depositField
        );

        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(12, 8, 5, 8);
        form.add(statusLabel, constraints);

        constraints.gridy = row + 1;
        constraints.insets = new Insets(18, 8, 0, 8);
        form.add(createButtons(), constraints);

        outer.add(form, BorderLayout.CENTER);

        return outer;
    }

    private int addRow(
            JPanel form,
            GridBagConstraints constraints,
            int row,
            String labelText,
            JComponent component
    ) {

        constraints.gridwidth = 1;
        constraints.gridy = row;

        constraints.gridx = 0;
        constraints.weightx = 0.32;

        JLabel label = new JLabel(labelText);
        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );
        label.setForeground(NAVY);

        form.add(label, constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.68;

        form.add(component, constraints);

        return row + 1;
    }

    private JPanel createButtons() {

        JPanel panel =
                new JPanel(
                        new GridLayout(1, 2, 12, 0)
                );

        panel.setOpaque(false);

        JButton cancelButton =
                new JButton("CANCEL");

        styleSecondaryButton(cancelButton);
        stylePrimaryButton(saveButton);

        cancelButton.addActionListener(
                event -> dispose()
        );

        saveButton.addActionListener(
                event -> savePatient()
        );

        panel.add(cancelButton);
        panel.add(saveButton);

        return panel;
    }

    private void loadAvailableRooms() {

        saveButton.setEnabled(false);
        roomBox.removeAllItems();

        setStatus(
                "Loading available rooms...",
                MUTED
        );

        SwingWorker<List<String>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<String> doInBackground()
                            throws Exception {

                        return patientService
                                .getAvailableRoomNumbers();
                    }

                    @Override
                    protected void done() {

                        try {

                            List<String> rooms = get();

                            for (String room : rooms) {
                                roomBox.addItem(room);
                            }

                            if (rooms.isEmpty()) {

                                setStatus(
                                        "No rooms are currently available.",
                                        ERROR
                                );

                            } else {

                                setStatus(
                                        rooms.size()
                                                + " room(s) available.",
                                        SUCCESS
                                );

                                saveButton.setEnabled(true);
                            }

                        } catch (Exception exception) {

                            showError(
                                    rootMessage(exception)
                            );
                        }
                    }
                };

        worker.execute();
    }

    private void savePatient() {

        String idType =
                String.valueOf(
                        idTypeBox.getSelectedItem()
                );

        String idNumber =
                idNumberField
                        .getText()
                        .trim()
                        .toUpperCase();

        String name =
                nameField
                        .getText()
                        .trim();

        String gender =
                String.valueOf(
                        genderBox.getSelectedItem()
                );

        String disease =
                getSelectedDisease();

        String roomNumber =
                (String) roomBox.getSelectedItem();

        String validationError =
                validateForm(
                        idType,
                        idNumber,
                        name,
                        disease,
                        roomNumber
                );

        if (validationError != null) {
            showError(validationError);
            return;
        }

        BigDecimal deposit;

        try {

            deposit = new BigDecimal(
                    depositField
                            .getText()
                            .trim()
            );

            if (
                    deposit.compareTo(
                            BigDecimal.ZERO
                    ) < 0
            ) {
                showError(
                        "Deposit cannot be negative."
                );
                return;
            }

        } catch (NumberFormatException exception) {

            showError(
                    "Enter a valid numeric deposit."
            );
            return;
        }

        Patient patient =
                new Patient(
                        idType,
                        idNumber,
                        name,
                        gender,
                        disease,
                        roomNumber,
                        LocalDateTime.now()
                                .format(
                                        DateTimeFormatter
                                                .ofPattern(
                                                        "yyyy-MM-dd HH:mm:ss"
                                                )
                                ),
                        deposit
                );

        saveButton.setEnabled(false);
        saveButton.setText("ADMITTING...");

        setStatus(
                "Patient admission is being saved...",
                BLUE
        );

        SwingWorker<Void, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Void doInBackground()
                            throws Exception {

                        patientService
                                .admitPatient(patient);

                        return null;
                    }

                    @Override
                    protected void done() {

                        try {

                            get();

                            JOptionPane.showMessageDialog(
                                    AddPatientDialog.this,
                                    "Patient successfully admitted.",
                                    "Admission Successful",
                                    JOptionPane.INFORMATION_MESSAGE
                            );

                            if (successAction != null) {
                                successAction.run();
                            }

                            dispose();

                        } catch (Exception exception) {

                            saveButton.setEnabled(true);
                            saveButton.setText("ADMIT PATIENT");

                            showError(
                                    rootMessage(exception)
                            );
                        }
                    }
                };

        worker.execute();
    }

    private String validateForm(
            String idType,
            String idNumber,
            String name,
            String disease,
            String room
    ) {

        if (
                "Aadhar Card".equals(idType)
                        &&
                        !idNumber.matches("[0-9]{12}")
        ) {
            idNumberField.requestFocusInWindow();
            return "Aadhaar must contain exactly 12 digits.";
        }

        if (
                "Voter Id".equals(idType)
                        &&
                        !idNumber.matches("[A-Z0-9]{10}")
        ) {
            idNumberField.requestFocusInWindow();
            return "Voter ID must contain exactly 10 letters/numbers.";
        }

        if (
                "Driving License".equals(idType)
                        &&
                        !idNumber.matches("[A-Z0-9]{15,16}")
        ) {
            idNumberField.requestFocusInWindow();
            return "Driving Licence must contain 15–16 letters/numbers.";
        }

        if (
                !name.matches(
                        "[\\p{L} .'-]{2,60}"
                )
        ) {
            nameField.requestFocusInWindow();
            return "Enter a valid patient name.";
        }

        if (
                disease == null
                        ||
                        disease.isBlank()
        ) {
            return "Enter disease or admission reason.";
        }

        if (
                room == null
                        ||
                        room.isBlank()
        ) {
            return "Select an available room.";
        }

        if (
                depositField
                        .getText()
                        .isBlank()
        ) {
            depositField.requestFocusInWindow();
            return "Enter the deposit amount.";
        }

        return null;
    }

    private String getSelectedDisease() {

        String selected =
                String.valueOf(
                        diseaseBox.getSelectedItem()
                );

        if ("Other".equals(selected)) {
            return otherDiseaseField
                    .getText()
                    .trim();
        }

        return selected;
    }

    private void styleInput(
            JComponent component
    ) {

        component.setPreferredSize(
                new Dimension(300, 39)
        );

        component.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        component.setBackground(Color.WHITE);

        component.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(205, 215, 230)
                        ),
                        new EmptyBorder(0, 10, 0, 10)
                )
        );
    }

    private void stylePrimaryButton(
            JButton button
    ) {

        button.setBackground(BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        button.setBorder(
                new EmptyBorder(12, 18, 12, 18)
        );
    }

    private void styleSecondaryButton(
            JButton button
    ) {

        button.setBackground(Color.WHITE);
        button.setForeground(BLUE);
        button.setFocusPainted(false);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BLUE),
                        new EmptyBorder(11, 18, 11, 18)
                )
        );
    }

    private void setStatus(
            String message,
            Color color
    ) {

        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void showError(
            String message
    ) {

        setStatus(message, ERROR);

        JOptionPane.showMessageDialog(
                this,
                message,
                "Patient Admission",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private String rootMessage(
            Throwable throwable
    ) {

        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() == null
                ? "Unknown error occurred."
                : current.getMessage();
    }

    private static final class IdFilter
            extends DocumentFilter {

        private final int maximumLength;
        private final boolean digitsOnly;

        IdFilter(
                int maximumLength,
                boolean digitsOnly
        ) {
            this.maximumLength = maximumLength;
            this.digitsOnly = digitsOnly;
        }

        @Override
        public void insertString(
                FilterBypass bypass,
                int offset,
                String text,
                AttributeSet attributes
        ) throws BadLocationException {

            replace(
                    bypass,
                    offset,
                    0,
                    text,
                    attributes
            );
        }

        @Override
        public void replace(
                FilterBypass bypass,
                int offset,
                int length,
                String text,
                AttributeSet attributes
        ) throws BadLocationException {

            if (text == null) {
                return;
            }

            String cleaned =
                    digitsOnly
                            ? text.replaceAll("\\D", "")
                            : text.toUpperCase()
                            .replaceAll(
                                    "[^A-Z0-9]",
                                    ""
                            );

            int newLength =
                    bypass.getDocument().getLength()
                            - length
                            + cleaned.length();

            if (newLength <= maximumLength) {
                bypass.replace(
                        offset,
                        length,
                        cleaned,
                        attributes
                );
            }
        }
    }

    private static final class DecimalFilter
            extends DocumentFilter {

        private final int integerDigits;
        private final int decimalDigits;

        DecimalFilter(
                int integerDigits,
                int decimalDigits
        ) {
            this.integerDigits = integerDigits;
            this.decimalDigits = decimalDigits;
        }

        @Override
        public void insertString(
                FilterBypass bypass,
                int offset,
                String text,
                AttributeSet attributes
        ) throws BadLocationException {

            replace(
                    bypass,
                    offset,
                    0,
                    text,
                    attributes
            );
        }

        @Override
        public void replace(
                FilterBypass bypass,
                int offset,
                int length,
                String text,
                AttributeSet attributes
        ) throws BadLocationException {

            if (text == null) {
                return;
            }

            String oldText =
                    bypass.getDocument()
                            .getText(
                                    0,
                                    bypass.getDocument()
                                            .getLength()
                            );

            String newText =
                    oldText.substring(0, offset)
                            + text
                            + oldText.substring(
                            offset + length
                    );

            String pattern =
                    "\\d{0,"
                            + integerDigits
                            + "}(\\.\\d{0,"
                            + decimalDigits
                            + "})?";

            if (
                    newText.isEmpty()
                            ||
                            newText.matches(pattern)
            ) {
                bypass.replace(
                        offset,
                        length,
                        text,
                        attributes
                );
            }
        }
    }
}