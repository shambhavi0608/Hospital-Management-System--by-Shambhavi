package hospital.management.system.ui;

import hospital.management.system.util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/*
 * DashboardCard ek reusable Swing component hai.
 *
 * Isi component ko total patients, available rooms
 * aur occupied rooms ke liye reuse kiya jayega.
 */
public class DashboardCard extends JPanel {

    /*
     * valueLabel ko field banaya hai taaki database se
     * new value aane par ise update kar saken.
     */
    private final JLabel valueLabel;

    public DashboardCard(
            String title,
            String initialValue,
            Color accentColor
    ) {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CARD_BACKGROUND);

        /*
         * Top accent border har statistics card ko
         * visually distinguish karta hai.
         */
        setBorder(
                new CompoundBorder(
                        new MatteBorder(
                                5, 0, 0, 0, accentColor
                        ),
                        new EmptyBorder(
                                20, 22, 20, 22
                        )
                )
        );

        setPreferredSize(
                new Dimension(240, 140)
        );

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.NORMAL_FONT);
        titleLabel.setForeground(
                UITheme.TEXT_SECONDARY
        );

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 32)
        );
        valueLabel.setForeground(
                UITheme.TEXT_PRIMARY
        );

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }

    /*
     * Live database value ko card par update karta hai.
     */
    public void setValue(int value) {
        valueLabel.setText(String.valueOf(value));
    }

    /*
     * Database error hone par text value show karne ke liye.
     */
    public void setValue(String value) {
        valueLabel.setText(value);
    }
}