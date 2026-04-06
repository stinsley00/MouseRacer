package mouseracer;

import javax.swing.*;
import java.awt.*;

public class MouseRacerAboutBox extends JDialog {

    public MouseRacerAboutBox(Frame parent) {
        super(parent);
        initComponents();
        getRootPane().setDefaultButton(closeButton);
    }

    public void closeAboutBox() {
        dispose();
    }

    private void initComponents() {
        closeButton = Theme.styledButton("Close", Theme.BTN_TOP);
        closeButton.addActionListener(e -> closeAboutBox());

        JLabel appTitleLabel = Theme.styledLabel("Mouse Racer", Theme.ACCENT, 18, Font.BOLD);
        JLabel versionLabel = Theme.styledLabel("Product Version:", Theme.TEXT_SECONDARY, 12, Font.BOLD);
        JLabel appVersionLabel = Theme.styledLabel("1.0", Theme.TEXT_PRIMARY, 12, Font.PLAIN);
        JLabel vendorLabel = Theme.styledLabel("Vendor:", Theme.TEXT_SECONDARY, 12, Font.BOLD);
        JLabel appVendorLabel = Theme.styledLabel("Mouse Racer Team", Theme.TEXT_PRIMARY, 12, Font.PLAIN);
        JLabel homepageLabel = Theme.styledLabel("Homepage:", Theme.TEXT_SECONDARY, 12, Font.BOLD);
        JLabel appHomepageLabel = Theme.styledLabel("github.com/mouse-racer", Theme.ACCENT, 12, Font.PLAIN);
        JLabel appDescLabel = new JLabel("<html>A simple click-racing desktop application built with Java Swing.");
        appDescLabel.setForeground(Theme.TEXT_PRIMARY);
        JLabel imageLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About: Mouse Racer 1.0");
        setModal(true);
        setResizable(false);

        try {
            java.net.URL imageUrl = getClass().getResource("/mouseracer/resources/about.png");
            if (imageUrl != null) {
                imageLabel.setIcon(new ImageIcon(imageUrl));
            }
        } catch (Exception e) {
            // ignore
        }

        getContentPane().setBackground(Theme.BG_TOP);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(imageLabel)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(versionLabel)
                            .addComponent(vendorLabel)
                            .addComponent(homepageLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(appVersionLabel)
                            .addComponent(appVendorLabel)
                            .addComponent(appHomepageLabel)))
                    .addComponent(appTitleLabel, GroupLayout.Alignment.LEADING)
                    .addComponent(appDescLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                    .addComponent(closeButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(imageLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(appTitleLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(appDescLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(appVersionLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(vendorLabel)
                    .addComponent(appVendorLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(homepageLabel)
                    .addComponent(appHomepageLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(closeButton)
                .addContainerGap())
        );

        pack();
    }

    private JButton closeButton;
}
