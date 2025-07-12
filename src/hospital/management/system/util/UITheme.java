package hospital.management.system.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/*
 * Poore application ki common styling ek jagah rakhta hai.
 * Isse colours aur fonts har screen mein repeat nahi honge.
 */
public final class UITheme {

    public static final Color PRIMARY =
            new Color(22, 98, 150);

    public static final Color PRIMARY_DARK =
            new Color(12, 52, 82);

    public static final Color ACCENT =
            new Color(20, 184, 166);

    public static final Color BACKGROUND =
            new Color(241, 245, 249);

    public static final Color CARD_BACKGROUND =
            Color.WHITE;

    public static final Color TEXT_PRIMARY =
            new Color(30, 41, 59);

    public static final Color TEXT_SECONDARY =
            new Color(100, 116, 139);

    public static final Font TITLE_FONT =
            new Font("Segoe UI", Font.BOLD, 28);

    public static final Font HEADING_FONT =
            new Font("Segoe UI", Font.BOLD, 18);

    public static final Font NORMAL_FONT =
            new Font("Segoe UI", Font.PLAIN, 14);

    /*
     * Utility class ka object nahi banana,
     * isliye constructor private hai.
     */
    private UITheme() {
    }

    public static void styleSidebarButton(JButton button) {

        button.setFont(NORMAL_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_DARK);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setBorder(
                new EmptyBorder(12, 20, 12, 20)
        );

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48)
        );
    }

    public static void stylePrimaryButton(JButton button) {

        button.setFont(NORMAL_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);

        button.setBorder(
                new EmptyBorder(10, 18, 10, 18)
        );

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );
    }
}