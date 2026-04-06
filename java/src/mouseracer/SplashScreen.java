package mouseracer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SplashScreen extends JWindow {

    private static final int DURATION_MS = 3000;
    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 56);
    private static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 20);
    private static final Font FONT_VERSION = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_HINT = new Font("SansSerif", Font.PLAIN, 14);
    private static final BasicStroke BORDER_STROKE = new BasicStroke(2f);

    private final long startTime;
    private final Runnable onDismiss;
    private Timer animTimer;

    public SplashScreen(Runnable onDismiss) {
        this.onDismiss = onDismiss;
        startTime = System.currentTimeMillis();
        setSize(600, 400);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, Theme.BG_TOP, 0, h, Theme.BG_BOTTOM));
                g2.fillRect(0, 0, w, h);

                g2.setColor(Theme.SURFACE_BORDER);
                g2.setStroke(BORDER_STROKE);
                g2.drawRect(0, 0, w - 1, h - 1);

                float elapsed = (System.currentTimeMillis() - startTime) / 1000f;
                float alpha = Math.min(1f, elapsed / 0.8f);

                g2.setFont(FONT_TITLE);
                g2.setColor(Theme.withAlpha(Theme.ACCENT, alpha));
                drawCentered(g2, "MOUSE RACER", w, h / 2 - 40);

                g2.setFont(FONT_SUBTITLE);
                g2.setColor(Theme.withAlpha(Theme.TEXT_SECONDARY, alpha));
                drawCentered(g2, "How fast can you click?", w, h / 2 + 15);

                g2.setFont(FONT_VERSION);
                g2.setColor(Theme.withAlpha(Theme.TEXT_SECONDARY, alpha * 0.6f));
                drawCentered(g2, "v1.0 \u2014 Java Edition", w, h / 2 + 55);

                if (elapsed > 1.0f) {
                    float pulse = (float) (Math.sin((elapsed - 1.0) * 2.0) * 0.3 + 0.7);
                    g2.setFont(FONT_HINT);
                    g2.setColor(Theme.withAlpha(Theme.TEXT_PRIMARY, pulse));
                    drawCentered(g2, "Click anywhere to continue", w, h - 40);
                }

                g2.dispose();
            }
        };

        setContentPane(panel);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { dismiss(); }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { dismiss(); }
        });

        animTimer = new Timer(16, e -> panel.repaint());
        animTimer.start();

        Timer autoClose = new Timer(DURATION_MS, e -> dismiss());
        autoClose.setRepeats(false);
        autoClose.start();

        setFocusable(true);
        requestFocus();
    }

    private void dismiss() {
        if (!isVisible()) return;
        animTimer.stop();
        setVisible(false);
        dispose();
        onDismiss.run();
    }

    private static void drawCentered(Graphics2D g2, String text, int width, int y) {
        int x = (width - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }
}
