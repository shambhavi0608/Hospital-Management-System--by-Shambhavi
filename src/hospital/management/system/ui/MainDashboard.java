package hospital.management.system.ui;

import hospital.management.system.Login;
import hospital.management.system.dao.DashboardDAO;
import hospital.management.system.model.LoggedInUser;
import hospital.management.system.model.UserRole;
import hospital.management.system.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MainDashboard extends JFrame {

    private static final Color NAVY = new Color(5, 39, 86);
    private static final Color NAVY_HOVER = new Color(8, 64, 120);
    private static final Color BLUE = new Color(20, 101, 219);
    private static final Color TEAL = new Color(12, 174, 184);
    private static final Color ORANGE = new Color(255, 139, 13);
    private static final Color PURPLE = new Color(105, 82, 235);
    private static final Color RED = new Color(244, 75, 85);
    private static final Color GREEN = new Color(21, 184, 128);

    private static final Color PAGE_BACKGROUND =
            new Color(245, 248, 252);

    private static final Color PRIMARY_TEXT =
            new Color(12, 38, 78);

    private static final Color MUTED_TEXT =
            new Color(83, 101, 129);

    private static final String DASHBOARD = "DASHBOARD";
    private static final String PATIENTS = "PATIENTS";
    private static final String STAFF = "STAFF";
    private static final String ROOMS = "ROOMS";
    private static final String ANALYTICS = "ANALYTICS";
    private static final String ASSISTANT = "ASSISTANT";
    private static final String PROFILE = "PROFILE";
    private static final String ADMISSION = "ADMISSION";
    private static final String MY_ROOM = "MY_ROOM";

    private final DashboardDAO dashboardDAO =
            new DashboardDAO();

    private final LoggedInUser currentUser =
            SessionManager.getCurrentUser().orElse(null);

    private final CardLayout cardLayout =
            new CardLayout();

    private final JPanel pageContainer =
            new JPanel(cardLayout);

    private final Map<String, NavigationButton>
            navigationButtons = new LinkedHashMap<>();

    private MetricCard patientsCard;
    private MetricCard availableRoomsCard;
    private MetricCard occupiedRoomsCard;
    private MetricCard doctorsCard;
    private MetricCard nursesCard;

    private JLabel databaseStatusLabel;
    private JLabel lastUpdatedLabel;

    private PatientPanel patientPanel;
    private StaffManagementPanel staffPanel;

    private Timer refreshTimer;

    private String currentPage = DASHBOARD;

    public MainDashboard() {

        if (currentUser == null) {
            SwingUtilities.invokeLater(
                    () -> Login.main(new String[0])
            );

            dispose();
            return;
        }

        configureWindow();
        registerPages();

        setLayout(new BorderLayout());

        add(
                createSidebar(),
                BorderLayout.WEST
        );

        add(
                pageContainer,
                BorderLayout.CENTER
        );

        showPage(DASHBOARD);

        setVisible(true);

        if (!isPatient()) {
            refreshDashboardData();
            startAutoRefresh();
        }
    }

    private void configureWindow() {

        setTitle(
                "Smart Hospital Management System"
        );

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setSize(1550, 900);

        setMinimumSize(
                new Dimension(1250, 720)
        );

        setLocationRelativeTo(null);

        getContentPane().setBackground(
                PAGE_BACKGROUND
        );

        setExtendedState(
                JFrame.MAXIMIZED_BOTH
        );
    }

    private void registerPages() {

        pageContainer.setBackground(
                PAGE_BACKGROUND
        );

        if (isPatient()) {

            pageContainer.add(
                    new PatientPortalPanel(
                            PatientPortalPanel.Section.HOME
                    ),
                    DASHBOARD
            );

            pageContainer.add(
                    new PatientPortalPanel(
                            PatientPortalPanel.Section.PROFILE
                    ),
                    PROFILE
            );

            pageContainer.add(
                    new PatientPortalPanel(
                            PatientPortalPanel.Section.ADMISSION
                    ),
                    ADMISSION
            );

            pageContainer.add(
                    new PatientPortalPanel(
                            PatientPortalPanel.Section.ROOM
                    ),
                    MY_ROOM
            );

        } else {

            pageContainer.add(
                    createDashboardPage(),
                    DASHBOARD
            );

            patientPanel =
                    new PatientPanel(
                            this::refreshDashboardData
                    );

            pageContainer.add(
                    patientPanel,
                    PATIENTS
            );

            pageContainer.add(
                    new RoomRecommendationPanel(),
                    ROOMS
            );

            if (isAdmin()) {

                staffPanel =
                        new StaffManagementPanel();

                pageContainer.add(
                        staffPanel,
                        STAFF
                );
            }

            if (isAdmin() || isDoctor()) {

                pageContainer.add(
                        new AnalyticsPanel(),
                        ANALYTICS
                );
            }
        }

        pageContainer.add(
                new ChatbotPanel(),
                ASSISTANT
        );
    }

    // ============================================================
    // SIDEBAR
    // ============================================================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();

        sidebar.setLayout(
                new BoxLayout(
                        sidebar,
                        BoxLayout.Y_AXIS
                )
        );

        sidebar.setBackground(NAVY);

        // Increased width so complete navigation labels are visible.
        sidebar.setPreferredSize(
                new Dimension(340, 0)
        );

        sidebar.setMinimumSize(
                new Dimension(340, 0)
        );

        sidebar.setBorder(
                new EmptyBorder(
                        28,
                        18,
                        22,
                        18
                )
        );

        sidebar.add(createBrandPanel());

        sidebar.add(
                Box.createVerticalStrut(30)
        );

        if (isPatient()) {

            addNavigationButton(
                    sidebar,
                    DASHBOARD,
                    "Home",
                    IconType.HOME
            );

            addNavigationButton(
                    sidebar,
                    PROFILE,
                    "My Profile",
                    IconType.USER
            );

            addNavigationButton(
                    sidebar,
                    ADMISSION,
                    "Admission Details",
                    IconType.PATIENT
            );

            addNavigationButton(
                    sidebar,
                    MY_ROOM,
                    "My Room",
                    IconType.ROOM
            );

        } else {

            addNavigationButton(
                    sidebar,
                    DASHBOARD,
                    "Dashboard",
                    IconType.DASHBOARD
            );

            addNavigationButton(
                    sidebar,
                    PATIENTS,
                    "Patients",
                    IconType.PATIENT
            );

            if (isAdmin()) {

                addNavigationButton(
                        sidebar,
                        STAFF,
                        "Staff Management",
                        IconType.STAFF
                );
            }

            addNavigationButton(
                    sidebar,
                    ROOMS,
                    "Smart Room Finder",
                    IconType.ROOM
            );

            if (isAdmin() || isDoctor()) {

                addNavigationButton(
                        sidebar,
                        ANALYTICS,
                        "Analytics",
                        IconType.ANALYTICS
                );
            }
        }

        addNavigationButton(
                sidebar,
                ASSISTANT,
                "Multilingual Assistant",
                IconType.CHAT
        );

        sidebar.add(
                Box.createVerticalGlue()
        );

        NavigationButton logoutButton =
                new NavigationButton(
                        "Logout",
                        IconType.LOGOUT
                );

        logoutButton.addActionListener(
                event -> logoutUser()
        );

        sidebar.add(logoutButton);

        sidebar.add(
                Box.createVerticalStrut(18)
        );

        sidebar.add(createUserCard());

        return sidebar;
    }

    private JPanel createBrandPanel() {

        JPanel brandPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                12,
                                0
                        )
                );

        brandPanel.setOpaque(false);

        brandPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        72
                )
        );

        brandPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel hospitalIcon =
                new JLabel(
                        new VectorIcon(
                                IconType.HOSPITAL,
                                48,
                                Color.WHITE
                        )
                );

        JPanel textPanel = new JPanel();

        textPanel.setOpaque(false);

        textPanel.setLayout(
                new BoxLayout(
                        textPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel brandName =
                new JLabel("SMART HMS");

        brandName.setForeground(Color.WHITE);

        brandName.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24
                )
        );

        JLabel portal =
                new JLabel("Operations Portal");

        portal.setForeground(
                new Color(
                        174,
                        207,
                        238
                )
        );

        portal.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        textPanel.add(brandName);

        textPanel.add(
                Box.createVerticalStrut(3)
        );

        textPanel.add(portal);

        brandPanel.add(hospitalIcon);
        brandPanel.add(textPanel);

        return brandPanel;
    }

    private void addNavigationButton(
            JPanel sidebar,
            String pageName,
            String text,
            IconType iconType
    ) {

        NavigationButton button =
                new NavigationButton(
                        text,
                        iconType
                );

        button.addActionListener(
                event -> {

                    if (
                            PATIENTS.equals(pageName)
                                    && patientPanel != null
                    ) {
                        patientPanel.refreshPatients();
                    }

                    if (
                            STAFF.equals(pageName)
                                    && staffPanel != null
                    ) {
                        staffPanel.refreshStaff();
                    }

                    showPage(pageName);
                }
        );

        navigationButtons.put(
                pageName,
                button
        );

        sidebar.add(button);

        sidebar.add(
                Box.createVerticalStrut(7)
        );
    }

    private JPanel createUserCard() {

        RoundedPanel card =
                new RoundedPanel(
                        new Color(
                                8,
                                54,
                                108
                        ),
                        18
                );

        card.setLayout(
                new BorderLayout(
                        12,
                        0
                )
        );

        card.setBorder(
                new EmptyBorder(
                        14,
                        14,
                        14,
                        14
                )
        );

        card.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        84
                )
        );

        card.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel icon =
                new JLabel(
                        new VectorIcon(
                                IconType.USER,
                                45,
                                Color.WHITE
                        )
                );

        JPanel textPanel = new JPanel();

        textPanel.setOpaque(false);

        textPanel.setLayout(
                new BoxLayout(
                        textPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel username =
                new JLabel(displayUsername());

        username.setForeground(Color.WHITE);

        username.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        JLabel role =
                new JLabel(displayRole());

        role.setForeground(
                new Color(
                        77,
                        232,
                        224
                )
        );

        role.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        textPanel.add(username);

        textPanel.add(
                Box.createVerticalStrut(5)
        );

        textPanel.add(role);

        card.add(
                icon,
                BorderLayout.WEST
        );

        card.add(
                textPanel,
                BorderLayout.CENTER
        );

        return card;
    }

    // ============================================================
    // DASHBOARD
    // ============================================================

    private JPanel createDashboardPage() {

        JPanel page =
                new JPanel(
                        new BorderLayout()
                );

        page.setBackground(
                PAGE_BACKGROUND
        );

        JPanel content =
                new JPanel();

        content.setBackground(
                PAGE_BACKGROUND
        );

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        content.setBorder(
                new EmptyBorder(
                        30,
                        34,
                        30,
                        34
                )
        );

        content.add(
                createDashboardHeader()
        );

        content.add(
                Box.createVerticalStrut(24)
        );

        content.add(
                createMetricCards()
        );

        content.add(
                Box.createVerticalStrut(24)
        );

        content.add(
                createOverviewSection()
        );

        content.add(
                Box.createVerticalStrut(24)
        );

        content.add(
                createQuickActionsSection()
        );

        JScrollPane scrollPane =
                new JScrollPane(content);

        scrollPane.setBorder(null);

        scrollPane.getViewport()
                .setBackground(
                        PAGE_BACKGROUND
                );

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(18);

        page.add(
                scrollPane,
                BorderLayout.CENTER
        );

        return page;
    }

    private JPanel createDashboardHeader() {

        JPanel header =
                new JPanel(
                        new BorderLayout(
                                20,
                                0
                        )
                );

        header.setOpaque(false);

        JPanel greeting =
                new JPanel();

        greeting.setOpaque(false);

        greeting.setLayout(
                new BoxLayout(
                        greeting,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel welcome =
                new JLabel(
                        "Welcome, "
                                + displayUsername()
                );

        welcome.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        30
                )
        );

        welcome.setForeground(
                PRIMARY_TEXT
        );

        JLabel subtitle =
                new JLabel(
                        "Here is your live hospital overview"
                );

        subtitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        subtitle.setForeground(
                MUTED_TEXT
        );

        greeting.add(welcome);

        greeting.add(
                Box.createVerticalStrut(5)
        );

        greeting.add(subtitle);

        JPanel right =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                12,
                                6
                        )
                );

        right.setOpaque(false);

        JLabel userIcon =
                new JLabel(
                        new VectorIcon(
                                IconType.USER,
                                38,
                                BLUE
                        )
                );

        JPanel identity =
                new JPanel();

        identity.setOpaque(false);

        identity.setLayout(
                new BoxLayout(
                        identity,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel name =
                new JLabel(
                        displayUsername()
                );

        name.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        name.setForeground(
                PRIMARY_TEXT
        );

        JLabel role =
                new JLabel(
                        displayRole()
                );

        role.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        role.setForeground(
                MUTED_TEXT
        );

        identity.add(name);
        identity.add(role);

        JButton refresh =
                createActionButton(
                        "REFRESH",
                        BLUE
                );

        refresh.addActionListener(
                event -> refreshDashboardData()
        );

        right.add(userIcon);
        right.add(identity);
        right.add(refresh);

        header.add(
                greeting,
                BorderLayout.WEST
        );

        header.add(
                right,
                BorderLayout.EAST
        );

        return header;
    }

    private JPanel createMetricCards() {

        JPanel panel =
                new JPanel(
                        new GridLayout(
                                1,
                                5,
                                16,
                                0
                        )
                );

        panel.setOpaque(false);

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        160
                )
        );

        patientsCard =
                new MetricCard(
                        "Admitted Patients",
                        BLUE,
                        IconType.PATIENT
                );

        availableRoomsCard =
                new MetricCard(
                        "Available Rooms",
                        TEAL,
                        IconType.ROOM
                );

        occupiedRoomsCard =
                new MetricCard(
                        "Occupied Rooms",
                        ORANGE,
                        IconType.BED
                );

        doctorsCard =
                new MetricCard(
                        "Active Doctors",
                        PURPLE,
                        IconType.DOCTOR
                );

        nursesCard =
                new MetricCard(
                        "Active Nurses",
                        RED,
                        IconType.NURSE
                );

        panel.add(patientsCard);
        panel.add(availableRoomsCard);
        panel.add(occupiedRoomsCard);
        panel.add(doctorsCard);
        panel.add(nursesCard);

        return panel;
    }

    private JPanel createOverviewSection() {

        JPanel overviewPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                20,
                                0
                        )
                );

        overviewPanel.setOpaque(false);

        /*
         * Fixed height prevents the occupancy card
         * from being vertically compressed.
         */
        overviewPanel.setPreferredSize(
                new Dimension(
                        1000,
                        250
                )
        );

        overviewPanel.setMinimumSize(
                new Dimension(
                        800,
                        250
                )
        );

        overviewPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        250
                )
        );

        overviewPanel.add(
                createOccupancyCard()
        );

        overviewPanel.add(
                createSystemStatusCard()
        );

        return overviewPanel;
    }

    private JPanel createOccupancyCard() {

        RoundedPanel card =
                createContentCard();

        card.setLayout(
                new BorderLayout(
                        0,
                        10
                )
        );

        card.add(
                createSectionTitle(
                        "Room Occupancy Overview"
                ),
                BorderLayout.NORTH
        );

        card.add(
                new OccupancyInformationPanel(),
                BorderLayout.CENTER
        );

        return card;
    }

    private JPanel createSystemStatusCard() {

        RoundedPanel card =
                createContentCard();

        card.setLayout(
                new BorderLayout(
                        0,
                        15
                )
        );

        card.add(
                createSectionTitle(
                        "System Status"
                ),
                BorderLayout.NORTH
        );

        JPanel status =
                new JPanel();

        status.setOpaque(false);

        status.setLayout(
                new BoxLayout(
                        status,
                        BoxLayout.Y_AXIS
                )
        );

        databaseStatusLabel =
                createStatusLabel(
                        "Checking database connection...",
                        ORANGE
                );

        JLabel autoRefresh =
                createStatusLabel(
                        "Auto-refresh: Every 10 seconds",
                        GREEN
                );

        lastUpdatedLabel =
                createStatusLabel(
                        "Last updated: Waiting",
                        MUTED_TEXT
                );

        JLabel session =
                createStatusLabel(
                        "Session role: "
                                + displayRole(),
                        BLUE
                );

        status.add(databaseStatusLabel);

        status.add(
                Box.createVerticalStrut(17)
        );

        status.add(autoRefresh);

        status.add(
                Box.createVerticalStrut(17)
        );

        status.add(lastUpdatedLabel);

        status.add(
                Box.createVerticalStrut(17)
        );

        status.add(session);

        card.add(
                status,
                BorderLayout.CENTER
        );

        return card;
    }

    // ============================================================
    // QUICK ACTIONS
    // ============================================================

    private JPanel createQuickActionsSection() {

        RoundedPanel card =
                createContentCard();

        card.setLayout(
                new BorderLayout(
                        0,
                        15
                )
        );

        card.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        180
                )
        );

        card.add(
                createSectionTitle(
                        "Quick Actions"
                ),
                BorderLayout.NORTH
        );

        int columns = isAdmin() ? 5 : 4;

        JPanel actions =
                new JPanel(
                        new GridLayout(
                                1,
                                columns,
                                14,
                                0
                        )
                );

        actions.setOpaque(false);

        actions.add(
                createQuickButton(
                        "Patients",
                        IconType.PATIENT,
                        BLUE,
                        () -> showPage(PATIENTS)
                )
        );

        if (isAdmin()) {

            actions.add(
                    createQuickButton(
                            "Staff",
                            IconType.STAFF,
                            PURPLE,
                            () -> showPage(STAFF)
                    )
            );
        }

        actions.add(
                createQuickButton(
                        "Find Room",
                        IconType.ROOM,
                        ORANGE,
                        () -> showPage(ROOMS)
                )
        );

        if (isAdmin() || isDoctor()) {

            actions.add(
                    createQuickButton(
                            "Analytics",
                            IconType.ANALYTICS,
                            PURPLE,
                            () -> showPage(ANALYTICS)
                    )
            );
        }

        actions.add(
                createQuickButton(
                        "Assistant",
                        IconType.CHAT,
                        TEAL,
                        () -> showPage(ASSISTANT)
                )
        );

        card.add(
                actions,
                BorderLayout.CENTER
        );

        return card;
    }

    /*
     * Custom painted dashboard button.
     * Isse Windows Look & Feel button ko grey nahi karega.
     */
    private JButton createQuickButton(
            String text,
            IconType iconType,
            Color background,
            Runnable action
    ) {

        DashboardButton button =
                new DashboardButton(
                        text,
                        iconType,
                        background
                );

        button.addActionListener(
                event -> action.run()
        );

        return button;
    }

    private JButton createActionButton(
            String text,
            Color background
    ) {

        DashboardActionButton button =
                new DashboardActionButton(
                        text,
                        background
                );

        return button;
    }

    // ============================================================
    // PAGE NAVIGATION
    // ============================================================

    private void showPage(
            String pageName
    ) {

        if (
                ANALYTICS.equals(pageName)
                        && !(isAdmin() || isDoctor())
        ) {
            showAccessRestricted();
            return;
        }

        if (
                STAFF.equals(pageName)
                        && !isAdmin()
        ) {
            showAccessRestricted();
            return;
        }

        if (
                PATIENTS.equals(pageName)
                        && isPatient()
        ) {
            showAccessRestricted();
            return;
        }

        currentPage = pageName;

        cardLayout.show(
                pageContainer,
                pageName
        );

        for (
                Map.Entry<String, NavigationButton> entry
                : navigationButtons.entrySet()
        ) {

            entry.getValue().setActive(
                    entry.getKey().equals(pageName)
            );
        }

        if (
                DASHBOARD.equals(pageName)
                        && !isPatient()
        ) {
            refreshDashboardData();
        }
    }

    private void showAccessRestricted() {

        JOptionPane.showMessageDialog(
                this,
                "Your account does not have permission "
                        + "to access this section.",
                "Access Restricted",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // ============================================================
    // LIVE DATABASE DATA
    // ============================================================

    private void refreshDashboardData() {

        if (
                isPatient()
                        || patientsCard == null
        ) {
            return;
        }

        setMetricsText("...");

        if (databaseStatusLabel != null) {
            databaseStatusLabel.setText(
                    "Loading live database data..."
            );
        }

        SwingWorker<int[], Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected int[] doInBackground()
                            throws Exception {

                        return new int[]{
                                dashboardDAO
                                        .getTotalPatients(),

                                dashboardDAO
                                        .getAvailableRooms(),

                                dashboardDAO
                                        .getOccupiedRooms(),

                                dashboardDAO
                                        .getActiveDoctors(),

                                dashboardDAO
                                        .getActiveNurses()
                        };
                    }

                    @Override
                    protected void done() {

                        try {

                            int[] result = get();

                            patientsCard.setValue(result[0]);
                            availableRoomsCard.setValue(result[1]);
                            occupiedRoomsCard.setValue(result[2]);
                            doctorsCard.setValue(result[3]);
                            nursesCard.setValue(result[4]);

                            databaseStatusLabel.setText(
                                    "Database connected successfully"
                            );

                            lastUpdatedLabel.setText(
                                    "Last updated: "
                                            + LocalTime.now()
                                            .format(
                                                    DateTimeFormatter
                                                            .ofPattern(
                                                                    "hh:mm:ss a"
                                                            )
                                            )
                            );

                            repaint();

                        } catch (Exception exception) {

                            setMetricsText("Error");

                            databaseStatusLabel.setText(
                                    "Database connection failed"
                            );
                        }
                    }
                };

        worker.execute();
    }

    private void setMetricsText(
            String text
    ) {

        if (patientsCard != null) {
            patientsCard.setText(text);
        }

        if (availableRoomsCard != null) {
            availableRoomsCard.setText(text);
        }

        if (occupiedRoomsCard != null) {
            occupiedRoomsCard.setText(text);
        }

        if (doctorsCard != null) {
            doctorsCard.setText(text);
        }

        if (nursesCard != null) {
            nursesCard.setText(text);
        }
    }

    private void startAutoRefresh() {

        refreshTimer =
                new Timer(
                        10_000,
                        event -> {

                            if (
                                    DASHBOARD.equals(
                                            currentPage
                                    )
                            ) {
                                refreshDashboardData();
                            }
                        }
                );

        refreshTimer.start();
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    private void logoutUser() {

        int confirmation =
                JOptionPane.showConfirmDialog(
                        this,
                        "Do you want to logout from "
                                + displayUsername()
                                + "?",
                        "Confirm Logout",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (
                confirmation
                        != JOptionPane.YES_OPTION
        ) {
            return;
        }

        if (refreshTimer != null) {
            refreshTimer.stop();
        }

        SessionManager.endSession();

        dispose();

        SwingUtilities.invokeLater(
                () -> Login.main(
                        new String[0]
                )
        );
    }

    private String displayUsername() {

        return currentUser == null
                ? "Guest"
                : currentUser.getUsername();
    }

    private String displayRole() {

        return currentUser == null
                ? "NO SESSION"
                : currentUser
                .getRole()
                .getDisplayName()
                .toUpperCase();
    }

    private boolean isAdmin() {

        return currentUser != null
                && currentUser.getRole()
                == UserRole.ADMIN;
    }

    private boolean isDoctor() {

        return currentUser != null
                && currentUser.getRole()
                == UserRole.DOCTOR;
    }

    private boolean isPatient() {

        return currentUser != null
                && currentUser.getRole()
                == UserRole.PATIENT;
    }

    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(
                () -> {

                    if (
                            SessionManager
                                    .getCurrentUser()
                                    .isEmpty()
                    ) {
                        Login.main(
                                new String[0]
                        );
                    } else {
                        new MainDashboard();
                    }
                }
        );
    }

    // ============================================================
    // NAVIGATION BUTTON
    // ============================================================

    private final class NavigationButton
            extends JButton {

        private boolean active;

        NavigationButton(
                String text,
                IconType iconType
        ) {

            super(
                    text,
                    new VectorIcon(
                            iconType,
                            25,
                            Color.WHITE
                    )
            );

            setHorizontalAlignment(
                    SwingConstants.LEFT
            );

            setIconTextGap(16);

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            15
                    )
            );

            setForeground(Color.WHITE);

            setBackground(NAVY);

            setOpaque(true);

            setBorderPainted(false);

            setFocusPainted(false);

            setContentAreaFilled(true);

            setBorder(
                    new EmptyBorder(
                            13,
                            18,
                            13,
                            18
                    )
            );

            setPreferredSize(
                    new Dimension(
                            304,
                            52
                    )
            );

            setMinimumSize(
                    new Dimension(
                            304,
                            52
                    )
            );

            setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            52
                    )
            );

            setAlignmentX(
                    Component.LEFT_ALIGNMENT
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            addMouseListener(
                    new MouseAdapter() {

                        @Override
                        public void mouseEntered(
                                MouseEvent event
                        ) {

                            if (!active) {
                                setBackground(
                                        NAVY_HOVER
                                );
                            }
                        }

                        @Override
                        public void mouseExited(
                                MouseEvent event
                        ) {

                            if (!active) {
                                setBackground(NAVY);
                            }
                        }
                    }
            );
        }

        void setActive(
                boolean value
        ) {

            active = value;

            setBackground(
                    active
                            ? BLUE
                            : NAVY
            );

            setFont(
                    new Font(
                            "Segoe UI",
                            active
                                    ? Font.BOLD
                                    : Font.PLAIN,
                            15
                    )
            );
        }
    }

    // ============================================================
    // DASHBOARD QUICK BUTTON
    // ============================================================

    private static final class DashboardButton
            extends JButton {

        private final Color normalColor;
        private final Color hoverColor;
        private final Icon icon;

        DashboardButton(
                String text,
                IconType iconType,
                Color color
        ) {

            super(text);

            normalColor = color;

            hoverColor =
                    color.brighter();

            icon =
                    new VectorIcon(
                            iconType,
                            30,
                            Color.WHITE
                    );

            setForeground(Color.WHITE);

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            13
                    )
            );

            setHorizontalAlignment(
                    SwingConstants.CENTER
            );

            setVerticalTextPosition(
                    SwingConstants.BOTTOM
            );

            setHorizontalTextPosition(
                    SwingConstants.CENTER
            );

            setIcon(icon);

            setIconTextGap(8);

            setFocusPainted(false);

            setBorderPainted(false);

            setContentAreaFilled(false);

            setOpaque(false);

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            setBorder(
                    new EmptyBorder(
                            12,
                            8,
                            12,
                            8
                    )
            );

            addMouseListener(
                    new MouseAdapter() {

                        @Override
                        public void mouseEntered(
                                MouseEvent e
                        ) {
                            repaint();
                        }

                        @Override
                        public void mouseExited(
                                MouseEvent e
                        ) {
                            repaint();
                        }
                    }
            );
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {

            Graphics2D g =
                    (Graphics2D)
                            graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color fill =
                    getModel().isRollover()
                            ? hoverColor
                            : normalColor;

            g.setColor(fill);

            g.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    18,
                    18
            );

            g.dispose();

            super.paintComponent(graphics);
        }
    }

    private static final class DashboardActionButton
            extends JButton {

        private final Color color;

        DashboardActionButton(
                String text,
                Color color
        ) {

            super(text);

            this.color = color;

            setForeground(Color.WHITE);

            setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            12
                    )
            );

            setFocusPainted(false);

            setBorderPainted(false);

            setContentAreaFilled(false);

            setOpaque(false);

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            setBorder(
                    new EmptyBorder(
                            11,
                            18,
                            11,
                            18
                    )
            );
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {

            Graphics2D g =
                    (Graphics2D)
                            graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(
                    getModel().isPressed()
                            ? color.darker()
                            : getModel().isRollover()
                            ? color.brighter()
                            : color
            );

            g.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    14,
                    14
            );

            g.dispose();

            super.paintComponent(graphics);
        }
    }

    // ============================================================
    // METRIC CARD
    // ============================================================

    private static final class MetricCard
            extends RoundedPanel {

        private final JLabel valueLabel =
                new JLabel("...");

        MetricCard(
                String title,
                Color accent,
                IconType iconType
        ) {

            super(
                    Color.WHITE,
                    18
            );

            setLayout(
                    new BorderLayout(
                            12,
                            6
                    )
            );

            setBorder(
                    new EmptyBorder(
                            22,
                            18,
                            18,
                            18
                    )
            );

            JLabel icon =
                    new JLabel(
                            new VectorIcon(
                                    iconType,
                                    44,
                                    accent
                            )
                    );

            JPanel textPanel =
                    new JPanel();

            textPanel.setOpaque(false);

            textPanel.setLayout(
                    new BoxLayout(
                            textPanel,
                            BoxLayout.Y_AXIS
                    )
            );

            JLabel titleLabel =
                    new JLabel(title);

            titleLabel.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            12
                    )
            );

            titleLabel.setForeground(
                    MUTED_TEXT
            );

            valueLabel.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            29
                    )
            );

            valueLabel.setForeground(accent);

            JLabel live =
                    new JLabel("Live count");

            live.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            11
                    )
            );

            live.setForeground(
                    MUTED_TEXT
            );

            textPanel.add(titleLabel);

            textPanel.add(
                    Box.createVerticalStrut(7)
            );

            textPanel.add(valueLabel);

            textPanel.add(
                    Box.createVerticalStrut(5)
            );

            textPanel.add(live);

            add(
                    icon,
                    BorderLayout.WEST
            );

            add(
                    textPanel,
                    BorderLayout.CENTER
            );
        }

        void setValue(int value) {
            valueLabel.setText(
                    String.valueOf(value)
            );
        }

        void setText(String value) {
            valueLabel.setText(value);
        }
    }

    // ============================================================
    // OCCUPANCY
    // ============================================================
    private final class OccupancyInformationPanel
            extends JPanel {

        OccupancyInformationPanel() {

            setOpaque(false);

            setPreferredSize(
                    new Dimension(
                            500,
                            190
                    )
            );

            setMinimumSize(
                    new Dimension(
                            400,
                            180
                    )
            );
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {

            super.paintComponent(graphics);

            Graphics2D g =
                    (Graphics2D)
                            graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int available =
                    getMetricValue(
                            availableRoomsCard
                    );

            int occupied =
                    getMetricValue(
                            occupiedRoomsCard
                    );

            int total =
                    available + occupied;

            double occupancy =
                    total == 0
                            ? 0
                            : (occupied * 100.0) / total;

            /*
             * ---------------------------------------------------------
             * PIE CHART
             * ---------------------------------------------------------
             */

            int diameter = 135;

            int chartX = 20;

            int chartY =
                    Math.max(
                            8,
                            (getHeight() - diameter) / 2
                    );

            /*
             * Ring thickness
             */
            g.setStroke(
                    new BasicStroke(
                            25,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND
                    )
            );

            /*
             * Available portion
             */
            g.setColor(
                    TEAL
            );

            g.drawArc(
                    chartX,
                    chartY,
                    diameter,
                    diameter,
                    90,
                    360
            );

            /*
             * Occupied portion
             */
            g.setColor(
                    ORANGE
            );

            int occupiedAngle =
                    (int)
                            Math.round(
                                    -occupancy * 3.6
                            );

            g.drawArc(
                    chartX,
                    chartY,
                    diameter,
                    diameter,
                    90,
                    occupiedAngle
            );

            /*
             * ---------------------------------------------------------
             * CENTER PERCENTAGE
             * ---------------------------------------------------------
             */

            String percentageText =
                    Math.round(occupancy)
                            + "%";

            g.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            22
                    )
            );

            g.setColor(
                    PRIMARY_TEXT
            );

            FontMetrics percentageMetrics =
                    g.getFontMetrics();

            int percentageX =
                    chartX
                            + (
                            diameter
                                    - percentageMetrics
                                    .stringWidth(
                                            percentageText
                                    )
                    ) / 2;

            int percentageY =
                    chartY
                            + diameter / 2
                            + 7;

            g.drawString(
                    percentageText,
                    percentageX,
                    percentageY
            );

            /*
             * ---------------------------------------------------------
             * CENTER LABEL
             * ---------------------------------------------------------
             */

            String centerLabel =
                    "Occupied";

            g.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            11
                    )
            );

            g.setColor(
                    MUTED_TEXT
            );

            FontMetrics centerMetrics =
                    g.getFontMetrics();

            int centerX =
                    chartX
                            + (
                            diameter
                                    - centerMetrics
                                    .stringWidth(
                                            centerLabel
                                    )
                    ) / 2;

            int centerY =
                    chartY
                            + diameter / 2
                            + 25;

            g.drawString(
                    centerLabel,
                    centerX,
                    centerY
            );

            /*
             * ---------------------------------------------------------
             * LEGEND
             * ---------------------------------------------------------
             *
             * Enough vertical spacing so that both rows
             * remain completely visible.
             */

            int legendX =
                    chartX
                            + diameter
                            + 45;

            int legendY =
                    chartY
                            + 55;

            /*
             * Available Rooms
             */
            drawLegendItem(
                    g,
                    legendX,
                    legendY,
                    TEAL,
                    "Available Rooms",
                    available
            );

            /*
             * Occupied Rooms
             */
            drawLegendItem(
                    g,
                    legendX,
                    legendY + 48,
                    ORANGE,
                    "Occupied Rooms",
                    occupied
            );

            g.dispose();
        }

        private int getMetricValue(
                MetricCard card
        ) {

            try {

                return Integer.parseInt(
                        card.valueLabel
                                .getText()
                                .trim()
                );

            } catch (
                    NumberFormatException exception
            ) {

                return 0;
            }
        }

        private void drawLegendItem(
                Graphics2D g,
                int x,
                int y,
                Color color,
                String label,
                int value
        ) {

            /*
             * Color indicator
             */
            g.setColor(color);

            g.fillRoundRect(
                    x,
                    y - 11,
                    16,
                    16,
                    5,
                    5
            );

            /*
             * Label
             */
            g.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            13
                    )
            );

            g.setColor(
                    MUTED_TEXT
            );

            g.drawString(
                    label,
                    x + 27,
                    y + 2
            );

            /*
             * Value
             */
            g.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            14
                    )
            );

            g.setColor(
                    PRIMARY_TEXT
            );

            g.drawString(
                    String.valueOf(value),
                    x + 170,
                    y + 2
            );
        }
    }
    private int getMetricValue(
            MetricCard card
    ) {

        try {

            return Integer.parseInt(
                    card.valueLabel
                            .getText()
            );

        } catch (Exception e) {

            return 0;
        }
    }

    private void drawLegend(
            Graphics2D g,
            int x,
            int y,
            Color color,
            String label,
            int value
    ) {

        g.setColor(color);

        g.fillRoundRect(
                x,
                y - 13,
                16,
                16,
                5,
                5
        );

        g.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        g.setColor(MUTED_TEXT);

        g.drawString(
                label,
                x + 28,
                y
        );

        g.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        g.setColor(PRIMARY_TEXT);

        g.drawString(
                String.valueOf(value),
                x + 170,
                y
        );
    }


// ============================================================
// COMMON UI
// ============================================================

private RoundedPanel createContentCard() {

    RoundedPanel card =
            new RoundedPanel(
                    Color.WHITE,
                    18
            );

    card.setBorder(
            new EmptyBorder(
                    22,
                    22,
                    22,
                    22
            )
    );

    return card;
}

private JLabel createSectionTitle(
        String text
) {

    JLabel label =
            new JLabel(text);

    label.setFont(
            new Font(
                    "Segoe UI",
                    Font.BOLD,
                    18
            )
    );

    label.setForeground(
            PRIMARY_TEXT
    );

    return label;
}

private JLabel createStatusLabel(
        String text,
        Color color
) {

    JLabel label =
            new JLabel(
                    text,
                    new VectorIcon(
                            IconType.DOT,
                            13,
                            color
                    ),
                    SwingConstants.LEFT
            );

    label.setFont(
            new Font(
                    "Segoe UI",
                    Font.PLAIN,
                    13
            )
    );

    label.setForeground(
            MUTED_TEXT
    );

    label.setIconTextGap(10);

    return label;
}

// ============================================================
// ROUNDED PANEL
// ============================================================

private static class RoundedPanel
        extends JPanel {

    private final Color fillColor;
    private final int radius;

    RoundedPanel(
            Color fillColor,
            int radius
    ) {

        this.fillColor = fillColor;
        this.radius = radius;

        setOpaque(false);
    }

    @Override
    protected void paintComponent(
            Graphics graphics
    ) {

        Graphics2D g =
                (Graphics2D)
                        graphics.create();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setColor(
                new Color(
                        24,
                        45,
                        74,
                        18
                )
        );

        g.fillRoundRect(
                2,
                4,
                getWidth() - 4,
                getHeight() - 6,
                radius,
                radius
        );

        g.setColor(fillColor);

        g.fillRoundRect(
                0,
                0,
                getWidth() - 3,
                getHeight() - 6,
                radius,
                radius
        );

        g.dispose();

        super.paintComponent(graphics);
    }
}

// ============================================================
// ICONS
// ============================================================

private enum IconType {
    HOSPITAL,
    HOME,
    DASHBOARD,
    PATIENT,
    STAFF,
    ROOM,
    BED,
    ANALYTICS,
    CHAT,
    LOGOUT,
    USER,
    DOCTOR,
    NURSE,
    DOT
}

private static final class VectorIcon
        implements Icon {

    private final IconType iconType;
    private final int size;
    private final Color color;

    VectorIcon(
            IconType iconType,
            int size,
            Color color
    ) {

        this.iconType = iconType;
        this.size = size;
        this.color = color;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(
            Component component,
            Graphics graphics,
            int x,
            int y
    ) {

        Graphics2D g =
                (Graphics2D)
                        graphics.create();

        g.translate(x, y);

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        float scale =
                size / 32.0f;

        g.setColor(color);

        g.setStroke(
                new BasicStroke(
                        Math.max(
                                1.6f,
                                2.1f * scale
                        ),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        switch (iconType) {

            case DOT ->
                    g.fillOval(
                            size / 4,
                            size / 4,
                            size / 2,
                            size / 2
                    );

            case DASHBOARD -> {

                int square =
                        Math.round(
                                10 * scale
                        );

                int gap =
                        Math.round(
                                4 * scale
                        );

                g.fillRoundRect(
                        3,
                        3,
                        square,
                        square,
                        3,
                        3
                );

                g.fillRoundRect(
                        3 + square + gap,
                        3,
                        square,
                        square,
                        3,
                        3
                );

                g.fillRoundRect(
                        3,
                        3 + square + gap,
                        square,
                        square,
                        3,
                        3
                );

                g.fillRoundRect(
                        3 + square + gap,
                        3 + square + gap,
                        square,
                        square,
                        3,
                        3
                );
            }

            case HOME -> {

                g.drawLine(
                        4,
                        15,
                        16,
                        4
                );

                g.drawLine(
                        16,
                        4,
                        28,
                        15
                );

                g.drawRect(
                        7,
                        14,
                        18,
                        14
                );

                g.drawRect(
                        14,
                        20,
                        5,
                        8
                );
            }

            case USER, PATIENT -> {

                g.drawOval(
                        11,
                        3,
                        10,
                        10
                );

                g.drawArc(
                        5,
                        14,
                        22,
                        15,
                        0,
                        180
                );
            }

            case STAFF -> {

                g.drawOval(
                        12,
                        2,
                        8,
                        8
                );

                g.drawOval(
                        3,
                        7,
                        7,
                        7
                );

                g.drawOval(
                        22,
                        7,
                        7,
                        7
                );

                g.drawArc(
                        7,
                        12,
                        18,
                        15,
                        0,
                        180
                );
            }

            case ROOM, BED -> {

                g.drawLine(
                        4,
                        6,
                        4,
                        27
                );

                g.drawLine(
                        4,
                        21,
                        28,
                        21
                );

                g.drawLine(
                        28,
                        15,
                        28,
                        27
                );

                g.drawRoundRect(
                        10,
                        13,
                        18,
                        8,
                        4,
                        4
                );

                g.fillOval(
                        6,
                        13,
                        6,
                        6
                );
            }

            case ANALYTICS -> {

                g.fillRoundRect(
                        4,
                        18,
                        5,
                        10,
                        2,
                        2
                );

                g.fillRoundRect(
                        13,
                        11,
                        5,
                        17,
                        2,
                        2
                );

                g.fillRoundRect(
                        22,
                        4,
                        5,
                        24,
                        2,
                        2
                );
            }

            case CHAT -> {

                g.drawRoundRect(
                        3,
                        5,
                        26,
                        19,
                        8,
                        8
                );

                g.drawLine(
                        10,
                        24,
                        7,
                        29
                );

                g.fillOval(
                        9,
                        13,
                        3,
                        3
                );

                g.fillOval(
                        15,
                        13,
                        3,
                        3
                );

                g.fillOval(
                        21,
                        13,
                        3,
                        3
                );
            }

            case LOGOUT -> {

                g.drawRoundRect(
                        3,
                        4,
                        16,
                        24,
                        4,
                        4
                );

                g.drawLine(
                        13,
                        16,
                        29,
                        16
                );

                g.drawLine(
                        24,
                        11,
                        29,
                        16
                );

                g.drawLine(
                        24,
                        21,
                        29,
                        16
                );
            }

            case HOSPITAL -> {

                g.drawOval(
                        1,
                        1,
                        size - 3,
                        size - 3
                );

                g.fillRoundRect(
                        13,
                        6,
                        6,
                        20,
                        2,
                        2
                );

                g.fillRoundRect(
                        6,
                        13,
                        20,
                        6,
                        2,
                        2
                );
            }

            case DOCTOR -> {

                g.drawOval(
                        11,
                        3,
                        10,
                        10
                );

                g.drawArc(
                        6,
                        14,
                        20,
                        15,
                        0,
                        180
                );

                g.drawOval(
                        13,
                        20,
                        6,
                        6
                );
            }

            case NURSE -> {

                g.drawOval(
                        11,
                        8,
                        10,
                        10
                );

                g.drawLine(
                        8,
                        5,
                        24,
                        5
                );

                g.drawLine(
                        16,
                        1,
                        16,
                        9
                );

                g.drawArc(
                        6,
                        18,
                        20,
                        12,
                        0,
                        180
                );
            }
        }

        g.dispose();
    }
}
}