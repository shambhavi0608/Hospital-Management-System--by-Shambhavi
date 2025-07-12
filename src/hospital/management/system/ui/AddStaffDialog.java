package hospital.management.system.ui;

import hospital.management.system.dao.StaffDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

/**
 * Admin ke liye Doctor/Nurse add karne ka dialog.
 *
 * Ye ek saath:
 * 1. Login account create karta hai.
 * 2. Staff information database mein save karta hai.
 */
public final class AddStaffDialog extends JDialog {

    private static final Color NAVY =
            new Color(6, 45, 91);

    private static final Color BLUE =
            new Color(23, 104, 218);

    private static final Color PAGE_BACKGROUND =
            new Color(245, 248, 252);

    private static final Color BORDER_COLOR =
            new Color(205, 215, 230);

    private final StaffDAO staffDAO;
    private final Runnable savedAction;

    private final JTextField nameField =
            new JTextField();

    private final JComboBox<String> roleBox =
            new JComboBox<>(
                    new String[]{
                            "DOCTOR",
                            "NURSE"
                    }
            );

    private final JComboBox<String> departmentBox =
            new JComboBox<>(
                    new String[]{
                            "Emergency Medicine",
                            "Cardiology",
                            "Neurology",
                            "Orthopaedics",
                            "Paediatrics",
                            "Obstetrics & Gynaecology",
                            "General Medicine",
                            "General Surgery",
                            "Dermatology",
                            "ENT",
                            "Ophthalmology",
                            "Oncology",
                            "Psychiatry",
                            "Radiology",
                            "Pathology",
                            "Anaesthesiology",
                            "Intensive Care (ICU)",
                            "Nephrology",
                            "Urology",
                            "Pulmonology",
                            "Gastroenterology",
                            "Physiotherapy"
                    }
            );

    private final JTextField specializationField =
            new JTextField();

    private final JTextField phoneField =
            new JTextField();

    private final JTextField emailField =
            new JTextField();

    private final JComboBox<String> shiftBox =
            new JComboBox<>(
                    new String[]{
                            "Morning",
                            "Evening",
                            "Night",
                            "Rotational"
                    }
            );

    private final JTextField loginIdField =
            new JTextField();

    private final JPasswordField passwordField =
            new JPasswordField();

    private final JButton saveButton =
            new ModernButton("ADD STAFF");

    public AddStaffDialog(
            Window owner,
            StaffDAO staffDAO,
            Runnable savedAction
    ) {

        super(
                owner,
                "Add Doctor / Nurse",
                ModalityType.APPLICATION_MODAL
        );

        if (staffDAO == null) {
            throw new IllegalArgumentException(
                    "StaffDAO cannot be null."
            );
        }

        this.staffDAO = staffDAO;
        this.savedAction = savedAction;

        configureDialog();
        createInterface();
    }

    private void configureDialog() {

        setSize(680, 700);

        setMinimumSize(
                new Dimension(640, 650)
        );

        setDefaultCloseOperation(
                DISPOSE_ON_CLOSE
        );

        setResizable(true);

        setLocationRelativeTo(
                getOwner()
        );
    }

    private void createInterface() {

        JPanel rootPanel =
                new JPanel(
                        new BorderLayout()
                );

        rootPanel.setBackground(
                PAGE_BACKGROUND
        );

        rootPanel.add(
                createHeader(),
                BorderLayout.NORTH
        );

        rootPanel.add(
                createFormPanel(),
                BorderLayout.CENTER
        );

        setContentPane(rootPanel);

        getRootPane().setDefaultButton(
                saveButton
        );
    }

    private JPanel createHeader() {

        JPanel header =
                new JPanel();

        header.setLayout(
                new BoxLayout(
                        header,
                        BoxLayout.Y_AXIS
                )
        );

        header.setBackground(NAVY);

        header.setBorder(
                new EmptyBorder(
                        24,
                        30,
                        22,
                        30
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "Add New Staff Member"
                );

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        25
                )
        );

        titleLabel.setForeground(
                Color.WHITE
        );

        titleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel subtitleLabel =
                new JLabel(
                        "Create a doctor or nurse profile and secure login"
                );

        subtitleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        subtitleLabel.setForeground(
                new Color(
                        186,
                        214,
                        242
                )
        );

        subtitleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        header.add(titleLabel);

        header.add(
                Box.createVerticalStrut(5)
        );

        header.add(subtitleLabel);

        return header;
    }

    private JPanel createFormPanel() {

        JPanel outerPanel =
                new JPanel(
                        new BorderLayout()
                );

        outerPanel.setBackground(
                PAGE_BACKGROUND
        );

        outerPanel.setBorder(
                new EmptyBorder(
                        20,
                        24,
                        20,
                        24
                )
        );

        JPanel formPanel =
                new JPanel(
                        new GridBagLayout()
                );

        formPanel.setBackground(
                Color.WHITE
        );

        formPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        226,
                                        232,
                                        240
                                )
                        ),
                        new EmptyBorder(
                                22,
                                26,
                                22,
                                26
                        )
                )
        );

        styleInput(nameField);
        styleInput(roleBox);
        styleInput(departmentBox);
        styleInput(specializationField);
        styleInput(phoneField);
        styleInput(emailField);
        styleInput(shiftBox);
        styleInput(loginIdField);
        styleInput(passwordField);

        GridBagConstraints constraints =
                new GridBagConstraints();

        constraints.fill =
                GridBagConstraints.HORIZONTAL;

        constraints.weightx = 1.0;

        constraints.insets =
                new Insets(
                        6,
                        8,
                        6,
                        8
                );

        int row = 0;

        row = addDoubleFieldRow(
                formPanel,
                constraints,
                row,
                "Full Name *",
                nameField,
                "Role *",
                roleBox
        );

        row = addDoubleFieldRow(
                formPanel,
                constraints,
                row,
                "Department *",
                departmentBox,
                "Specialization",
                specializationField
        );

        row = addDoubleFieldRow(
                formPanel,
                constraints,
                row,
                "Phone",
                phoneField,
                "Email",
                emailField
        );

        row = addDoubleFieldRow(
                formPanel,
                constraints,
                row,
                "Shift",
                shiftBox,
                "Login Username *",
                loginIdField
        );

        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 4;

        formPanel.add(
                createLabel(
                        "Initial Password *"
                ),
                constraints
        );

        constraints.gridy =
                row + 1;

        formPanel.add(
                passwordField,
                constraints
        );

        JLabel passwordHint =
                new JLabel(
                        "Password must contain at least 6 characters."
                );

        passwordHint.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        passwordHint.setForeground(
                new Color(
                        100,
                        116,
                        139
                )
        );

        constraints.gridy =
                row + 2;

        constraints.insets =
                new Insets(
                        0,
                        8,
                        8,
                        8
                );

        formPanel.add(
                passwordHint,
                constraints
        );

        constraints.gridy =
                row + 3;

        constraints.insets =
                new Insets(
                        22,
                        8,
                        0,
                        8
                );

        formPanel.add(
                createActionPanel(),
                constraints
        );

        outerPanel.add(
                formPanel,
                BorderLayout.CENTER
        );

        return outerPanel;
    }

    private int addDoubleFieldRow(
            JPanel formPanel,
            GridBagConstraints constraints,
            int row,
            String leftLabelText,
            JComponent leftComponent,
            String rightLabelText,
            JComponent rightComponent
    ) {

        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.gridy = row;

        formPanel.add(
                createLabel(leftLabelText),
                constraints
        );

        constraints.gridx = 2;

        formPanel.add(
                createLabel(rightLabelText),
                constraints
        );

        constraints.gridx = 0;
        constraints.gridy = row + 1;
        constraints.gridwidth = 2;

        formPanel.add(
                leftComponent,
                constraints
        );

        constraints.gridx = 2;
        constraints.gridwidth = 2;

        formPanel.add(
                rightComponent,
                constraints
        );

        return row + 2;
    }

    private JPanel createActionPanel() {

        JPanel actionsPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                12,
                                0
                        )
                );

        actionsPanel.setOpaque(false);

        JButton cancelButton =
                new ModernButton("CANCEL");

        styleSecondaryButton(
                cancelButton
        );

        stylePrimaryButton(
                saveButton
        );

        cancelButton.addActionListener(
                event -> dispose()
        );

        saveButton.addActionListener(
                event -> saveStaff()
        );

        actionsPanel.add(
                cancelButton
        );

        actionsPanel.add(
                saveButton
        );

        return actionsPanel;
    }

    private void saveStaff() {

        String name =
                nameField.getText().trim();

        String department =
                String.valueOf(
                        departmentBox.getSelectedItem()
                ).trim();

        String specialization =
                specializationField.getText().trim();

        String phone =
                phoneField.getText().trim();

        String email =
                emailField.getText().trim();

        String loginId =
                loginIdField.getText().trim();

        String selectedRole =
                (String) roleBox.getSelectedItem();

        String selectedShift =
                (String) shiftBox.getSelectedItem();

        char[] password =
                passwordField.getPassword();

        if (name.isBlank()) {

            showWarning(
                    "Please enter the staff member's full name."
            );

            nameField.requestFocusInWindow();

            clearPasswordArray(password);

            return;
        }

        if (department.isBlank()) {

            showWarning(
                    "Please enter the department."
            );

            departmentBox.requestFocusInWindow();

            clearPasswordArray(password);

            return;
        }

        if (loginId.isBlank()) {

            showWarning(
                    "Please enter a login username."
            );

            loginIdField.requestFocusInWindow();

            clearPasswordArray(password);

            return;
        }

        if (password.length < 6) {

            showWarning(
                    "Password must contain at least 6 characters."
            );

            passwordField.requestFocusInWindow();

            clearPasswordArray(password);

            return;
        }

        if (
                !email.isBlank()
                        &&
                        !email.matches(
                                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                        )
        ) {

            showWarning(
                    "Please enter a valid email address."
            );

            emailField.requestFocusInWindow();

            clearPasswordArray(password);

            return;
        }

        if (
                !phone.isBlank()
                        &&
                        !phone.matches(
                                "(?:\\+91[- ]?)?[6-9][0-9]{9}"
                        )
        ) {

            showWarning(
                    "Enter a valid 10-digit Indian mobile number "
                            + "(optionally starting with +91)."
            );

            phoneField.requestFocusInWindow();

            clearPasswordArray(password);

            return;
        }

        saveButton.setEnabled(false);

        saveButton.setText(
                "SAVING..."
        );

        SwingWorker<Void, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Void doInBackground()
                            throws Exception {

                        if (
                                staffDAO.loginIdExists(
                                        loginId
                                )
                        ) {
                            throw new IllegalArgumentException(
                                    "Login username already exists."
                            );
                        }

                        staffDAO.addStaff(
                                name,
                                selectedRole,
                                department,
                                specialization,
                                phone,
                                email,
                                selectedShift,
                                loginId,
                                password
                        );

                        return null;
                    }

                    @Override
                    protected void done() {

                        clearPasswordArray(
                                password
                        );

                        saveButton.setEnabled(
                                true
                        );

                        saveButton.setText(
                                "ADD STAFF"
                        );

                        try {
                            get();

                            JOptionPane.showMessageDialog(
                                    AddStaffDialog.this,
                                    selectedRole
                                            + " added successfully.\n"
                                            + "Login username: "
                                            + loginId,
                                    "Staff Added",
                                    JOptionPane.INFORMATION_MESSAGE
                            );

                            if (savedAction != null) {
                                savedAction.run();
                            }

                            dispose();

                        } catch (Exception exception) {

                            showDatabaseError(
                                    rootMessage(
                                            exception
                                    )
                            );
                        }
                    }
                };

        worker.execute();
    }

    private JLabel createLabel(
            String text
    ) {

        JLabel label =
                new JLabel(text);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        label.setForeground(NAVY);

        return label;
    }

    private void styleInput(
            JComponent component
    ) {

        component.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        component.setPreferredSize(
                new Dimension(
                        230,
                        42
                )
        );

        component.setBackground(
                Color.WHITE
        );

        component.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(
                                0,
                                10,
                                0,
                                10
                        )
                )
        );
    }

    private void stylePrimaryButton(
            JButton button
    ) {

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        button.setBackground(BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                new EmptyBorder(
                        12,
                        20,
                        12,
                        20
                )
        );
    }

    private void styleSecondaryButton(
            JButton button
    ) {

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        button.setBackground(Color.WHITE);
        button.setForeground(BLUE);
        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BLUE
                        ),
                        new EmptyBorder(
                                11,
                                20,
                                11,
                                20
                        )
                )
        );
    }

    private void showWarning(
            String message
    ) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Check Details",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showDatabaseError(
            String message
    ) {

        JOptionPane.showMessageDialog(
                this,
                "Could not add staff member.\n"
                        + message,
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void clearPasswordArray(
            char[] password
    ) {

        if (password != null) {
            Arrays.fill(
                    password,
                    '\0'
            );
        }
    }

    private String rootMessage(
            Throwable throwable
    ) {

        Throwable current =
                throwable;

        while (
                current.getCause()
                        != null
        ) {
            current =
                    current.getCause();
        }

        if (
                current.getMessage()
                        == null
        ) {
            return "Unknown error occurred.";
        }

        return current.getMessage();
    }
}
