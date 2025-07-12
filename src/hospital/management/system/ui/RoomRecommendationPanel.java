package hospital.management.system.ui;

import hospital.management.system.dao.RoomDAO.RoomSuggestion;
import hospital.management.system.service.RoomRecommendationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/*
 * PURPOSE:
 * Ye Swing panel patient ke budget ke according available rooms suggest karta hai.
 *
 * INTERVIEW EXPLANATION:
 * SwingWorker database query ko background thread par run karta hai,
 * jisse application UI freeze nahi hoti.
 */
public class RoomRecommendationPanel extends JPanel {

    private final JTextField budgetField;
    private final JButton searchButton;
    private final JLabel statusLabel;
    private final DefaultTableModel tableModel;

    private final RoomRecommendationService recommendationService;

    public RoomRecommendationPanel() {

        recommendationService = new RoomRecommendationService();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 248, 252));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        /*
         * Panel heading
         */
        JLabel headingLabel = new JLabel("Smart Room Recommendation");
        headingLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        headingLabel.setForeground(new Color(25, 55, 95));

        JLabel descriptionLabel = new JLabel(
                "Patient ke maximum budget ke according available rooms search karein."
        );
        descriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descriptionLabel.setForeground(new Color(90, 100, 115));

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.add(headingLabel);
        headingPanel.add(Box.createVerticalStrut(5));
        headingPanel.add(descriptionLabel);

        /*
         * Budget input section
         */
        JLabel budgetLabel = new JLabel("Maximum Budget (₹):");
        budgetLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

        budgetField = new JTextField();
        budgetField.setPreferredSize(new Dimension(180, 38));
        budgetField.setFont(new Font("SansSerif", Font.PLAIN, 15));

        searchButton = new ModernButton("Find Available Rooms");
        searchButton.setPreferredSize(new Dimension(190, 38));
        searchButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        searchButton.setBackground(new Color(34, 114, 195));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        searchPanel.add(budgetLabel);
        searchPanel.add(budgetField);
        searchPanel.add(searchButton);

        /*
         * Table user ko room recommendations clearly show karegi.
         * Cells editable nahi rakhe gaye hain.
         */
        tableModel = new DefaultTableModel(
                new Object[]{"Room Number", "Price Per Day", "Status"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable roomTable = new JTable(tableModel);
        roomTable.setRowHeight(32);
        roomTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roomTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        roomTable.getTableHeader().setBackground(
                new Color(225, 235, 247)
        );
        roomTable.setSelectionBackground(new Color(205, 225, 248));
        roomTable.setFillsViewportHeight(true);

        JScrollPane tableScrollPane = new JScrollPane(roomTable);
        tableScrollPane.setBorder(
                BorderFactory.createLineBorder(new Color(215, 220, 228))
        );

        /*
         * Status label user ko loading, success aur error information deta hai.
         */
        statusLabel = new JLabel(
                "Budget enter karke Find Available Rooms par click karein."
        );
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(80, 90, 105));

        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setOpaque(false);
        topPanel.add(headingPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        /*
         * Button click par room search start hogi.
         */
        searchButton.addActionListener(event -> searchRooms());

        /*
         * Enter key press karke bhi search kar sakte hain.
         */
        budgetField.addActionListener(event -> searchRooms());
    }

    private void searchRooms() {

        String enteredBudget = budgetField.getText().trim();

        if (enteredBudget.isEmpty()) {
            showError("Please maximum budget enter karein.");
            return;
        }

        final double maximumBudget;

        try {
            maximumBudget = Double.parseDouble(enteredBudget);

            if (maximumBudget <= 0) {
                showError("Budget zero se greater hona chahiye.");
                return;
            }

        } catch (NumberFormatException exception) {
            showError("Budget mein valid number enter karein, jaise 1500.");
            return;
        }

        /*
         * Previous results clear karke loading state show karte hain.
         */
        tableModel.setRowCount(0);
        searchButton.setEnabled(false);
        statusLabel.setForeground(new Color(34, 114, 195));
        statusLabel.setText("Available rooms search ho rahe hain...");

        /*
         * SwingWorker database query ko background mein execute karta hai.
         */
        SwingWorker<List<RoomSuggestion>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<RoomSuggestion> doInBackground()
                            throws SQLException {

                        return recommendationService.recommendRooms(
                                maximumBudget
                        );
                    }

                    @Override
                    protected void done() {

                        searchButton.setEnabled(true);

                        try {
                            List<RoomSuggestion> suggestions = get();

                            if (suggestions.isEmpty()) {
                                statusLabel.setForeground(
                                        new Color(190, 90, 20)
                                );
                                statusLabel.setText(
                                        "Is budget mein koi available room nahi mila."
                                );
                                return;
                            }

                            for (RoomSuggestion suggestion : suggestions) {

                                tableModel.addRow(new Object[]{
                                        suggestion.roomNumber(),
                                        "₹" + String.format(
                                                "%.2f",
                                                suggestion.price()
                                        ),
                                        "Available"
                                });
                            }

                            statusLabel.setForeground(
                                    new Color(25, 135, 84)
                            );
                            statusLabel.setText(
                                    suggestions.size()
                                            + " suitable room(s) mile."
                            );

                        } catch (Exception exception) {

                            /*
                             * SwingWorker ExecutionException mein actual
                             * database exception wrap kar sakta hai.
                             */
                            Throwable cause = exception.getCause();

                            String message = cause != null
                                    ? cause.getMessage()
                                    : exception.getMessage();

                            showError(
                                    "Rooms load nahi hue: " + message
                            );
                        }
                    }
                };

        worker.execute();
    }

    private void showError(String message) {

        statusLabel.setForeground(new Color(190, 45, 45));
        statusLabel.setText(message);

        JOptionPane.showMessageDialog(
                this,
                message,
                "Room Recommendation",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
