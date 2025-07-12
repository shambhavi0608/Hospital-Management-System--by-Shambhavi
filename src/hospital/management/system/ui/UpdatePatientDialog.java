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
import java.util.List;

/**
 * Existing admitted patient update dialog.
 */
public final class UpdatePatientDialog extends JDialog {

    private static final Color NAVY = new Color(6, 45, 91);
    private static final Color BLUE = new Color(23, 104, 218);
    private static final Color PAGE = new Color(245, 248, 252);
    private static final Color ERROR = new Color(200, 55, 65);
    private static final Color SUCCESS = new Color(25, 135, 84);

    private final Patient originalPatient;
    private final PatientService patientService;
    private final Runnable successAction;

    private final JTextField idNumberField;
    private final JTextField idTypeField;
    private final JTextField nameField;

    private final JComboBox<String> genderBox =
            new JComboBox<>(new String[]{
                    "Male",
                    "Female",
                    "Other"
            });

    private final JTextField diseaseField;
    private final JComboBox<String> roomBox =
            new JComboBox<>();

    private final JTextField depositField;

    private final JButton updateButton =
            new JButton("UPDATE PATIENT");

    private final JLabel statusLabel =
            new JLabel("Loading available rooms...");

    public UpdatePatientDialog(
            Window owner,
            Patient patient,
            PatientService patientService,
            Runnable successAction
    ) {
        super(
                owner,
                "Update Patient",
                ModalityType.APPLICATION_MODAL
        );

        if (patient == null) {
            throw new IllegalArgumentException(
                    "Patient cannot be null."
            );
        }

        if (patientService == null) {
            throw new IllegalArgumentException(
                    "PatientService cannot be null."
            );
        }

        this.originalPatient = patient;
        this.patientService = patientService;
        this.successAction = successAction;

        idNumberField =
                createReadOnlyField(
                        patient.getIdNumber()
                );

        idTypeField =
                createReadOnlyField(
                        patient.getIdType()
                );

        nameField =
                new JTextField(
                        patient.getName()
                );

        diseaseField =
                new JTextField(
                        patient.getDisease()
                );

        depositField =
                new JTextField(
                        patient.getDeposit() == null
                                ? "0"
                                : patient.getDeposit()
                                .toPlainString()
                );

        genderBox.setSelectedItem(
                patient.getGender()
        );

        ((AbstractDocument) depositField.getDocument())
                .setDocumentFilter(
                        new DecimalFilter(10, 2)
                );

        configureDialog();
        createInterface();
        loadAvailableRooms();
    }

    private void configureDialog() {

        setSize(650, 640);
        setMinimumSize(new Dimension(600, 590));
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
        getRootPane().setDefaultButton(updateButton);
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
                new JLabel("Update Patient Details");

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
                        "Identity details remain protected"
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

        styleInput(idNumberField);
        styleInput(idTypeField);
        styleInput(nameField);
        styleInput(genderBox);
        styleInput(diseaseField);
        styleInput(roomBox);
        styleInput(depositField);

        GridBagConstraints constraints =
                new GridBagConstraints();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(8, 7, 8, 7);

        int row = 0;

        addRow(
                form,
                constraints,
                row++,
                "ID Type",
                idTypeField
        );

        addRow(
                form,
                constraints,
                row++,
                "ID Number",
                idNumberField
        );

        addRow(
                form,
                constraints,
                row++,
                "Patient Name *",
                nameField
        );

        addRow(
                form,
                constraints,
                row++,
                "Gender *",
                genderBox
        );

        addRow(
                form,
                constraints,
                row++,
                "Disease / Reason *",
                diseaseField
        );

        addRow(
                form,
                constraints,
                row++,
                "Room *",
                roomBox
        );

        addRow(
                form,
                constraints,
                row++,
                "Deposit (₹) *",
                depositField
        );

        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(14, 7, 5, 7);
        form.add(statusLabel, constraints);

        constraints.gridy = row + 1;
        constraints.insets = new Insets(18, 7, 0, 7);
        form.add(createButtons(), constraints);

        outer.add(form, BorderLayout.CENTER);

        return outer;
    }

    private void addRow(
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
        stylePrimaryButton(updateButton);

        cancelButton.addActionListener(
                event -> dispose()
        );

        updateButton.addActionListener(
                event -> updatePatient()
        );

        panel.add(cancelButton);
        panel.add(updateButton);

        return panel;
    }

    private JTextField createReadOnlyField(
            String value
    ) {

        JTextField field =
                new JTextField(value);

        field.setEditable(false);

        field.setBackground(
                new Color(238, 242, 247)
        );

        field.setForeground(
                new Color(80, 94, 115)
        );

        return field;
    }

    private void loadAvailableRooms() {

        updateButton.setEnabled(false);
        roomBox.removeAllItems();

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

                            String currentRoom =
                                    originalPatient
                                            .getRoomNumber();

                            if (
                                    currentRoom != null
                                            &&
                                            !currentRoom.isBlank()
                            ) {
                                roomBox.addItem(currentRoom);
                            }

                            for (String room : get()) {

                                if (
                                        !room.equals(
                                                currentRoom
                                        )
                                ) {
                                    roomBox.addItem(room);
                                }
                            }

                            roomBox.setSelectedItem(
                                    currentRoom
                            );

                            setStatus(
                                    "Patient details can now be updated.",
                                    SUCCESS
                            );

                            updateButton.setEnabled(true);

                        } catch (Exception exception) {

                            showError(
                                    rootMessage(exception)
                            );
                        }
                    }
                };

        worker.execute();
    }

    private void updatePatient() {

        String name =
                nameField
                        .getText()
                        .trim();

        String disease =
                diseaseField
                        .getText()
                        .trim();

        String selectedRoom =
                (String) roomBox.getSelectedItem();

        if (
                !name.matches(
                        "[\\p{L} .'-]{2,60}"
                )
        ) {
            nameField.requestFocusInWindow();
            showError("Enter a valid patient name.");
            return;
        }

        if (disease.isBlank()) {
            diseaseField.requestFocusInWindow();
            showError(
                    "Disease or admission reason is required."
            );
            return;
        }

        if (
                selectedRoom == null
                        ||
                        selectedRoom.isBlank()
        ) {
            showError("Select a valid room.");
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

        Patient updatedPatient =
                new Patient(
                        originalPatient.getIdType(),
                        originalPatient.getIdNumber(),
                        name,
                        String.valueOf(
                                genderBox.getSelectedItem()
                        ),
                        disease,
                        selectedRoom,
                        originalPatient.getAdmissionTime(),
                        deposit,
                        originalPatient.getStatus(),
                        originalPatient.getDischargeTime()
                );

        updateButton.setEnabled(false);
        updateButton.setText("UPDATING...");

        setStatus(
                "Updating patient information...",
                BLUE
        );

        SwingWorker<Void, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Void doInBackground()
                            throws Exception {

                        patientService
                                .updatePatient(
                                        updatedPatient
                                );

                        return null;
                    }

                    @Override
                    protected void done() {

                        try {

                            get();

                            JOptionPane.showMessageDialog(
                                    UpdatePatientDialog.this,
                                    "Patient successfully updated.",
                                    "Update Successful",
                                    JOptionPane.INFORMATION_MESSAGE
                            );

                            if (successAction != null) {
                                successAction.run();
                            }

                            dispose();

                        } catch (Exception exception) {

                            updateButton.setEnabled(true);
                            updateButton.setText("UPDATE PATIENT");

                            showError(
                                    rootMessage(exception)
                            );
                        }
                    }
                };

        worker.execute();
    }

    private void styleInput(
            JComponent component
    ) {

        component.setPreferredSize(
                new Dimension(310, 40)
        );

        component.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

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
                "Update Patient",
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
                ? "Patient update failed."
                : current.getMessage();
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