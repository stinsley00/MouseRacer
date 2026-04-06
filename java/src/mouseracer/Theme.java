package mouseracer;

import javax.swing.*;
import java.awt.*;

public class Theme {
    public static final Color BG_TOP = new Color(15, 15, 35);
    public static final Color BG_BOTTOM = new Color(30, 30, 60);
    public static final Color ACCENT = new Color(0, 200, 255);
    public static final Color ACCENT_DIM = new Color(0, 120, 180);
    public static final Color SURFACE_BORDER = new Color(60, 60, 100);
    public static final Color TEXT_PRIMARY = new Color(230, 235, 255);
    public static final Color TEXT_SECONDARY = new Color(140, 150, 180);
    public static final Color BTN_TOP = new Color(0, 180, 230);
    public static final Color BTN_BOTTOM = new Color(0, 100, 180);
    public static final Color BTN_DISABLED_TOP = new Color(50, 50, 70);
    public static final Color BTN_DISABLED_BOTTOM = new Color(35, 35, 55);
    public static final Color DANGER_TOP = new Color(180, 50, 50);
    public static final Color DANGER_BOTTOM = new Color(120, 30, 30);
    public static final Color SUCCESS = new Color(0, 220, 120);
    public static final Color GAME_OVER = new Color(255, 80, 80);
    public static final Color RESULTS_BG = new Color(20, 20, 45);

    public static final Color GOLD = new Color(255, 215, 0);
    public static final Color SILVER = new Color(192, 192, 192);
    public static final Color BRONZE = new Color(205, 127, 50);

    public static Color brighter(Color c, int amount) {
        return new Color(
            Math.min(255, c.getRed() + amount),
            Math.min(255, c.getGreen() + amount),
            Math.min(255, c.getBlue() + amount)
        );
    }

    public static Color withAlpha(Color c, float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255)));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    public static JLabel styledLabel(String text, Color color, int size, int style) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("SansSerif", style, size));
        return label;
    }

    public static JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }
}
