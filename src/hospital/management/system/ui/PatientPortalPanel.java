package hospital.management.system.ui;

import hospital.management.system.dao.PatientDAO;
import hospital.management.system.model.LoggedInUser;
import hospital.management.system.model.Patient;
import hospital.management.system.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Optional;

/**
 * Patient role ke liye privacy-safe portal.
 *
 * Logged-in patient sirf apna linked record dekh sakta hai.
 */
public final class PatientPortalPanel extends JPanel {

    public enum Section {
        HOME,
        PROFILE,
        ADMISSION,
        ROOM
    }

    private static final Color NAVY =
            new Color(7, 44, 90);

    private static final Color BLUE =
            new Color(22, 104, 220);

    private static final Color TEAL =
            new Color(13, 177, 177);

    private static final Color PAGE_BACKGROUND =
            new Color(245, 248, 252);

    private static final Color MUTED_TEXT =
            new Color(88, 104, 130);

    private static final Color BORDER_COLOR =
            new Color(220, 228, 238);

    private final PatientDAO patientDAO =
            new PatientDAO();

    private final LoggedInUser currentUser =
            SessionManager
                    .getCurrentUser()
                    .orElse(null);

    private final Section section;

    private final JPanel contentPanel =
            new JPanel();

    public PatientPortalPanel(
            Section section
    ) {

        this.section =
                section == null
                        ? Section.HOME
                        : section;

        configurePanel();

        add(
                createHeader(),
                BorderLayout.NORTH
        );

        configureContentPanel();

        JScrollPane scrollPane =
                new JScrollPane(
                        contentPanel,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                );

        scrollPane.setBorder(null);

        scrollPane.getViewport()
                .setBackground(
                        PAGE_BACKGROUND
                );

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(16);

        add(
                scrollPane,
                BorderLayout.CENTER
        );

        loadPatient();
    }

    private void configurePanel() {

        setLayout(
                new BorderLayout(
                        0,
                        20
                )
        );

        setBackground(
                PAGE_BACKGROUND
        );

        setBorder(
                new EmptyBorder(
                        28,
                        32,
                        28,
                        32
                )
        );
    }

    private void configureContentPanel() {

        contentPanel.setBackground(
                PAGE_BACKGROUND
        );

        contentPanel.setLayout(
                new BoxLayout(
                        contentPanel,
                        BoxLayout.Y_AXIS
                )
        );
    }

    private JComponent createHeader() {

        JPanel headerPanel =
                new JPanel(
                        new BorderLayout()
                );

        headerPanel.setOpaque(false);

        JPanel titlePanel =
                new JPanel();

        titlePanel.setOpaque(false);

        titlePanel.setLayout(
                new BoxLayout(
                        titlePanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel titleLabel =
                new JLabel(
                        getSectionTitle()
                );

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        28
                )
        );

        titleLabel.setForeground(NAVY);

        titleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel subtitleLabel =
                new JLabel(
                        "Secure personal hospital information"
                );

        subtitleLabel.setForeground(
                MUTED_TEXT
        );

        subtitleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        subtitleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        titlePanel.add(titleLabel);

        titlePanel.add(
                Box.createVerticalStrut(4)
        );

        titlePanel.add(subtitleLabel);

        JButton refreshButton =
                new ModernButton("REFRESH");

        refreshButton.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        refreshButton.setForeground(
                Color.WHITE
        );

        refreshButton.setBackground(BLUE);

        refreshButton.setFocusPainted(false);

        refreshButton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        refreshButton.setBorder(
                new EmptyBorder(
                        11,
                        18,
                        11,
                        18
                )
        );

        refreshButton.addActionListener(
                event -> loadPatient()
        );

        headerPanel.add(
                titlePanel,
                BorderLayout.WEST
        );

        headerPanel.add(
                refreshButton,
                BorderLayout.EAST
        );

        return headerPanel;
    }

    private String getSectionTitle() {

        return switch (section) {

            case HOME ->
                    "Patient Home";

            case PROFILE ->
                    "My Profile";

            case ADMISSION ->
                    "Admission Details";

            case ROOM ->
                    "My Room";
        };
    }

    private void loadPatient() {

        contentPanel.removeAll();

        contentPanel.add(
                createMessageCard(
                        "Loading your record...",
                        MUTED_TEXT
                )
        );

        refreshContent();

        SwingWorker<Optional<Patient>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Optional<Patient> doInBackground()
                            throws Exception {

                        if (
                                currentUser == null
                                        ||
                                        !currentUser.hasPatientNumber()
                        ) {
                            return Optional.empty();
                        }

                        return patientDAO.findByIdNumber(
                                currentUser.getPatientNumber()
                        );
                    }

                    @Override
                    protected void done() {

                        contentPanel.removeAll();

                        try {

                            Optional<Patient> result =
                                    get();

                            if (result.isEmpty()) {

                                contentPanel.add(
                                        createMessageCard(
                                                "No patient record is linked "
                                                        + "to this login. Please "
                                                        + "contact the administrator.",
                                                new Color(
                                                        190,
                                                        92,
                                                        20
                                                )
                                        )
                                );

                            } else {

                                renderPatient(
                                        result.get()
                                );
                            }

                        } catch (Exception exception) {

                            contentPanel.add(
                                    createMessageCard(
                                            "Your patient record could not "
                                                    + "be loaded from the database.",
                                            new Color(
                                                    205,
                                                    57,
                                                    66
                                            )
                                    )
                            );
                        }

                        refreshContent();
                    }
                };

        worker.execute();
    }

    private void renderPatient(
            Patient patient
    ) {

        if (
                section == Section.HOME
        ) {

            renderHome(patient);

        } else if (
                section == Section.PROFILE
        ) {

            renderProfile(patient);

        } else if (
                section == Section.ADMISSION
        ) {

            renderAdmission(patient);

        } else {

            renderRoom(patient);
        }
    }

    private void renderHome(
            Patient patient
    ) {

        JPanel greetingCard =
                createCard();

        greetingCard.setLayout(
                new BorderLayout()
        );

        JLabel greetingLabel =
                new JLabel(
                        "<html>"
                                + "<b style='font-size:18px'>"
                                + "Welcome, "
                                + escapeHtml(
                                patient.getName()
                        )
                                + "</b><br><br>"
                                + "Here is your current hospital "
                                + "stay overview."
                                + "</html>"
                );

        greetingLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        greetingLabel.setForeground(NAVY);

        greetingLabel.setBorder(
                new EmptyBorder(
                        4,
                        4,
                        4,
                        4
                )
        );

        greetingCard.add(
                greetingLabel,
                BorderLayout.CENTER
        );

        greetingCard.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        130
                )
        );

        contentPanel.add(greetingCard);

        contentPanel.add(
                Box.createVerticalStrut(18)
        );

        JPanel summaryPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                18,
                                0
                        )
                );

        summaryPanel.setOpaque(false);

        summaryPanel.add(
                createSummaryCard(
                        "Admission Status",
                        safeValue(
                                patient.getStatus()
                        ),
                        BLUE
                )
        );

        summaryPanel.add(
                createSummaryCard(
                        "Room Number",
                        safeValue(
                                patient.getRoomNumber()
                        ),
                        TEAL
                )
        );

        summaryPanel.add(
                createSummaryCard(
                        "Deposit",
                        "₹ "
                                + patient.getDeposit(),
                        new Color(
                                246,
                                139,
                                32
                        )
                )
        );

        summaryPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        165
                )
        );

        contentPanel.add(summaryPanel);
    }

    private void renderProfile(
            Patient patient
    ) {

        JPanel detailsCard =
                createDetailsCard();

        addDetail(
                detailsCard,
                "Patient ID",
                patient.getIdNumber()
        );

        addDetail(
                detailsCard,
                "ID Type",
                patient.getIdType()
        );

        addDetail(
                detailsCard,
                "Full Name",
                patient.getName()
        );

        addDetail(
                detailsCard,
                "Gender",
                patient.getGender()
        );

        contentPanel.add(detailsCard);
    }

    private void renderAdmission(
            Patient patient
    ) {

        JPanel detailsCard =
                createDetailsCard();

        addDetail(
                detailsCard,
                "Status",
                patient.getStatus()
        );

        addDetail(
                detailsCard,
                "Disease / Reason",
                patient.getDisease()
        );

        addDetail(
                detailsCard,
                "Admission Time",
                String.valueOf(
                        patient.getAdmissionTime()
                )
        );

        String dischargeTime =
                patient.getDischargeTime() == null
                        ? "Not discharged"
                        : patient.getDischargeTime()
                        .toString();

        addDetail(
                detailsCard,
                "Discharge Time",
                dischargeTime
        );

        addDetail(
                detailsCard,
                "Deposit",
                "₹ "
                        + patient.getDeposit()
        );

        contentPanel.add(detailsCard);
    }

    private void renderRoom(
            Patient patient
    ) {

        JPanel detailsCard =
                createDetailsCard();

        addDetail(
                detailsCard,
                "Room Number",
                patient.getRoomNumber()
        );

        addDetail(
                detailsCard,
                "Current Status",
                patient.getStatus()
        );

        addDetail(
                detailsCard,
                "Patient Name",
                patient.getName()
        );

        addDetail(
                detailsCard,
                "Need Assistance?",
                "Please contact the nursing desk"
        );

        contentPanel.add(detailsCard);

        contentPanel.add(
                Box.createVerticalStrut(18)
        );

        contentPanel.add(
                createMessageCard(
                        "For emergency assistance, immediately "
                                + "contact hospital staff or the "
                                + "nearest nursing desk.",
                        new Color(
                                190,
                                92,
                                20
                        )
                )
        );
    }

    private JPanel createDetailsCard() {

        JPanel detailsCard =
                createCard();

        detailsCard.setLayout(
                new GridLayout(
                        0,
                        2,
                        20,
                        14
                )
        );

        detailsCard.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        420
                )
        );

        return detailsCard;
    }

    private JPanel createSummaryCard(
            String title,
            String value,
            Color accentColor
    ) {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout(
                        0,
                        12
                )
        );

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                5,
                                0,
                                0,
                                0,
                                accentColor
                        ),
                        new EmptyBorder(
                                20,
                                20,
                                20,
                                20
                        )
                )
        );

        JLabel headingLabel =
                new JLabel(title);

        headingLabel.setForeground(
                MUTED_TEXT
        );

        headingLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        JLabel valueLabel =
                new JLabel(
                        safeValue(value)
                );

        valueLabel.setForeground(NAVY);

        valueLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        25
                )
        );

        panel.add(
                headingLabel,
                BorderLayout.NORTH
        );

        panel.add(
                valueLabel,
                BorderLayout.CENTER
        );

        return panel;
    }

    private void addDetail(
            JPanel panel,
            String label,
            String value
    ) {

        JLabel nameLabel =
                new JLabel(label);

        nameLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        nameLabel.setForeground(
                MUTED_TEXT
        );

        JLabel valueLabel =
                new JLabel(
                        escapeHtml(value)
                );

        valueLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        valueLabel.setForeground(NAVY);

        panel.add(nameLabel);
        panel.add(valueLabel);
    }

    private JPanel createCard() {

        JPanel panel =
                new JPanel();

        panel.setBackground(Color.WHITE);

        panel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(
                                24,
                                24,
                                24,
                                24
                        )
                )
        );

        return panel;
    }

    private JPanel createMessageCard(
            String message,
            Color color
    ) {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout()
        );

        JLabel messageLabel =
                new JLabel(
                        "<html>"
                                + escapeHtml(message)
                                + "</html>"
                );

        messageLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        messageLabel.setForeground(color);

        panel.add(
                messageLabel,
                BorderLayout.CENTER
        );

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        100
                )
        );

        return panel;
    }

    private void refreshContent() {

        contentPanel.revalidate();
        contentPanel.repaint();

        revalidate();
        repaint();
    }

    private static String safeValue(
            String text
    ) {

        if (
                text == null
                        ||
                        text.isBlank()
        ) {
            return "—";
        }

        return text;
    }

    private static String escapeHtml(
            String text
    ) {

        return safeValue(text)
                .replace(
                        "&",
                        "&amp;"
                )
                .replace(
                        "<",
                        "&lt;"
                )
                .replace(
                        ">",
                        "&gt;"
                );
    }
}
