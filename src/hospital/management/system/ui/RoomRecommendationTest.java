package hospital.management.system.ui;

import javax.swing.*;

/*
 * PURPOSE:
 * Main dashboard mein add karne se pehle Room Recommendation Panel
 * ko independently test karne ke liye ye class banayi hai.
 *
 * INTERVIEW:
 * Individual component testing se bugs isolate karna easy hota hai.
 */
public class RoomRecommendationTest {

    public static void main(String[] args) {

        /*
         * Swing UI ko Event Dispatch Thread par create karna thread-safe hai.
         */
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame(
                    "Hospital Management System - Room Recommendation"
            );

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);

            frame.add(new RoomRecommendationPanel());

            frame.setVisible(true);
        });
    }
}