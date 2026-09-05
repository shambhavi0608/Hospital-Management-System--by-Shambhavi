package hospital.management.system.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A rounded, consistently painted Swing button.
 *
 * Windows Look & Feel can ignore JButton background colours. This component
 * paints its own background so action buttons never fall back to grey/silver.
 */
public class ModernButton extends JButton {

    private boolean hovered;

    public ModernButton(String text) {
        super(text);
        configure();
    }

    public ModernButton(String text, Icon icon) {
        super(text, icon);
        configure();
    }

    private void configure() {
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(Color.WHITE);
        setBackground(new Color(23, 104, 218));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        setBorderPainted(true);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Color base = isEnabled()
                ? getBackground()
                : new Color(203, 213, 225);

        if (getModel().isPressed()) {
            base = shade(base, 0.82f);
        } else if (hovered && isEnabled()) {
            base = brighten(base, 0.10f);
        }

        int arc = Math.min(16, Math.max(10, getHeight() / 3));
        g.setColor(base);
        g.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        if (isFocusOwner()) {
            g.setColor(new Color(255, 255, 255, 150));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5,
                    Math.max(8, arc - 2), Math.max(8, arc - 2));
        }

        g.dispose();
        super.paintComponent(graphics);
    }

    private Color brighten(Color color, float amount) {
        int red = color.getRed() + Math.round((255 - color.getRed()) * amount);
        int green = color.getGreen() + Math.round((255 - color.getGreen()) * amount);
        int blue = color.getBlue() + Math.round((255 - color.getBlue()) * amount);
        return new Color(red, green, blue, color.getAlpha());
    }

    private Color shade(Color color, float factor) {
        return new Color(
                Math.max(0, Math.round(color.getRed() * factor)),
                Math.max(0, Math.round(color.getGreen() * factor)),
                Math.max(0, Math.round(color.getBlue() * factor)),
                color.getAlpha()
        );
    }
}
