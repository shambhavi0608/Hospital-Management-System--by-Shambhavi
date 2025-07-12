package hospital.management.system.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * PURPOSE:
 * External chart library ke bina Java Graphics2D se bar chart banata hai.
 *
 * INTERVIEW:
 * Custom Swing painting se lightweight data visualization implement ki.
 */
public class SimpleBarChartPanel extends JPanel {

    private final String chartTitle;
    private Map<String, Integer> chartData;

    public SimpleBarChartPanel(String chartTitle) {

        this.chartTitle = chartTitle;
        this.chartData = new LinkedHashMap<>();

        setBackground(Color.WHITE);

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(220, 225, 232)
                ),
                new EmptyBorder(15, 15, 15, 15)
        ));
    }

    public void setChartData(
            Map<String, Integer> chartData
    ) {

        this.chartData = chartData == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(chartData);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        Graphics2D graphics2D =
                (Graphics2D) graphics.create();

        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics2D.setColor(new Color(30, 45, 70));

        graphics2D.setFont(
                new Font("Segoe UI", Font.BOLD, 18)
        );

        graphics2D.drawString(chartTitle, 20, 30);

        if (chartData.isEmpty()) {

            graphics2D.setFont(
                    new Font("Segoe UI", Font.PLAIN, 14)
            );

            graphics2D.setColor(Color.GRAY);

            graphics2D.drawString(
                    "No analytics data available",
                    20,
                    65
            );

            graphics2D.dispose();
            return;
        }

        int maximumValue = chartData.values()
                .stream()
                .max(Integer::compareTo)
                .orElse(1);

        int chartLeft = 55;
        int chartTop = 60;
        int chartBottom = getHeight() - 55;
        int chartHeight = chartBottom - chartTop;

        int availableWidth =
                getWidth() - chartLeft - 30;

        int barAreaWidth =
                availableWidth / chartData.size();

        int barWidth =
                Math.max(25, barAreaWidth - 25);

        int index = 0;

        Color[] colors = {
                new Color(59, 130, 246),
                new Color(16, 185, 129),
                new Color(249, 115, 22),
                new Color(139, 92, 246)
        };

        graphics2D.setColor(new Color(190, 195, 205));

        graphics2D.drawLine(
                chartLeft,
                chartBottom,
                getWidth() - 20,
                chartBottom
        );

        for (Map.Entry<String, Integer> entry
                : chartData.entrySet()) {

            int value = entry.getValue();

            int calculatedHeight =
                    (int) (
                            (value / (double) maximumValue)
                                    * (chartHeight - 35)
                    );

            int x = chartLeft + index * barAreaWidth;
            int y = chartBottom - calculatedHeight;

            graphics2D.setColor(
                    colors[index % colors.length]
            );

            graphics2D.fillRoundRect(
                    x,
                    y,
                    barWidth,
                    calculatedHeight,
                    12,
                    12
            );

            graphics2D.setColor(new Color(30, 45, 70));

            graphics2D.setFont(
                    new Font("Segoe UI", Font.BOLD, 13)
            );

            graphics2D.drawString(
                    String.valueOf(value),
                    x + (barWidth / 2) - 5,
                    y - 8
            );

            graphics2D.setFont(
                    new Font("Segoe UI", Font.PLAIN, 12)
            );

            graphics2D.drawString(
                    entry.getKey(),
                    x,
                    chartBottom + 20
            );

            index++;
        }

        graphics2D.dispose();
    }
}