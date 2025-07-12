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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Smart HMS ka final role-based dashboard.
 *
 * ADMIN:
 * Dashboard, Patients, Staff, Rooms, Analytics, Assistant
 *
 * DOCTOR:
 * Dashboard, Patients, Rooms, Analytics, Assistant
 *
 * NURSE:
 * Dashboard, Patients, Rooms, Assistant
 *
 * PATIENT:
 * Home, Profile, Admission, My Room, Assistant
 */
public final class MainDashboard extends JFrame {

    private static final Color NAVY =
            new Color(5, 39, 86);

    private static final Color NAVY_HOVER =
            new Color(8, 64, 120);

    private static final Color BLUE =
            new Color(20, 101, 219);

    private static final Color TEAL =
            new Color(12, 174, 184);

    private static final Color ORANGE =
            new Color(255, 139, 13);

    private static final Color PURPLE =
            new Color(105, 82, 235);

    private static final Color RED =
            new Color(244, 75, 85);

    private static final Color GREEN =
            new Color(21, 184, 128);

    private static final Color PAGE_BACKGROUND =
            new Color(245, 248, 252);

    private static final Color PRIMARY_TEXT =
            new Color(12, 38, 78);

    private static final Color MUTED_TEXT =
            new Color(83, 101, 129);

    private static final String DASHBOARD =
            "DASHBOARD";

    private static final String PATIENTS =
            "PATIENTS";

    private static final String STAFF =
            "STAFF";

    private static final String ROOMS =
            "ROOMS";

    private static final String ANALYTICS =
            "ANALYTICS";

    private static final String ASSISTANT =
            "ASSISTANT";

    private static final String PROFILE =
            "PROFILE";

    private static final String ADMISSION =
            "ADMISSION";

    private static final String MY_ROOM =
            "MY_ROOM";

    private final DashboardDAO dashboardDAO =
            new DashboardDAO();

    private final LoggedInUser currentUser =
            SessionManager
                    .getCurrentUser()
                    .orElse(null);

    private final CardLayout cardLayout =
            new CardLayout();

    private final JPanel pageContainer =
            new JPanel(cardLayout);

    private final Map<String, NavigationButton>
            navigationButtons =
            new LinkedHashMap<>();

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

    private String currentPage =
            DASHBOARD;

    public MainDashboard() {

        if (currentUser == null) {

            SwingUtilities.invokeLater(
                    () -> Login.main(
                            new String[0]
                    )
            );

            dispose();

            return;
        }

        configureWindow();

        registerPages();

        setLayout(
                new BorderLayout()
        );

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

        setSize(
                1500,
                900
        );

        setMinimumSize(
                new Dimension(
                        1180,
                        720
                )
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

            if (
                    isAdmin()
                            ||
                            isDoctor()
            ) {

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

    private JPanel createSidebar() {

        JPanel sidebar =
                new JPanel();

        sidebar.setLayout(
                new BoxLayout(
                        sidebar,
                        BoxLayout.Y_AXIS
                )
        );

        sidebar.setBackground(NAVY);

        sidebar.setPreferredSize(
                new Dimension(
                        285,
                        0
                )
        );

        sidebar.setBorder(
                new EmptyBorder(
                        28,
                        14,
                        22,
                        14
                )
        );

        sidebar.add(
                createBrandPanel()
        );

        sidebar.add(
                Box.createVerticalStrut(28)
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

            if (
                    isAdmin()
                            ||
                            isDoctor()
            ) {

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

        sidebar.add(
                createUserCard()
        );

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
                        70
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

        JPanel brandTextPanel =
                new JPanel();

        brandTextPanel.setOpaque(false);

        brandTextPanel.setLayout(
                new BoxLayout(
                        brandTextPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel brandNameLabel =
                new JLabel(
                        "SMART HMS"
                );

        brandNameLabel.setForeground(
                Color.WHITE
        );

        brandNameLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24
                )
        );

        JLabel portalLabel =
                new JLabel(
                        "Operations Portal"
                );

        portalLabel.setForeground(
                new Color(
                        174,
                        207,
                        238
                )
        );

        portalLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        brandTextPanel.add(
                brandNameLabel
        );

        brandTextPanel.add(
                Box.createVerticalStrut(3)
        );

        brandTextPanel.add(
                portalLabel
        );

        brandPanel.add(hospitalIcon);
        brandPanel.add(brandTextPanel);

        return brandPanel;
    }

    private void addNavigationButton(
            JPanel sidebar,
            String pageName,
            String buttonText,
            IconType iconType
    ) {

        NavigationButton button =
                new NavigationButton(
                        buttonText,
                        iconType
                );

        button.addActionListener(
                event -> {

                    if (
                            PATIENTS.equals(
                                    pageName
                            )
                                    &&
                                    patientPanel != null
                    ) {

                        patientPanel
                                .refreshPatients();
                    }

                    if (
                            STAFF.equals(
                                    pageName
                            )
                                    &&
                                    staffPanel != null
                    ) {

                        staffPanel
                                .refreshStaff();
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

        RoundedPanel userCard =
                new RoundedPanel(
                        new Color(
                                8,
                                54,
                                108
                        ),
                        18
                );

        userCard.setLayout(
                new BorderLayout(
                        12,
                        0
                )
        );

        userCard.setBorder(
                new EmptyBorder(
                        14,
                        14,
                        14,
                        14
                )
        );

        userCard.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        82
                )
        );

        userCard.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel iconLabel =
                new JLabel(
                        new VectorIcon(
                                IconType.USER,
                                45,
                                Color.WHITE
                        )
                );

        JPanel userTextPanel =
                new JPanel();

        userTextPanel.setOpaque(false);

        userTextPanel.setLayout(
                new BoxLayout(
                        userTextPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel usernameLabel =
                new JLabel(
                        displayUsername()
                );

        usernameLabel.setForeground(
                Color.WHITE
        );

        usernameLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        JLabel roleLabel =
                new JLabel(
                        displayRole()
                );

        roleLabel.setForeground(
                new Color(
                        77,
                        232,
                        224
                )
        );

        roleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        userTextPanel.add(
                usernameLabel
        );

        userTextPanel.add(
                Box.createVerticalStrut(5)
        );

        userTextPanel.add(
                roleLabel
        );

        userCard.add(
                iconLabel,
                BorderLayout.WEST
        );

        userCard.add(
                userTextPanel,
                BorderLayout.CENTER
        );

        return userCard;
    }

    private JPanel createDashboardPage() {

        JPanel page =
                new JPanel(
                        new BorderLayout()
                );

        page.setBackground(
                PAGE_BACKGROUND
        );

        JPanel dashboardContent =
                new JPanel();

        dashboardContent.setBackground(
                PAGE_BACKGROUND
        );

        dashboardContent.setLayout(
                new BoxLayout(
                        dashboardContent,
                        BoxLayout.Y_AXIS
                )
        );

        dashboardContent.setBorder(
                new EmptyBorder(
                        30,
                        34,
                        30,
                        34
                )
        );

        dashboardContent.add(
                createDashboardHeader()
        );

        dashboardContent.add(
                Box.createVerticalStrut(24)
        );

        dashboardContent.add(
                createMetricCards()
        );

        dashboardContent.add(
                Box.createVerticalStrut(24)
        );

        dashboardContent.add(
                createOverviewSection()
        );

        dashboardContent.add(
                Box.createVerticalStrut(24)
        );

        dashboardContent.add(
                createQuickActionsSection()
        );

        dashboardContent.add(
                Box.createVerticalGlue()
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        dashboardContent
                );

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

        JPanel headerPanel =
                new JPanel(
                        new BorderLayout(
                                20,
                                0
                        )
                );

        headerPanel.setOpaque(false);

        headerPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        90
                )
        );

        headerPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JPanel greetingPanel =
                new JPanel();

        greetingPanel.setOpaque(false);

        greetingPanel.setLayout(
                new BoxLayout(
                        greetingPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel welcomeLabel =
                new JLabel(
                        "Welcome, "
                                + displayUsername()
                );

        welcomeLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        30
                )
        );

        welcomeLabel.setForeground(
                PRIMARY_TEXT
        );

        JLabel subtitleLabel =
                new JLabel(
                        "Here is your live hospital overview"
                );

        subtitleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        subtitleLabel.setForeground(
                MUTED_TEXT
        );

        greetingPanel.add(
                welcomeLabel
        );

        greetingPanel.add(
                Box.createVerticalStrut(5)
        );

        greetingPanel.add(
                subtitleLabel
        );

        JPanel userArea =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                12,
                                6
                        )
                );

        userArea.setOpaque(false);

        JLabel userIcon =
                new JLabel(
                        new VectorIcon(
                                IconType.USER,
                                38,
                                BLUE
                        )
                );

        JPanel identityPanel =
                new JPanel();

        identityPanel.setOpaque(false);

        identityPanel.setLayout(
                new BoxLayout(
                        identityPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel nameLabel =
                new JLabel(
                        displayUsername()
                );

        nameLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        nameLabel.setForeground(
                PRIMARY_TEXT
        );

        JLabel roleLabel =
                new JLabel(
                        displayRole()
                );

        roleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        roleLabel.setForeground(
                MUTED_TEXT
        );

        identityPanel.add(nameLabel);
        identityPanel.add(roleLabel);

        JButton refreshButton =
                createActionButton(
                        "REFRESH",
                        BLUE
                );

        refreshButton.addActionListener(
                event ->
                        refreshDashboardData()
        );

        userArea.add(userIcon);
        userArea.add(identityPanel);
        userArea.add(refreshButton);

        headerPanel.add(
                greetingPanel,
                BorderLayout.WEST
        );

        headerPanel.add(
                userArea,
                BorderLayout.EAST
        );

        return headerPanel;
    }

    private JPanel createMetricCards() {

        JPanel cardsPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                5,
                                16,
                                0
                        )
                );

        cardsPanel.setOpaque(false);

        cardsPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        160
                )
        );

        cardsPanel.setPreferredSize(
                new Dimension(
                        1000,
                        160
                )
        );

        cardsPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT
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

        cardsPanel.add(patientsCard);
        cardsPanel.add(availableRoomsCard);
        cardsPanel.add(occupiedRoomsCard);
        cardsPanel.add(doctorsCard);
        cardsPanel.add(nursesCard);

        return cardsPanel;
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

        overviewPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        overviewPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        300
                )
        );

        overviewPanel.setPreferredSize(
                new Dimension(
                        1000,
                        300
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

        JPanel statusPanel =
                new JPanel();

        statusPanel.setOpaque(false);

        statusPanel.setLayout(
                new BoxLayout(
                        statusPanel,
                        BoxLayout.Y_AXIS
                )
        );

        databaseStatusLabel =
                createStatusLabel(
                        "Checking database connection...",
                        ORANGE
                );

        JLabel refreshStatusLabel =
                createStatusLabel(
                        "Auto-refresh: Every 10 seconds",
                        GREEN
                );

        lastUpdatedLabel =
                createStatusLabel(
                        "Last updated: Waiting",
                        MUTED_TEXT
                );

        JLabel sessionStatusLabel =
                createStatusLabel(
                        "Session role: "
                                + displayRole(),
                        BLUE
                );

        statusPanel.add(
                databaseStatusLabel
        );

        statusPanel.add(
                Box.createVerticalStrut(17)
        );

        statusPanel.add(
                refreshStatusLabel
        );

        statusPanel.add(
                Box.createVerticalStrut(17)
        );

        statusPanel.add(
                lastUpdatedLabel
        );

        statusPanel.add(
                Box.createVerticalStrut(17)
        );

        statusPanel.add(
                sessionStatusLabel
        );

        card.add(
                statusPanel,
                BorderLayout.CENTER
        );

        return card;
    }

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

        card.setPreferredSize(
                new Dimension(
                        1000,
                        180
                )
        );

        card.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        card.add(
                createSectionTitle(
                        "Quick Actions"
                ),
                BorderLayout.NORTH
        );

        JPanel actionsPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                isAdmin() ? 5 : 4,
                                14,
                                0
                        )
                );

        actionsPanel.setOpaque(false);

        actionsPanel.add(
                createQuickButton(
                        "Patients",
                        IconType.PATIENT,
                        BLUE,
                        () -> showPage(
                                PATIENTS
                        )
                )
        );

        if (isAdmin()) {

            actionsPanel.add(
                    createQuickButton(
                            "Staff",
                            IconType.STAFF,
                            PURPLE,
                            () -> showPage(
                                    STAFF
                            )
                    )
            );
        }

        actionsPanel.add(
                createQuickButton(
                        "Find Room",
                        IconType.ROOM,
                        ORANGE,
                        () -> showPage(
                                ROOMS
                        )
                )
        );

        if (
                isAdmin()
                        ||
                        isDoctor()
        ) {

            actionsPanel.add(
                    createQuickButton(
                            "Analytics",
                            IconType.ANALYTICS,
                            new Color(
                                    92,
                                    79,
                                    210
                            ),
                            () -> showPage(
                                    ANALYTICS
                            )
                    )
            );
        }

        actionsPanel.add(
                createQuickButton(
                        "Assistant",
                        IconType.CHAT,
                        TEAL,
                        () -> showPage(
                                ASSISTANT
                        )
                )
        );

        card.add(
                actionsPanel,
                BorderLayout.CENTER
        );

        return card;
    }

    private JButton createQuickButton(
            String text,
            IconType iconType,
            Color background,
            Runnable action
    ) {

        JButton button =
                new JButton(
                        text,
                        new VectorIcon(
                                iconType,
                                30,
                                Color.WHITE
                        )
                );

        button.setVerticalTextPosition(
                SwingConstants.BOTTOM
        );

        button.setHorizontalTextPosition(
                SwingConstants.CENTER
        );

        button.setIconTextGap(8);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        button.setForeground(
                Color.WHITE
        );

        button.setBackground(
                background
        );

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                new EmptyBorder(
                        12,
                        8,
                        12,
                        8
                )
        );

        button.addActionListener(
                event -> action.run()
        );

        return button;
    }

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

        label.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        return label;
    }

    private JButton createActionButton(
            String text,
            Color color
    ) {

        JButton button =
                new JButton(text);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                new EmptyBorder(
                        11,
                        18,
                        11,
                        18
                )
        );

        return button;
    }

    private void showPage(
            String pageName
    ) {

        if (
                ANALYTICS.equals(pageName)
                        &&
                        !(isAdmin() || isDoctor())
        ) {

            showAccessRestricted();

            return;
        }

        if (
                STAFF.equals(pageName)
                        &&
                        !isAdmin()
        ) {

            showAccessRestricted();

            return;
        }

        if (
                PATIENTS.equals(pageName)
                        &&
                        isPatient()
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

            entry.getValue()
                    .setActive(
                            entry.getKey()
                                    .equals(
                                            pageName
                                    )
                    );
        }

        if (
                DASHBOARD.equals(pageName)
                        &&
                        !isPatient()
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

    private void refreshDashboardData() {

        if (
                isPatient()
                        ||
                        patientsCard == null
        ) {
            return;
        }

        setMetricsText("...");

        databaseStatusLabel.setText(
                "Loading live database data..."
        );

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

                            int[] result =
                                    get();

                            patientsCard.setValue(
                                    result[0]
                            );

                            availableRoomsCard.setValue(
                                    result[1]
                            );

                            occupiedRoomsCard.setValue(
                                    result[2]
                            );

                            doctorsCard.setValue(
                                    result[3]
                            );

                            nursesCard.setValue(
                                    result[4]
                            );

                            databaseStatusLabel.setText(
                                    "Database connected successfully"
                            );

                            lastUpdatedLabel.setText(
                                    "Last updated: "
                                            + java.time.LocalTime
                                            .now()
                                            .format(
                                                    java.time.format
                                                            .DateTimeFormatter
                                                            .ofPattern(
                                                                    "hh:mm:ss a"
                                                            )
                                            )
                            );

                            repaint();

                        } catch (Exception exception) {

                            setMetricsText(
                                    "Error"
                            );

                            databaseStatusLabel.setText(
                                    "Database connection failed"
                            );

                            JOptionPane.showMessageDialog(
                                    MainDashboard.this,
                                    "Dashboard data could not be loaded:\n"
                                            + rootMessage(
                                            exception
                                    ),
                                    "Database Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                };

        worker.execute();
    }

    private void setMetricsText(
            String text
    ) {

        patientsCard.setText(text);
        availableRoomsCard.setText(text);
        occupiedRoomsCard.setText(text);
        doctorsCard.setText(text);
        nursesCard.setText(text);
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
                &&
                currentUser.getRole()
                        == UserRole.ADMIN;
    }

    private boolean isDoctor() {

        return currentUser != null
                &&
                currentUser.getRole()
                        == UserRole.DOCTOR;
    }

    private boolean isPatient() {

        return currentUser != null
                &&
                currentUser.getRole()
                        == UserRole.PATIENT;
    }

    private String rootMessage(
            Throwable throwable
    ) {

        Throwable current = throwable;

        while (
                current.getCause()
                        != null
        ) {
            current = current.getCause();
        }

        return current.getMessage() == null
                ? "Unknown database error"
                : current.getMessage();
    }

    public static void main(
            String[] arguments
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

            setForeground(
                    Color.WHITE
            );

            setBackground(NAVY);

            setOpaque(true);

            setBorderPainted(false);

            setFocusPainted(false);

            setBorder(
                    new EmptyBorder(
                            13,
                            18,
                            13,
                            18
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

                                setBackground(
                                        NAVY
                                );
                            }
                        }
                    }
            );
        }

        void setActive(
                boolean active
        ) {

            this.active = active;

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

    private static final class MetricCard
            extends RoundedPanel {

        private final JLabel valueLabel =
                new JLabel("...");

        MetricCard(
                String title,
                Color accentColor,
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

            JLabel iconLabel =
                    new JLabel(
                            new VectorIcon(
                                    iconType,
                                    44,
                                    accentColor
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

            valueLabel.setForeground(
                    accentColor
            );

            JLabel liveLabel =
                    new JLabel(
                            "Live count"
                    );

            liveLabel.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            11
                    )
            );

            liveLabel.setForeground(
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

            textPanel.add(liveLabel);

            add(
                    iconLabel,
                    BorderLayout.WEST
            );

            add(
                    textPanel,
                    BorderLayout.CENTER
            );
        }

        void setValue(
                int value
        ) {

            valueLabel.setText(
                    String.valueOf(value)
            );
        }

        void setText(
                String text
        ) {

            valueLabel.setText(text);
        }
    }

    private final class OccupancyInformationPanel
            extends JPanel {

        OccupancyInformationPanel() {

            setOpaque(false);
        }

        @Override
        protected void paintComponent(
                Graphics graphics
        ) {

            super.paintComponent(graphics);

            Graphics2D graphics2D =
                    (Graphics2D)
                            graphics.create();

            graphics2D.setRenderingHint(
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

            double occupiedPercentage =
                    total == 0
                            ? 0
                            : occupied
                            * 100.0
                            / total;

            int diameter =
                    Math.min(
                            175,
                            Math.min(
                                    getWidth() / 2,
                                    getHeight() - 35
                            )
                    );

            int x = 30;

            int y =
                    Math.max(
                            15,
                            (
                                    getHeight()
                                            - diameter
                            ) / 2
                    );

            graphics2D.setStroke(
                    new BasicStroke(
                            30,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND
                    )
            );

            graphics2D.setColor(TEAL);

            graphics2D.draw(
                    new Arc2D.Double(
                            x,
                            y,
                            diameter,
                            diameter,
                            90,
                            360,
                            Arc2D.OPEN
                    )
            );

            graphics2D.setColor(ORANGE);

            graphics2D.draw(
                    new Arc2D.Double(
                            x,
                            y,
                            diameter,
                            diameter,
                            90,
                            -occupiedPercentage
                                    * 3.6,
                            Arc2D.OPEN
                    )
            );

            graphics2D.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            25
                    )
            );

            graphics2D.setColor(
                    PRIMARY_TEXT
            );

            String percentageText =
                    Math.round(
                            occupiedPercentage
                    )
                            + "%";

            FontMetrics metrics =
                    graphics2D
                            .getFontMetrics();

            graphics2D.drawString(
                    percentageText,
                    x
                            + (
                            diameter
                                    - metrics.stringWidth(
                                    percentageText
                            )
                    ) / 2,
                    y
                            + diameter / 2
            );

            graphics2D.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            12
                    )
            );

            graphics2D.setColor(
                    MUTED_TEXT
            );

            graphics2D.drawString(
                    "Occupied",
                    x
                            + diameter / 2
                            - 25,
                    y
                            + diameter / 2
                            + 23
            );

            int textX =
                    x
                            + diameter
                            + 55;

            int textY =
                    y + 65;

            drawLegend(
                    graphics2D,
                    textX,
                    textY,
                    TEAL,
                    "Available Rooms",
                    available
            );

            drawLegend(
                    graphics2D,
                    textX,
                    textY + 55,
                    ORANGE,
                    "Occupied Rooms",
                    occupied
            );

            graphics2D.dispose();
        }

        private int getMetricValue(
                MetricCard card
        ) {

            try {

                return Integer.parseInt(
                        card.valueLabel
                                .getText()
                );

            } catch (
                    NumberFormatException exception
            ) {

                return 0;
            }
        }

        private void drawLegend(
                Graphics2D graphics,
                int x,
                int y,
                Color color,
                String label,
                int value
        ) {

            graphics.setColor(color);

            graphics.fillRoundRect(
                    x,
                    y - 13,
                    16,
                    16,
                    5,
                    5
            );

            graphics.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            13
                    )
            );

            graphics.setColor(
                    MUTED_TEXT
            );

            graphics.drawString(
                    label,
                    x + 28,
                    y
            );

            graphics.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            14
                    )
            );

            graphics.setColor(
                    PRIMARY_TEXT
            );

            graphics.drawString(
                    String.valueOf(value),
                    x + 170,
                    y
            );
        }
    }

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

            Graphics2D graphics2D =
                    (Graphics2D)
                            graphics.create();

            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics2D.setColor(
                    new Color(
                            24,
                            45,
                            74,
                            18
                    )
            );

            graphics2D.fillRoundRect(
                    2,
                    4,
                    getWidth() - 4,
                    getHeight() - 6,
                    radius,
                    radius
            );

            graphics2D.setColor(
                    fillColor
            );

            graphics2D.fillRoundRect(
                    0,
                    0,
                    getWidth() - 3,
                    getHeight() - 6,
                    radius,
                    radius
            );

            graphics2D.dispose();

            super.paintComponent(graphics);
        }
    }

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

    /**
     * Vector icons use hue hain.
     * Isliye emoji wale square boxes show nahi honge.
     */
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

            g.setColor(color);

            float scale =
                    size / 32.0f;

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
                            Math.round(4 * scale),
                            Math.round(15 * scale),
                            Math.round(16 * scale),
                            Math.round(4 * scale)
                    );

                    g.drawLine(
                            Math.round(16 * scale),
                            Math.round(4 * scale),
                            Math.round(28 * scale),
                            Math.round(15 * scale)
                    );

                    g.drawRect(
                            Math.round(7 * scale),
                            Math.round(14 * scale),
                            Math.round(18 * scale),
                            Math.round(14 * scale)
                    );

                    g.drawRect(
                            Math.round(14 * scale),
                            Math.round(20 * scale),
                            Math.round(5 * scale),
                            Math.round(8 * scale)
                    );
                }

                case USER, PATIENT -> {

                    g.drawOval(
                            Math.round(11 * scale),
                            Math.round(3 * scale),
                            Math.round(10 * scale),
                            Math.round(10 * scale)
                    );

                    g.drawArc(
                            Math.round(5 * scale),
                            Math.round(14 * scale),
                            Math.round(22 * scale),
                            Math.round(15 * scale),
                            0,
                            180
                    );
                }

                case STAFF -> {

                    g.drawOval(
                            Math.round(12 * scale),
                            Math.round(2 * scale),
                            Math.round(8 * scale),
                            Math.round(8 * scale)
                    );

                    g.drawOval(
                            Math.round(3 * scale),
                            Math.round(7 * scale),
                            Math.round(7 * scale),
                            Math.round(7 * scale)
                    );

                    g.drawOval(
                            Math.round(22 * scale),
                            Math.round(7 * scale),
                            Math.round(7 * scale),
                            Math.round(7 * scale)
                    );

                    g.drawArc(
                            Math.round(7 * scale),
                            Math.round(12 * scale),
                            Math.round(18 * scale),
                            Math.round(15 * scale),
                            0,
                            180
                    );

                    g.drawArc(
                            0,
                            Math.round(16 * scale),
                            Math.round(13 * scale),
                            Math.round(11 * scale),
                            0,
                            180
                    );

                    g.drawArc(
                            Math.round(19 * scale),
                            Math.round(16 * scale),
                            Math.round(13 * scale),
                            Math.round(11 * scale),
                            0,
                            180
                    );
                }

                case ROOM, BED -> {

                    g.drawLine(
                            Math.round(4 * scale),
                            Math.round(6 * scale),
                            Math.round(4 * scale),
                            Math.round(27 * scale)
                    );

                    g.drawLine(
                            Math.round(4 * scale),
                            Math.round(21 * scale),
                            Math.round(28 * scale),
                            Math.round(21 * scale)
                    );

                    g.drawLine(
                            Math.round(28 * scale),
                            Math.round(15 * scale),
                            Math.round(28 * scale),
                            Math.round(27 * scale)
                    );

                    g.drawRoundRect(
                            Math.round(10 * scale),
                            Math.round(13 * scale),
                            Math.round(18 * scale),
                            Math.round(8 * scale),
                            4,
                            4
                    );

                    g.fillOval(
                            Math.round(6 * scale),
                            Math.round(13 * scale),
                            Math.round(6 * scale),
                            Math.round(6 * scale)
                    );
                }

                case ANALYTICS -> {

                    g.fillRoundRect(
                            Math.round(4 * scale),
                            Math.round(18 * scale),
                            Math.round(5 * scale),
                            Math.round(10 * scale),
                            2,
                            2
                    );

                    g.fillRoundRect(
                            Math.round(13 * scale),
                            Math.round(11 * scale),
                            Math.round(5 * scale),
                            Math.round(17 * scale),
                            2,
                            2
                    );

                    g.fillRoundRect(
                            Math.round(22 * scale),
                            Math.round(4 * scale),
                            Math.round(5 * scale),
                            Math.round(24 * scale),
                            2,
                            2
                    );
                }

                case CHAT -> {

                    g.drawRoundRect(
                            Math.round(3 * scale),
                            Math.round(5 * scale),
                            Math.round(26 * scale),
                            Math.round(19 * scale),
                            Math.round(8 * scale),
                            Math.round(8 * scale)
                    );

                    g.drawLine(
                            Math.round(10 * scale),
                            Math.round(24 * scale),
                            Math.round(7 * scale),
                            Math.round(29 * scale)
                    );

                    g.fillOval(
                            Math.round(9 * scale),
                            Math.round(13 * scale),
                            Math.round(3 * scale),
                            Math.round(3 * scale)
                    );

                    g.fillOval(
                            Math.round(15 * scale),
                            Math.round(13 * scale),
                            Math.round(3 * scale),
                            Math.round(3 * scale)
                    );

                    g.fillOval(
                            Math.round(21 * scale),
                            Math.round(13 * scale),
                            Math.round(3 * scale),
                            Math.round(3 * scale)
                    );
                }

                case LOGOUT -> {

                    g.drawRoundRect(
                            Math.round(3 * scale),
                            Math.round(4 * scale),
                            Math.round(16 * scale),
                            Math.round(24 * scale),
                            4,
                            4
                    );

                    g.drawLine(
                            Math.round(13 * scale),
                            Math.round(16 * scale),
                            Math.round(29 * scale),
                            Math.round(16 * scale)
                    );

                    g.drawLine(
                            Math.round(24 * scale),
                            Math.round(11 * scale),
                            Math.round(29 * scale),
                            Math.round(16 * scale)
                    );

                    g.drawLine(
                            Math.round(24 * scale),
                            Math.round(21 * scale),
                            Math.round(29 * scale),
                            Math.round(16 * scale)
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
                            Math.round(13 * scale),
                            Math.round(6 * scale),
                            Math.round(6 * scale),
                            Math.round(20 * scale),
                            2,
                            2
                    );

                    g.fillRoundRect(
                            Math.round(6 * scale),
                            Math.round(13 * scale),
                            Math.round(20 * scale),
                            Math.round(6 * scale),
                            2,
                            2
                    );
                }

                case DOCTOR -> {

                    g.drawOval(
                            Math.round(11 * scale),
                            Math.round(3 * scale),
                            Math.round(10 * scale),
                            Math.round(10 * scale)
                    );

                    g.drawArc(
                            Math.round(6 * scale),
                            Math.round(14 * scale),
                            Math.round(20 * scale),
                            Math.round(15 * scale),
                            0,
                            180
                    );

                    g.drawOval(
                            Math.round(13 * scale),
                            Math.round(20 * scale),
                            Math.round(6 * scale),
                            Math.round(6 * scale)
                    );
                }

                case NURSE -> {

                    g.drawOval(
                            Math.round(11 * scale),
                            Math.round(8 * scale),
                            Math.round(10 * scale),
                            Math.round(10 * scale)
                    );

                    g.drawLine(
                            Math.round(8 * scale),
                            Math.round(5 * scale),
                            Math.round(24 * scale),
                            Math.round(5 * scale)
                    );

                    g.drawLine(
                            Math.round(16 * scale),
                            Math.round(1 * scale),
                            Math.round(16 * scale),
                            Math.round(9 * scale)
                    );

                    g.drawArc(
                            Math.round(6 * scale),
                            Math.round(18 * scale),
                            Math.round(20 * scale),
                            Math.round(12 * scale),
                            0,
                            180
                    );
                }
            }

            g.dispose();
        }
    }
}