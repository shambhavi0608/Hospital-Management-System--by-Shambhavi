package hospital.management.system.ui;

import hospital.management.system.dao.StaffDAO;
import hospital.management.system.model.StaffMember;
import hospital.management.system.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin-only Staff Management screen.
 *
 * Features:
 * - Doctors aur nurses ki list
 * - Search staff
 * - Add doctor/nurse
 * - Staff ko ACTIVE/INACTIVE karna
 * - Database operations background thread par
 */
public final class StaffManagementPanel extends JPanel {

    private static final Color NAVY =
            new Color(6, 45, 91);

    private static final Color BLUE =
            new Color(23, 104, 218);

    private static final Color PAGE_BACKGROUND =
            new Color(245, 248, 252);

    private static final Color MUTED_TEXT =
            new Color(88, 104, 130);

    private static final Color BORDER_COLOR =
            new Color(220, 228, 238);

    private static final Color DANGER =
            new Color(210, 57, 66);

    private final StaffDAO staffDAO =
            new StaffDAO();

    private final Runnable dashboardRefreshAction;

    private final JTextField searchField =
            new JTextField();

    private final JLabel statusLabel =
            new JLabel("Loading staff...");

    private final JButton addStaffButton =
            new ModernButton("+  ADD STAFF");

    private final JButton activeButton =
            new ModernButton("MARK ACTIVE");

    private final JButton inactiveButton =
            new ModernButton("MARK INACTIVE");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                            "ID",
                            "Name",
                            "Role",
                            "Department",
                            "Specialization",
                            "Phone",
                            "Shift",
                            "Login",
                            "Status"
                    },
                    0
            ) {

                @Override
                public boolean isCellEditable(
                        int row,
                        int column
                ) {
                    return false;
                }
            };

    private final JTable staffTable =
            new JTable(tableModel);

    private List<StaffMember> displayedStaff =
            new ArrayList<>();

    public StaffManagementPanel() {

        this(null);
    }

    public StaffManagementPanel(
            Runnable dashboardRefreshAction
    ) {

        this.dashboardRefreshAction =
                dashboardRefreshAction;

        configurePanel();

        add(
                createHeaderSection(),
                BorderLayout.NORTH
        );

        add(
                createTableCard(),
                BorderLayout.CENTER
        );

        add(
                createStatusPanel(),
                BorderLayout.SOUTH
        );

        configureRoleAccess();

        refreshStaff();
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
                        30,
                        28,
                        30
                )
        );
    }

    private JComponent createHeaderSection() {

        JPanel wrapper =
                new JPanel(
                        new BorderLayout(
                                20,
                                18
                        )
                );

        wrapper.setOpaque(false);

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
                        "Staff Management"
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
                        "Manage doctors, nurses, departments and secure staff logins"
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

        subtitleLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        titlePanel.add(titleLabel);

        titlePanel.add(
                Box.createVerticalStrut(4)
        );

        titlePanel.add(subtitleLabel);

        stylePrimaryButton(
                addStaffButton
        );

        addStaffButton.addActionListener(
                event -> openAddStaffDialog()
        );

        wrapper.add(
                titlePanel,
                BorderLayout.WEST
        );

        wrapper.add(
                addStaffButton,
                BorderLayout.EAST
        );

        wrapper.add(
                createSearchSection(),
                BorderLayout.SOUTH
        );

        return wrapper;
    }

    private JPanel createSearchSection() {

        JPanel searchPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                0
                        )
                );

        searchPanel.setOpaque(false);

        searchField.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        searchField.setPreferredSize(
                new Dimension(
                        420,
                        43
                )
        );

        searchField.setToolTipText(
                "Search by name, role, department, specialization or login ID"
        );

        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        205,
                                        215,
                                        230
                                )
                        ),
                        new EmptyBorder(
                                0,
                                13,
                                0,
                                13
                        )
                )
        );

        JButton searchButton =
                new ModernButton("SEARCH");

        JButton refreshButton =
                new ModernButton("REFRESH");

        styleSecondaryButton(
                searchButton,
                BLUE
        );

        styleSecondaryButton(
                refreshButton,
                BLUE
        );

        searchButton.addActionListener(
                event -> loadStaff(
                        searchField
                                .getText()
                                .trim()
                )
        );

        refreshButton.addActionListener(
                event -> refreshStaff()
        );

        searchField.addActionListener(
                event -> loadStaff(
                        searchField
                                .getText()
                                .trim()
                )
        );

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                10,
                                0
                        )
                );

        buttonPanel.setOpaque(false);

        buttonPanel.add(searchButton);
        buttonPanel.add(refreshButton);

        searchPanel.add(
                searchField,
                BorderLayout.CENTER
        );

        searchPanel.add(
                buttonPanel,
                BorderLayout.EAST
        );

        return searchPanel;
    }

    private JComponent createTableCard() {

        JPanel cardPanel =
                new JPanel(
                        new BorderLayout()
                );

        cardPanel.setBackground(
                Color.WHITE
        );

        cardPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        new EmptyBorder(
                                12,
                                12,
                                12,
                                12
                        )
                )
        );

        configureTable();

        JScrollPane scrollPane =
                new JScrollPane(
                        staffTable
                );

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scrollPane.getViewport()
                .setBackground(Color.WHITE);

        cardPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        cardPanel.add(
                createTableActions(),
                BorderLayout.SOUTH
        );

        return cardPanel;
    }

    private void configureTable() {

        staffTable.setRowHeight(42);

        staffTable.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        staffTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        staffTable.setShowVerticalLines(false);

        staffTable.setShowHorizontalLines(true);

        staffTable.setGridColor(
                new Color(
                        226,
                        232,
                        240
                )
        );

        staffTable.setSelectionBackground(
                new Color(
                        219,
                        234,
                        254
                )
        );

        staffTable.setSelectionForeground(
                NAVY
        );

        staffTable.setFillsViewportHeight(
                true
        );

        staffTable.setAutoCreateRowSorter(
                true
        );

        staffTable.getTableHeader()
                .setFont(
                        new Font(
                                "Segoe UI",
                                Font.BOLD,
                                12
                        )
                );

        staffTable.getTableHeader()
                .setBackground(
                        new Color(
                                238,
                                244,
                                252
                        )
                );

        staffTable.getTableHeader()
                .setForeground(NAVY);

        staffTable.getTableHeader()
                .setPreferredSize(
                        new Dimension(
                                0,
                                42
                        )
                );

        staffTable.getTableHeader()
                .setReorderingAllowed(false);

        staffTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(45);

        staffTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(145);

        staffTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(80);

        staffTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(120);

        staffTable.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(135);

        staffTable.getColumnModel()
                .getColumn(5)
                .setPreferredWidth(100);

        staffTable.getColumnModel()
                .getColumn(6)
                .setPreferredWidth(85);

        staffTable.getColumnModel()
                .getColumn(7)
                .setPreferredWidth(105);

        staffTable.getColumnModel()
                .getColumn(8)
                .setPreferredWidth(80);
    }

    private JPanel createTableActions() {

        JPanel actionsPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                10,
                                10
                        )
                );

        actionsPanel.setOpaque(false);

        styleSecondaryButton(
                activeButton,
                BLUE
        );

        styleSecondaryButton(
                inactiveButton,
                DANGER
        );

        activeButton.addActionListener(
                event -> changeSelectedStaffStatus(
                        "ACTIVE"
                )
        );

        inactiveButton.addActionListener(
                event -> changeSelectedStaffStatus(
                        "INACTIVE"
                )
        );

        actionsPanel.add(activeButton);
        actionsPanel.add(inactiveButton);

        return actionsPanel;
    }

    private JPanel createStatusPanel() {

        JPanel statusPanel =
                new JPanel(
                        new BorderLayout()
                );

        statusPanel.setOpaque(false);

        statusLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        statusLabel.setForeground(
                MUTED_TEXT
        );

        statusPanel.add(
                statusLabel,
                BorderLayout.WEST
        );

        return statusPanel;
    }

    private void configureRoleAccess() {

        boolean isAdmin =
                SessionManager
                        .isCurrentUserAdmin();

        addStaffButton.setVisible(
                isAdmin
        );

        activeButton.setVisible(
                isAdmin
        );

        inactiveButton.setVisible(
                isAdmin
        );
    }

    private void openAddStaffDialog() {

        if (
                !SessionManager
                        .isCurrentUserAdmin()
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Only administrators can add staff members.",
                    "Access Restricted",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Window owner =
                SwingUtilities
                        .getWindowAncestor(this);

        AddStaffDialog dialog =
                new AddStaffDialog(
                        owner,
                        staffDAO,
                        this::afterStaffChange
                );

        dialog.setVisible(true);
    }

    public void refreshStaff() {

        searchField.setText("");

        loadStaff("");
    }

    private void afterStaffChange() {

        refreshStaff();

        if (dashboardRefreshAction != null) {
            dashboardRefreshAction.run();
        }
    }

    private void loadStaff(
            String keyword
    ) {

        statusLabel.setText(
                "Loading staff records..."
        );

        setControlsEnabled(false);

        SwingWorker<List<StaffMember>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<StaffMember> doInBackground()
                            throws Exception {

                        return staffDAO.findAll(
                                keyword
                        );
                    }

                    @Override
                    protected void done() {

                        setControlsEnabled(true);

                        try {
                            displayedStaff = get();

                            tableModel.setRowCount(0);

                            for (
                                    StaffMember staffMember
                                    : displayedStaff
                            ) {

                                tableModel.addRow(
                                        new Object[]{
                                                staffMember.getStaffId(),
                                                staffMember.getName(),
                                                staffMember.getRole(),
                                                staffMember.getDepartment(),
                                                staffMember.getSpecialization(),
                                                staffMember.getPhone(),
                                                staffMember.getShift(),
                                                staffMember.getLoginId(),
                                                staffMember.getStatus()
                                        }
                                );
                            }

                            statusLabel.setText(
                                    displayedStaff.size()
                                            + " staff record(s)"
                            );

                        } catch (Exception exception) {

                            displayedStaff =
                                    new ArrayList<>();

                            tableModel.setRowCount(0);

                            statusLabel.setText(
                                    "Staff records could not be loaded."
                            );

                            JOptionPane.showMessageDialog(
                                    StaffManagementPanel.this,
                                    "Staff data load failed:\n"
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

    private void changeSelectedStaffStatus(
            String newStatus
    ) {

        if (
                !SessionManager
                        .isCurrentUserAdmin()
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Only administrators can change staff status.",
                    "Access Restricted",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int selectedViewRow =
                staffTable.getSelectedRow();

        if (selectedViewRow < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select one staff member first.",
                    "No Staff Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int selectedModelRow =
                staffTable.convertRowIndexToModel(
                        selectedViewRow
                );

        int staffId =
                ((Number) tableModel.getValueAt(
                        selectedModelRow,
                        0
                )).intValue();

        String staffName =
                String.valueOf(
                        tableModel.getValueAt(
                                selectedModelRow,
                                1
                        )
                );

        int confirmation =
                JOptionPane.showConfirmDialog(
                        this,
                        "Mark "
                                + staffName
                                + " as "
                                + newStatus
                                + "?",
                        "Confirm Status Change",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (
                confirmation
                        != JOptionPane.YES_OPTION
        ) {
            return;
        }

        setControlsEnabled(false);

        statusLabel.setText(
                "Updating staff status..."
        );

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Boolean doInBackground()
                            throws Exception {

                        return staffDAO.setStatus(
                                staffId,
                                newStatus
                        );
                    }

                    @Override
                    protected void done() {

                        setControlsEnabled(true);

                        try {
                            boolean updated = get();

                            if (updated) {

                                JOptionPane.showMessageDialog(
                                        StaffManagementPanel.this,
                                        staffName
                                                + " marked as "
                                                + newStatus
                                                + ".",
                                        "Status Updated",
                                        JOptionPane.INFORMATION_MESSAGE
                                );

                                loadStaff(
                                        searchField
                                                .getText()
                                                .trim()
                                );

                                if (dashboardRefreshAction != null) {
                                    dashboardRefreshAction.run();
                                }

                            } else {

                                JOptionPane.showMessageDialog(
                                        StaffManagementPanel.this,
                                        "Staff record was not found.",
                                        "Update Failed",
                                        JOptionPane.WARNING_MESSAGE
                                );
                            }

                        } catch (Exception exception) {

                            statusLabel.setText(
                                    "Status update failed."
                            );

                            JOptionPane.showMessageDialog(
                                    StaffManagementPanel.this,
                                    "Could not update staff status:\n"
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

    private void setControlsEnabled(
            boolean enabled
    ) {

        searchField.setEnabled(enabled);

        boolean admin =
                SessionManager
                        .isCurrentUserAdmin();

        addStaffButton.setEnabled(
                enabled && admin
        );

        activeButton.setEnabled(
                enabled && admin
        );

        inactiveButton.setEnabled(
                enabled && admin
        );
    }

    private void stylePrimaryButton(
            JButton button
    ) {

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
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
                        11,
                        18,
                        11,
                        18
                )
        );
    }

    private void styleSecondaryButton(
            JButton button,
            Color foreground
    ) {

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        button.setBackground(Color.WHITE);
        button.setForeground(foreground);
        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                foreground
                        ),
                        new EmptyBorder(
                                10,
                                16,
                                10,
                                16
                        )
                )
        );
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
