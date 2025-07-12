    package hospital.management.system.ui;

    import hospital.management.system.model.Patient;
    import hospital.management.system.service.PatientService;

    import javax.swing.*;
    import javax.swing.border.EmptyBorder;
    import javax.swing.table.DefaultTableModel;
    import java.awt.*;
    import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.List;

    /*
     * Modern Patient Management screen.
     */
    public class PatientPanel extends JPanel {

        private final PatientService patientService;
        private final Runnable dashboardRefreshAction;

        private final JTextField searchField;
        private final JButton refreshButton;
        private final JButton addButton;
        private final JButton updateButton;
        private final JButton dischargeButton;

        private final DefaultTableModel tableModel;
        private final JTable patientTable;
        private final JLabel statusLabel;

        private List<Patient> displayedPatients;

        public PatientPanel(
                Runnable dashboardRefreshAction
        ) {
            patientService = new PatientService();

            this.dashboardRefreshAction =
                    dashboardRefreshAction;

            displayedPatients = new ArrayList<>();

            setLayout(new BorderLayout(15, 15));

            setBackground(
                    new Color(245, 248, 252)
            );

            setBorder(
                    new EmptyBorder(25, 25, 25, 25)
            );

            searchField = new JTextField();

            refreshButton = new ModernButton("Refresh");
            addButton = new ModernButton("Add Patient");
            updateButton = new ModernButton("Update");
            dischargeButton = new ModernButton("Discharge");

            tableModel = new DefaultTableModel(
                    new Object[]{
                            "ID Number",
                            "Name",
                            "Gender",
                            "Disease",
                            "Room",
                            "Admission Time",
                            "Deposit",
                            "Status",
                            "Discharge Time"
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

            patientTable = new JTable(tableModel);
            statusLabel = new JLabel("Loading patients...");

            createInterface();
            refreshPatients();
        }

        private void createInterface() {

            JLabel heading =
                    new JLabel("Patient Management");

            heading.setFont(
                    new Font("Segoe UI", Font.BOLD, 26)
            );

            heading.setForeground(
                    new Color(25, 55, 95)
            );

            JPanel header =
                    new JPanel(new BorderLayout());

            header.setOpaque(false);
            header.add(heading, BorderLayout.WEST);
            header.add(addButton, BorderLayout.EAST);

            searchField.setPreferredSize(
                    new Dimension(270, 38)
            );

            searchField.setToolTipText(
                    "Search by ID, name, disease, room or status"
            );

            JButton searchButton =
                    new ModernButton("Search");

            addButton.setBackground(new Color(20, 101, 219));
            searchButton.setBackground(new Color(12, 174, 184));
            refreshButton.setBackground(new Color(71, 85, 105));
            updateButton.setBackground(new Color(105, 82, 235));
            dischargeButton.setBackground(new Color(225, 65, 75));

            Dimension actionSize = new Dimension(118, 38);
            searchButton.setPreferredSize(actionSize);
            refreshButton.setPreferredSize(actionSize);
            updateButton.setPreferredSize(actionSize);
            dischargeButton.setPreferredSize(actionSize);
            addButton.setPreferredSize(new Dimension(145, 40));

            JPanel searchPanel = new JPanel(
                    new FlowLayout(FlowLayout.LEFT, 8, 0)
            );

            searchPanel.setOpaque(false);

            searchPanel.add(new JLabel("Search:"));
            searchPanel.add(searchField);
            searchPanel.add(searchButton);
            searchPanel.add(refreshButton);
            searchPanel.add(updateButton);
            searchPanel.add(dischargeButton);

            patientTable.setRowHeight(30);
            patientTable.setSelectionMode(
                    ListSelectionModel.SINGLE_SELECTION
            );

            patientTable.getTableHeader().setFont(
                    new Font("Segoe UI", Font.BOLD, 13)
            );

            JScrollPane scrollPane =
                    new JScrollPane(patientTable);

            JPanel top = new JPanel(
                    new BorderLayout(0, 15)
            );

            top.setOpaque(false);
            top.add(header, BorderLayout.NORTH);
            top.add(searchPanel, BorderLayout.SOUTH);

            add(top, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            add(statusLabel, BorderLayout.SOUTH);

            addButton.addActionListener(
                    event -> openAddDialog()
            );

            updateButton.addActionListener(
                    event -> openUpdateDialog()
            );

            dischargeButton.addActionListener(
                    event -> dischargeSelectedPatient()
            );

            refreshButton.addActionListener(
                    event -> refreshPatients()
            );

            searchButton.addActionListener(
                    event -> searchPatients()
            );

            searchField.addActionListener(
                    event -> searchPatients()
            );
        }

        public void refreshPatients() {

            loadPatients(null);
        }

        private void searchPatients() {

            loadPatients(searchField.getText());
        }

        private void loadPatients(
                String keyword
        ) {

            setLoading(true);

            SwingWorker<List<Patient>, Void> worker =
                    new SwingWorker<>() {

                        @Override
                        protected List<Patient> doInBackground()
                                throws Exception {

                            if (keyword == null
                                    || keyword.isBlank()) {

                                return patientService
                                        .getAllPatients();
                            }

                            return patientService
                                    .searchPatients(keyword);
                        }

                        @Override
                        protected void done() {

                            try {
                                displayedPatients = get();
                                showPatients(displayedPatients);

                                statusLabel.setForeground(
                                        new Color(25, 135, 84)
                                );

                                statusLabel.setText(
                                        displayedPatients.size()
                                                + " patient record(s)."
                                );

                            } catch (Exception exception) {

                                showError(
                                        getErrorMessage(exception)
                                );

                            } finally {
                                setLoading(false);
                            }
                        }
                    };

            worker.execute();
        }

        private void showPatients(
                List<Patient> patients
        ) {

            tableModel.setRowCount(0);

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd HH:mm"
                    );

            for (Patient patient : patients) {

                String dischargeTime =
                        patient.getDischargeTime() == null
                                ? "-"
                                : patient.getDischargeTime()
                                .format(formatter);

                tableModel.addRow(
                        new Object[]{
                                patient.getIdNumber(),
                                patient.getName(),
                                patient.getGender(),
                                patient.getDisease(),
                                patient.getRoomNumber(),
                                patient.getAdmissionTime(),
                                patient.getDeposit(),
                                patient.getStatus(),
                                dischargeTime
                        }
                );
            }
        }

        private Patient getSelectedPatient() {

            int selectedRow =
                    patientTable.getSelectedRow();

            if (selectedRow < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please table se patient select karein.",
                        "Patient Selection",
                        JOptionPane.WARNING_MESSAGE
                );

                return null;
            }

            int modelRow =
                    patientTable.convertRowIndexToModel(
                            selectedRow
                    );

            return displayedPatients.get(modelRow);
        }

        private void openAddDialog() {

            Window owner =
                    SwingUtilities.getWindowAncestor(this);

            AddPatientDialog dialog =
                    new AddPatientDialog(
                            owner,
                            patientService,
                            this::afterPatientChange
                    );

            dialog.setVisible(true);
        }

        private void openUpdateDialog() {

            Patient patient = getSelectedPatient();

            if (patient == null) {
                return;
            }

            if (!"Admitted".equalsIgnoreCase(
                    patient.getStatus()
            )) {
                showError(
                        "Discharged patient update nahi ho sakta."
                );
                return;
            }

            Window owner =
                    SwingUtilities.getWindowAncestor(this);

            UpdatePatientDialog dialog =
                    new UpdatePatientDialog(
                            owner,
                            patient,
                            patientService,
                            this::afterPatientChange
                    );

            dialog.setVisible(true);
        }

        private void dischargeSelectedPatient() {

            Patient patient = getSelectedPatient();

            if (patient == null) {
                return;
            }

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    patient.getName()
                            + " ko discharge karna hai?",
                    "Confirm Discharge",
                    JOptionPane.YES_NO_OPTION
            );

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }

            dischargeButton.setEnabled(false);
            statusLabel.setText("Patient discharge ho raha hai...");

            SwingWorker<Void, Void> worker =
                    new SwingWorker<>() {

                        @Override
                        protected Void doInBackground()
                                throws Exception {

                            patientService.dischargePatient(
                                    patient
                            );

                            return null;
                        }

                        @Override
                        protected void done() {

                            dischargeButton.setEnabled(true);

                            try {
                                get();

                                JOptionPane.showMessageDialog(
                                        PatientPanel.this,
                                        "Patient successfully discharged."
                                );

                                afterPatientChange();

                            } catch (Exception exception) {
                                showError(
                                        getErrorMessage(exception)
                                );
                            }
                        }
                    };

            worker.execute();
        }

        private void afterPatientChange() {

            refreshPatients();

            if (dashboardRefreshAction != null) {
                dashboardRefreshAction.run();
            }
        }

        private void setLoading(boolean loading) {

            refreshButton.setEnabled(!loading);
            addButton.setEnabled(!loading);
            updateButton.setEnabled(!loading);
            dischargeButton.setEnabled(!loading);

            if (loading) {
                statusLabel.setText("Patient data load ho raha hai...");
            }
        }

        private void showError(String message) {

            statusLabel.setForeground(
                    new Color(190, 45, 45)
            );

            statusLabel.setText(message);

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Patient Management",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        private String getErrorMessage(
                Exception exception
        ) {

            Throwable cause = exception.getCause();

            return cause != null
                    ? cause.getMessage()
                    : exception.getMessage();
        }
    }
