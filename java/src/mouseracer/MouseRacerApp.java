package mouseracer;

import javax.swing.*;

public class MouseRacerApp {

    private void startup() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore
        }

        SplashScreen splash = new SplashScreen(() -> {
            MouseRacerView view = new MouseRacerView();
            view.setVisible(true);
        });
        splash.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MouseRacerApp().startup());
    }
}
