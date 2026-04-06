package mouseracer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class MouseRacerView extends JFrame implements GameEngine.GameListener {

    private final GameEngine engine;
    private final HighScoreBoard highScoreBoard;

    // Components
    private GradientPanel mainPanel;
    private GradientButton raceButton;
    private GradientButton playAgainButton;
    private GradientButton exitButton;
    private JLabel statusLabel;
    private JLabel clickCountLabel;
    private JLabel timerLabel;
    private JLabel cpsLabel;
    private JRadioButton sprintMode;
    private JRadioButton enduranceMode;
    private JRadioButton marathonMode;
    private JTextPane resultsArea;
    private JDialog aboutBox;

    // Last game data for high score submission
    private GameEngine.GameMode lastGameMode;
    private int lastTotalClicks;
    private float lastSeconds;
    private float lastAvgCps;
    private float lastMaxCps;

    public MouseRacerView() {
        engine = new GameEngine(this);
        highScoreBoard = new HighScoreBoard();
        setTitle("Mouse Racer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(false);
        initComponents();
        resetUI();
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 700));
    }

    public void showAboutBox() {
        if (aboutBox == null) {
            aboutBox = new MouseRacerAboutBox(this);
            aboutBox.setLocationRelativeTo(this);
        }
        aboutBox.setVisible(true);
    }

    private void initComponents() {
        mainPanel = new GradientPanel(Theme.BG_TOP, Theme.BG_BOTTOM);
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setPreferredSize(new Dimension(1200, 750));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);

        // === Title ===
        statusLabel = new JLabel("MOUSE RACER");
        statusLabel.setForeground(Theme.ACCENT);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.6;
        mainPanel.add(statusLabel, gbc);

        // === Stats panel (top right) ===
        JPanel statsPanel = createStatsPanel();
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.4;
        mainPanel.add(statsPanel, gbc);

        // === Race Button (center-left, big) ===
        raceButton = new GradientButton("CLICK TO RACE!", Theme.BTN_TOP, Theme.BTN_BOTTOM);
        raceButton.setFont(new Font("SansSerif", Font.BOLD, 32));
        raceButton.setEnabled(false);
        raceButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (raceButton.isEnabled()) {
                    if (!engine.isRunning()) {
                        startGame();
                    }
                    engine.registerClick();
                }
            }
        });
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2; gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6; gbc.weighty = 1.0;
        gbc.insets = new Insets(15, 10, 15, 20);
        mainPanel.add(raceButton, gbc);

        // === Right side panel ===
        JPanel rightPanel = createRightPanel();
        gbc.gridx = 2; gbc.gridy = 1;
        gbc.gridwidth = 1; gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.4; gbc.weighty = 1.0;
        gbc.insets = new Insets(15, 0, 15, 10);
        mainPanel.add(rightPanel, gbc);

        // === Bottom bar with image ===
        JPanel bottomPanel = createBottomPanel();
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 3; gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.insets = new Insets(8, 10, 0, 10);
        mainPanel.add(bottomPanel, gbc);

        add(mainPanel);

        // === Menu Bar ===
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Theme.BG_TOP);
        menuBar.setBorderPainted(false);

        JMenu fileMenu = createStyledMenu("File");
        JMenuItem highScoresItem = createStyledMenuItem("High Scores");
        highScoresItem.addActionListener(e -> showHighScores());
        fileMenu.add(highScoresItem);
        fileMenu.addSeparator();
        JMenuItem exitMenuItem = createStyledMenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenuItem);

        JMenu helpMenu = createStyledMenu("Help");
        JMenuItem instructionsItem = createStyledMenuItem("Instructions");
        instructionsItem.addActionListener(e -> showInstructions());
        helpMenu.add(instructionsItem);
        JMenuItem aboutMenuItem = createStyledMenuItem("About");
        aboutMenuItem.addActionListener(e -> showAboutBox());
        helpMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 4));
        panel.setOpaque(false);

        JLabel timeText = Theme.styledLabel("TIME", Theme.TEXT_SECONDARY, 16, Font.BOLD);
        timerLabel = Theme.styledLabel("0", Theme.TEXT_PRIMARY, 28, Font.BOLD);

        JLabel clicksText = Theme.styledLabel("CLICKS", Theme.TEXT_SECONDARY, 16, Font.BOLD);
        clickCountLabel = Theme.styledLabel("0", Theme.TEXT_PRIMARY, 28, Font.BOLD);

        JLabel cpsText = Theme.styledLabel("CPS", Theme.TEXT_SECONDARY, 16, Font.BOLD);
        cpsLabel = Theme.styledLabel("0.0", Theme.SUCCESS, 28, Font.BOLD);

        panel.add(timeText);    panel.add(timerLabel);
        panel.add(clicksText);  panel.add(clickCountLabel);
        panel.add(cpsText);     panel.add(cpsLabel);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Results area
        resultsArea = new JTextPane();
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        resultsArea.setEditable(false);
        resultsArea.setBackground(Theme.RESULTS_BG);
        resultsArea.setForeground(Theme.TEXT_PRIMARY);
        resultsArea.setCaretColor(Theme.ACCENT);
        resultsArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.SURFACE_BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.RESULTS_BG);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scrollPane);

        panel.add(Box.createVerticalStrut(12));

        // Mode selection
        JLabel modeLabel = Theme.styledLabel("SELECT MODE", Theme.TEXT_SECONDARY, 13, Font.BOLD);
        modeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(modeLabel);
        panel.add(Box.createVerticalStrut(6));

        sprintMode = styledRadio("Sprint (10s)");
        enduranceMode = styledRadio("Endurance (100s)");
        marathonMode = styledRadio("Marathon (Infinite)");

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(sprintMode);
        modeGroup.add(enduranceMode);
        modeGroup.add(marathonMode);

        sprintMode.addActionListener(e -> raceButton.setEnabled(true));
        enduranceMode.addActionListener(e -> raceButton.setEnabled(true));
        marathonMode.addActionListener(e -> raceButton.setEnabled(true));

        panel.add(sprintMode);
        panel.add(enduranceMode);
        panel.add(marathonMode);
        panel.add(Box.createVerticalStrut(14));

        // Buttons
        playAgainButton = new GradientButton("Play Again", Theme.BTN_TOP, Theme.BTN_BOTTOM);
        playAgainButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        playAgainButton.setEnabled(false);
        playAgainButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        playAgainButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        playAgainButton.addActionListener(e -> resetUI());
        panel.add(playAgainButton);

        panel.add(Box.createVerticalStrut(8));

        exitButton = new GradientButton("Exit", Theme.DANGER_TOP, Theme.DANGER_BOTTOM);
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        exitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        exitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        exitButton.addActionListener(e -> System.exit(0));
        panel.add(exitButton);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);

        try {
            ImageIcon dmIcon = new ImageIcon(getClass().getResource("resources/dm.jpg"));
            Image dmImage = dmIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            JLabel dmLabel = new JLabel(new ImageIcon(dmImage));
            dmLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
            panel.add(dmLabel);
        } catch (Exception ignored) {}

        JLabel credit = Theme.styledLabel("Mouse Racer v1.0", Theme.TEXT_SECONDARY, 12, Font.PLAIN);
        panel.add(credit);

        return panel;
    }

    private JRadioButton styledRadio(String text) {
        JRadioButton radio = new JRadioButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int boxSize = 16;
                int y = (getHeight() - boxSize) / 2;

                g2.setColor(isSelected() ? Theme.ACCENT : Theme.SURFACE_BORDER);
                g2.fillOval(2, y, boxSize, boxSize);

                g2.setColor(Theme.RESULTS_BG);
                g2.fillOval(4, y + 2, boxSize - 4, boxSize - 4);

                if (isSelected()) {
                    g2.setColor(Theme.ACCENT);
                    g2.fillOval(6, y + 4, boxSize - 8, boxSize - 8);
                }

                g2.setFont(getFont());
                g2.setColor(isEnabled() ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), boxSize + 8, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);

                g2.dispose();
            }
        };
        radio.setFont(new Font("SansSerif", Font.PLAIN, 15));
        radio.setOpaque(false);
        radio.setForeground(Theme.TEXT_PRIMARY);
        radio.setAlignmentX(Component.LEFT_ALIGNMENT);
        radio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        radio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return radio;
    }

    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(Theme.TEXT_SECONDARY);
        menu.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return menu;
    }

    private JMenuItem createStyledMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setBackground(Theme.BG_TOP);
        item.setForeground(Theme.TEXT_PRIMARY);
        item.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return item;
    }

    private void startGame() {
        GameEngine.GameMode mode = GameEngine.GameMode.SPRINT;
        if (enduranceMode.isSelected()) mode = GameEngine.GameMode.ENDURANCE;
        else if (marathonMode.isSelected()) mode = GameEngine.GameMode.MARATHON;

        lastGameMode = mode;
        statusLabel.setText("RACING!");
        statusLabel.setForeground(Theme.SUCCESS);
        raceButton.setText("CLICK!");
        sprintMode.setEnabled(false);
        enduranceMode.setEnabled(false);
        marathonMode.setEnabled(false);
        playAgainButton.setEnabled(true);
        engine.start(mode);
    }

    private void resetUI() {
        engine.stop();
        statusLabel.setText("MOUSE RACER");
        statusLabel.setForeground(Theme.ACCENT);
        raceButton.setText("CLICK TO RACE!");
        clickCountLabel.setText("0");
        timerLabel.setText("0");
        cpsLabel.setText("0.0");
        resultsArea.setText("");
        sprintMode.setEnabled(true);
        enduranceMode.setEnabled(true);
        marathonMode.setEnabled(true);
        sprintMode.setSelected(false);
        enduranceMode.setSelected(false);
        marathonMode.setSelected(false);
        raceButton.setEnabled(false);
        playAgainButton.setEnabled(false);
    }

    // --- High Scores ---

    private void showNamePrompt() {
        JDialog dialog = new JDialog(this, "Game Over \u2014 Enter Your Name", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG_TOP);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel prompt = Theme.styledLabel("Save your score to the leaderboard!", Theme.TEXT_PRIMARY, 15, Font.PLAIN);
        prompt.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(prompt);
        panel.add(Box.createVerticalStrut(12));

        JTextField nameField = new JTextField(20);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        nameField.setBackground(Theme.RESULTS_BG);
        nameField.setForeground(Theme.TEXT_PRIMARY);
        nameField.setCaretColor(Theme.ACCENT);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.SURFACE_BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(14));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton submitBtn = Theme.styledButton("Submit", Theme.BTN_TOP);
        JButton skipBtn = Theme.styledButton("Skip", Theme.SURFACE_BORDER);

        Runnable submit = () -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) name = "Anonymous";
            HighScoreEntry entry = HighScoreBoard.newEntry(name, lastGameMode, lastTotalClicks, lastSeconds, lastAvgCps, lastMaxCps);
            highScoreBoard.addEntry(entry);
            highScoreBoard.save();
            dialog.dispose();
            showHighScores();
        };

        submitBtn.addActionListener(e -> submit.run());
        nameField.addActionListener(e -> submit.run()); // Enter key
        skipBtn.addActionListener(e -> dialog.dispose());

        buttons.add(submitBtn);
        buttons.add(skipBtn);
        panel.add(buttons);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showHighScores() {
        JDialog dialog = new JDialog(this, "High Scores", true);
        dialog.setSize(650, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Theme.BG_TOP);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Tab buttons
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabs.setOpaque(false);

        String[] modes = {"SPRINT", "ENDURANCE", "MARATHON"};
        String[] labels = {"Sprint", "Endurance", "Marathon"};
        JButton[] tabBtns = new JButton[3];
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);

        Runnable[] refreshTable = {null};
        final int[] selectedTab = {0};

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            tabBtns[i] = new JButton(labels[i]);
            tabBtns[i].setFocusPainted(false);
            tabBtns[i].setFont(new Font("SansSerif", Font.BOLD, 14));
            tabBtns[i].addActionListener(e -> {
                selectedTab[0] = idx;
                refreshTable[0].run();
            });
            tabs.add(tabBtns[i]);
        }

        refreshTable[0] = () -> {
            for (int i = 0; i < 3; i++) {
                if (i == selectedTab[0]) {
                    tabBtns[i].setBackground(Theme.ACCENT);
                    tabBtns[i].setForeground(Theme.BG_TOP);
                } else {
                    tabBtns[i].setBackground(Theme.SURFACE_BORDER);
                    tabBtns[i].setForeground(Theme.TEXT_SECONDARY);
                }
            }
            tableContainer.removeAll();

            List<HighScoreEntry> entries = highScoreBoard.topN(modes[selectedTab[0]], 10);
            String[] columns = {"#", "Name", "Clicks", "Time", "Avg CPS", "Max CPS", "Date"};
            Object[][] data = new Object[entries.size()][7];
            for (int i = 0; i < entries.size(); i++) {
                HighScoreEntry e = entries.get(i);
                data[i] = new Object[]{
                    i + 1,
                    e.name,
                    e.totalClicks,
                    String.format("%.0fs", e.seconds),
                    String.format("%.2f", e.avgCps),
                    String.format("%.1f", e.maxCps),
                    e.date.contains("T") ? e.date.split("T")[0] : e.date
                };
            }

            JTable table = new JTable(data, columns) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            };
            table.setBackground(Theme.RESULTS_BG);
            table.setForeground(Theme.TEXT_PRIMARY);
            table.setGridColor(Theme.SURFACE_BORDER);
            table.setSelectionBackground(Theme.ACCENT_DIM);
            table.setSelectionForeground(Color.WHITE);
            table.setFont(new Font("SansSerif", Font.PLAIN, 13));
            table.setRowHeight(28);
            table.getTableHeader().setBackground(Theme.BG_BOTTOM);
            table.getTableHeader().setForeground(Theme.TEXT_SECONDARY);
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

            // Rank column coloring
            table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                    c.setBackground(Theme.RESULTS_BG);
                    if (row == 0) c.setForeground(Theme.GOLD);
                    else if (row == 1) c.setForeground(Theme.SILVER);
                    else if (row == 2) c.setForeground(Theme.BRONZE);
                    else c.setForeground(Theme.TEXT_PRIMARY);
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    return c;
                }
            });

            // Set narrow rank column
            table.getColumnModel().getColumn(0).setPreferredWidth(30);
            table.getColumnModel().getColumn(0).setMaxWidth(40);

            if (entries.isEmpty()) {
                JLabel empty = new JLabel("No scores recorded yet. Play a game!");
                empty.setForeground(Theme.TEXT_SECONDARY);
                empty.setFont(new Font("SansSerif", Font.PLAIN, 15));
                empty.setHorizontalAlignment(SwingConstants.CENTER);
                tableContainer.add(empty, BorderLayout.CENTER);
            } else {
                JScrollPane sp = new JScrollPane(table);
                sp.setBorder(BorderFactory.createLineBorder(Theme.SURFACE_BORDER));
                sp.getViewport().setBackground(Theme.RESULTS_BG);
                tableContainer.add(sp, BorderLayout.CENTER);
            }
            tableContainer.revalidate();
            tableContainer.repaint();
        };

        // Default to the mode just played
        if (lastGameMode != null) {
            for (int i = 0; i < modes.length; i++) {
                if (modes[i].equals(lastGameMode.name())) {
                    selectedTab[0] = i;
                    break;
                }
            }
        }
        refreshTable[0].run();

        panel.add(tabs, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);

        JButton closeBtn = Theme.styledButton("Close", Theme.BTN_TOP);
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(closeBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showInstructions() {
        JDialog dialog = new JDialog(this, "Instructions", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG_TOP);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        panel.add(Theme.styledLabel("How to Play", Theme.ACCENT, 20, Font.BOLD));
        panel.add(Box.createVerticalStrut(8));
        panel.add(Theme.styledLabel("1. Select a game mode from the right panel.", Theme.TEXT_PRIMARY, 14, Font.PLAIN));
        panel.add(Theme.styledLabel("2. Click the big button to start the race.", Theme.TEXT_PRIMARY, 14, Font.PLAIN));
        panel.add(Theme.styledLabel("3. Click as fast as you can!", Theme.TEXT_PRIMARY, 14, Font.PLAIN));
        panel.add(Theme.styledLabel("4. Your results will appear when the game ends.", Theme.TEXT_PRIMARY, 14, Font.PLAIN));

        panel.add(Box.createVerticalStrut(14));
        panel.add(Theme.styledLabel("Game Modes", Theme.ACCENT, 18, Font.BOLD));
        panel.add(Box.createVerticalStrut(6));
        panel.add(Theme.styledLabel("Sprint (10s)", Theme.SUCCESS, 14, Font.BOLD));
        panel.add(Theme.styledLabel("  Timer counts down from 10. Click as many times as you can.", Theme.TEXT_PRIMARY, 13, Font.PLAIN));
        panel.add(Box.createVerticalStrut(4));
        panel.add(Theme.styledLabel("Endurance (100s)", Theme.SUCCESS, 14, Font.BOLD));
        panel.add(Theme.styledLabel("  Timer counts up to 100. Test your sustained clicking.", Theme.TEXT_PRIMARY, 13, Font.PLAIN));
        panel.add(Box.createVerticalStrut(4));
        panel.add(Theme.styledLabel("Marathon (Infinite)", Theme.SUCCESS, 14, Font.BOLD));
        panel.add(Theme.styledLabel("  Ends when your CPS drops below 75% of your peak.", Theme.TEXT_PRIMARY, 13, Font.PLAIN));

        panel.add(Box.createVerticalGlue());
        JButton closeBtn = Theme.styledButton("Close", Theme.BTN_TOP);
        closeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        closeBtn.addActionListener(e -> dialog.dispose());
        panel.add(closeBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // --- GameListener callbacks ---

    @Override
    public void onTick(float seconds, int totalClicks, float currentCps) {
        SwingUtilities.invokeLater(() -> {
            timerLabel.setText(String.format("%.0f", seconds));
            clickCountLabel.setText(String.valueOf(totalClicks));
            cpsLabel.setText(String.format("%.1f", currentCps));
        });
    }

    @Override
    public void onClick(int totalClicks) {
        SwingUtilities.invokeLater(() -> clickCountLabel.setText(String.valueOf(totalClicks)));
    }

    @Override
    public void onGameOver(String taunt, int totalClicks, float seconds, float avgCps, float maxCps) {
        SwingUtilities.invokeLater(() -> {
            raceButton.setEnabled(false);
            raceButton.setText("GAME OVER");
            statusLabel.setText("GAME OVER");
            statusLabel.setForeground(Theme.GAME_OVER);
            String results = String.format(
                "  %s\n\n  Total Clicks:    %d\n  Total Seconds:   %.0f\n  Max Clicks/Sec:  %.1f\n  Avg Clicks/Sec:  %.2f",
                taunt, totalClicks, seconds, maxCps, avgCps);
            resultsArea.setText(results);

            lastTotalClicks = totalClicks;
            lastSeconds = seconds;
            lastAvgCps = avgCps;
            lastMaxCps = maxCps;

            showNamePrompt();
        });
    }

    // === Custom gradient background panel ===
    private static class GradientPanel extends JPanel {
        private final Color top, bottom;

        GradientPanel(Color top, Color bottom) {
            this.top = top;
            this.bottom = bottom;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // === Custom gradient button with rounded corners ===
    static class GradientButton extends JButton {
        private final Color gradTop, gradBottom;
        private boolean hovering;

        GradientButton(String text, Color top, Color bottom) {
            super(text);
            this.gradTop = top;
            this.gradBottom = bottom;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 18;
            RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc);

            Color t, b;
            if (!isEnabled()) {
                t = Theme.BTN_DISABLED_TOP;
                b = Theme.BTN_DISABLED_BOTTOM;
            } else if (hovering) {
                t = Theme.brighter(gradTop, 30);
                b = Theme.brighter(gradBottom, 20);
            } else {
                t = gradTop;
                b = gradBottom;
            }

            g2.setPaint(new GradientPaint(0, 0, t, 0, getHeight(), b));
            g2.fill(shape);

            if (isEnabled()) {
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 40), 0, getHeight() / 2, new Color(255, 255, 255, 0)));
                g2.fill(shape);
            }

            g2.setColor(isEnabled() ? new Color(255, 255, 255, 30) : new Color(255, 255, 255, 10));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(shape);

            g2.setFont(getFont());
            g2.setColor(isEnabled() ? Color.WHITE : new Color(100, 100, 120));
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }
}
