use std::time::Instant;

use eframe::egui;

use crate::game_engine::{GameEngine, GameEvent, GameMode};
use crate::high_scores::HighScoreBoard;

use super::{about, high_scores_ui, theme, widgets};

#[derive(PartialEq, Eq)]
enum AppState {
    Splash,
    Idle,
    Racing,
    NamePrompt,
    GameOver,
}

struct LastGameData {
    mode: GameMode,
    total_clicks: u32,
    seconds: f32,
    avg_cps: f32,
    max_cps: f32,
}

pub struct MouseRacerApp {
    engine: GameEngine,
    state: AppState,
    selected_mode: Option<GameMode>,
    // Display state
    status_text: String,
    status_color: egui::Color32,
    race_button_text: String,
    timer_display: String,
    clicks_display: String,
    cps_display: String,
    results_text: String,
    // High scores
    high_score_board: HighScoreBoard,
    last_game: Option<LastGameData>,
    player_name: String,
    // Splash
    splash_start: Instant,
    // UI flags
    show_about: bool,
    show_instructions: bool,
    show_high_scores: bool,
    high_scores_tab: GameMode,
}

impl MouseRacerApp {
    pub fn new(_cc: &eframe::CreationContext<'_>) -> Self {
        theme::configure_visuals(&_cc.egui_ctx);
        Self {
            engine: GameEngine::new(),
            state: AppState::Splash,
            selected_mode: None,
            status_text: "MOUSE RACER".into(),
            status_color: theme::ACCENT,
            race_button_text: "CLICK TO RACE!".into(),
            timer_display: "0".into(),
            clicks_display: "0".into(),
            cps_display: "0.0".into(),
            results_text: String::new(),
            high_score_board: HighScoreBoard::load(),
            last_game: None,
            player_name: String::new(),
            splash_start: Instant::now(),
            show_about: false,
            show_instructions: false,
            show_high_scores: false,
            high_scores_tab: GameMode::Sprint,
        }
    }

    fn start_game(&mut self) {
        let mode = self.selected_mode.unwrap_or(GameMode::Sprint);
        self.status_text = "RACING!".into();
        self.status_color = theme::SUCCESS;
        self.race_button_text = "CLICK!".into();
        self.state = AppState::Racing;

        let event = self.engine.start(mode);
        self.process_event(event);
    }

    fn reset_ui(&mut self) {
        self.engine.stop();
        self.state = AppState::Idle;
        self.selected_mode = None;
        self.status_text = "MOUSE RACER".into();
        self.status_color = theme::ACCENT;
        self.race_button_text = "CLICK TO RACE!".into();
        self.timer_display = "0".into();
        self.clicks_display = "0".into();
        self.cps_display = "0.0".into();
        self.results_text.clear();
    }

    fn show_name_prompt(&mut self, ctx: &egui::Context) {
        egui::Window::new("Game Over — Enter Your Name")
            .collapsible(false)
            .resizable(false)
            .anchor(egui::Align2::CENTER_CENTER, [0.0, 0.0])
            .default_width(350.0)
            .show(ctx, |ui| {
                ui.add_space(8.0);
                ui.label(
                    egui::RichText::new("Save your score to the leaderboard!")
                        .color(theme::TEXT_PRIMARY)
                        .size(15.0),
                );
                ui.add_space(12.0);

                let response = ui.add(
                    egui::TextEdit::singleline(&mut self.player_name)
                        .hint_text("Your name...")
                        .desired_width(300.0)
                        .font(egui::FontId::proportional(16.0)),
                );

                // Auto-focus the text field on first frame
                if response.gained_focus() || self.player_name.is_empty() {
                    response.request_focus();
                }

                let enter_pressed = ui.input(|i| i.key_pressed(egui::Key::Enter));

                ui.add_space(10.0);
                ui.horizontal(|ui| {
                    if ui.button("Submit").clicked() || enter_pressed {
                        self.submit_score();
                    }
                    if ui.button("Skip").clicked() {
                        self.state = AppState::GameOver;
                        self.last_game = None;
                    }
                });
            });
    }

    fn submit_score(&mut self) {
        if let Some(game) = self.last_game.take() {
            let name = if self.player_name.trim().is_empty() {
                "Anonymous".to_string()
            } else {
                self.player_name.trim().to_string()
            };
            let entry = HighScoreBoard::new_entry(
                name,
                game.mode,
                game.total_clicks,
                game.seconds,
                game.avg_cps,
                game.max_cps,
            );
            self.high_score_board.add_entry(entry);
            let _ = self.high_score_board.save();
            self.high_scores_tab = game.mode;
            self.show_high_scores = true;
        }
        self.state = AppState::GameOver;
    }

    fn process_event(&mut self, event: GameEvent) {
        match event {
            GameEvent::Tick {
                seconds,
                total_clicks,
                current_cps,
            } => {
                self.timer_display = format!("{:.0}", seconds);
                self.clicks_display = total_clicks.to_string();
                self.cps_display = format!("{:.1}", current_cps);
            }
            GameEvent::Click { total_clicks } => {
                self.clicks_display = total_clicks.to_string();
            }
            GameEvent::GameOver {
                taunt,
                total_clicks,
                seconds,
                avg_cps,
                max_cps,
            } => {
                self.race_button_text = "GAME OVER".into();
                self.status_text = "GAME OVER".into();
                self.status_color = theme::GAME_OVER_COLOR;
                self.results_text = format!(
                    "  {}\n\n  Total Clicks:    {}\n  Total Seconds:   {:.0}\n  Max Clicks/Sec:  {:.1}\n  Avg Clicks/Sec:  {:.2}",
                    taunt, total_clicks, seconds, max_cps, avg_cps
                );
                self.last_game = Some(LastGameData {
                    mode: self.selected_mode.unwrap_or(GameMode::Sprint),
                    total_clicks,
                    seconds,
                    avg_cps,
                    max_cps,
                });
                self.player_name.clear();
                self.state = AppState::NamePrompt;
            }
        }
    }
}

impl eframe::App for MouseRacerApp {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        // Splash screen
        if self.state == AppState::Splash {
            let elapsed = self.splash_start.elapsed().as_secs_f32();
            let dismiss = elapsed > 3.0 || ctx.input(|i| {
                i.pointer.any_click()
                    || i.key_pressed(egui::Key::Enter)
                    || i.key_pressed(egui::Key::Space)
                    || i.key_pressed(egui::Key::Escape)
            });
            if dismiss {
                self.state = AppState::Idle;
            } else {
                egui::CentralPanel::default().show(ctx, |ui| {
                    widgets::paint_gradient_background(ui);
                    let rect = ui.max_rect();

                    // Fade in: 0..1 over the first second
                    let alpha = (elapsed / 0.8).min(1.0);
                    let alpha_byte = (alpha * 255.0) as u8;

                    let painter = ui.painter();

                    // Title
                    painter.text(
                        egui::pos2(rect.center().x, rect.center().y - 60.0),
                        egui::Align2::CENTER_CENTER,
                        "MOUSE RACER",
                        egui::FontId::proportional(64.0),
                        egui::Color32::from_rgba_premultiplied(
                            theme::ACCENT.r(),
                            theme::ACCENT.g(),
                            theme::ACCENT.b(),
                            alpha_byte,
                        ),
                    );

                    // Subtitle
                    painter.text(
                        egui::pos2(rect.center().x, rect.center().y + 10.0),
                        egui::Align2::CENTER_CENTER,
                        "How fast can you click?",
                        egui::FontId::proportional(22.0),
                        egui::Color32::from_rgba_premultiplied(
                            theme::TEXT_SECONDARY.r(),
                            theme::TEXT_SECONDARY.g(),
                            theme::TEXT_SECONDARY.b(),
                            alpha_byte,
                        ),
                    );

                    // Version
                    painter.text(
                        egui::pos2(rect.center().x, rect.center().y + 55.0),
                        egui::Align2::CENTER_CENTER,
                        "v1.0 — Rust Edition",
                        egui::FontId::proportional(14.0),
                        egui::Color32::from_rgba_premultiplied(
                            theme::TEXT_SECONDARY.r(),
                            theme::TEXT_SECONDARY.g(),
                            theme::TEXT_SECONDARY.b(),
                            (alpha_byte as f32 * 0.6) as u8,
                        ),
                    );

                    // "Click to continue" hint (pulse after 1s)
                    if elapsed > 1.0 {
                        let pulse = ((elapsed - 1.0) * 2.0).sin() * 0.3 + 0.7;
                        let hint_alpha = (pulse * 255.0) as u8;
                        painter.text(
                            egui::pos2(rect.center().x, rect.max.y - 40.0),
                            egui::Align2::CENTER_CENTER,
                            "Click anywhere to continue",
                            egui::FontId::proportional(15.0),
                            egui::Color32::from_rgba_premultiplied(
                                theme::TEXT_PRIMARY.r(),
                                theme::TEXT_PRIMARY.g(),
                                theme::TEXT_PRIMARY.b(),
                                hint_alpha,
                            ),
                        );
                    }
                });
                ctx.request_repaint();
                return;
            }
        }

        // Process engine tick
        if let Some(event) = self.engine.tick() {
            self.process_event(event);
        }

        // Keep repainting while game is running
        if self.engine.is_running() {
            ctx.request_repaint();
        }

        // Menu bar
        egui::TopBottomPanel::top("menu_bar").show(ctx, |ui| {
            ui.visuals_mut().panel_fill = theme::BG_TOP;
            egui::menu::bar(ui, |ui| {
                ui.menu_button(
                    egui::RichText::new("File").color(theme::TEXT_SECONDARY).size(13.0),
                    |ui| {
                        if ui.button("High Scores").clicked() {
                            self.show_high_scores = true;
                            ui.close_menu();
                        }
                        ui.separator();
                        if ui.button("Exit").clicked() {
                            ctx.send_viewport_cmd(egui::ViewportCommand::Close);
                        }
                    },
                );
                ui.menu_button(
                    egui::RichText::new("Help").color(theme::TEXT_SECONDARY).size(13.0),
                    |ui| {
                        if ui.button("Instructions").clicked() {
                            self.show_instructions = true;
                            ui.close_menu();
                        }
                        if ui.button("About").clicked() {
                            self.show_about = true;
                            ui.close_menu();
                        }
                    },
                );
            });
        });

        // Instructions window
        if self.show_instructions {
            show_instructions_window(ctx, &mut self.show_instructions);
        }

        // About window
        if self.show_about {
            about::show_about_window(ctx, &mut self.show_about);
        }

        // High scores window
        if self.show_high_scores {
            high_scores_ui::show_high_scores_window(
                ctx,
                &mut self.show_high_scores,
                &self.high_score_board,
                &mut self.high_scores_tab,
            );
        }

        // Name prompt modal after game over
        if self.state == AppState::NamePrompt {
            self.show_name_prompt(ctx);
        }

        // Main content
        egui::CentralPanel::default().show(ctx, |ui| {
            widgets::paint_gradient_background(ui);

            ui.add_space(20.0);

            // Top row: title + stats
            ui.horizontal(|ui| {
                // Title (left side)
                ui.with_layout(egui::Layout::left_to_right(egui::Align::Center), |ui| {
                    ui.label(
                        egui::RichText::new(&self.status_text)
                            .color(self.status_color)
                            .strong()
                            .size(36.0),
                    );
                });

                // Stats (right side)
                ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                    ui.horizontal(|ui| {
                        ui.spacing_mut().item_spacing.x = 10.0;

                        // CPS
                        ui.label(
                            egui::RichText::new(&self.cps_display)
                                .color(theme::SUCCESS)
                                .strong()
                                .size(28.0),
                        );
                        ui.label(
                            egui::RichText::new("CPS")
                                .color(theme::TEXT_SECONDARY)
                                .strong()
                                .size(16.0),
                        );
                        ui.add_space(10.0);

                        // Clicks
                        ui.label(
                            egui::RichText::new(&self.clicks_display)
                                .color(theme::TEXT_PRIMARY)
                                .strong()
                                .size(28.0),
                        );
                        ui.label(
                            egui::RichText::new("CLICKS")
                                .color(theme::TEXT_SECONDARY)
                                .strong()
                                .size(16.0),
                        );
                        ui.add_space(10.0);

                        // Time
                        ui.label(
                            egui::RichText::new(&self.timer_display)
                                .color(theme::TEXT_PRIMARY)
                                .strong()
                                .size(28.0),
                        );
                        ui.label(
                            egui::RichText::new("TIME")
                                .color(theme::TEXT_SECONDARY)
                                .strong()
                                .size(16.0),
                        );
                    });
                });
            });

            ui.add_space(15.0);
            ui.separator();
            ui.add_space(15.0);

            // Main content: race button (left) + right panel
            let available = ui.available_size();
            ui.horizontal(|ui| {
                // Left: Race button
                let btn_width = available.x * 0.55;
                let btn_height = available.y - 60.0;
                ui.vertical(|ui| {
                    let race_enabled = self.state == AppState::Idle && self.selected_mode.is_some()
                        || self.state == AppState::Racing;
                    let response = widgets::gradient_button(
                        ui,
                        &self.race_button_text,
                        theme::BTN_TOP,
                        theme::BTN_BOTTOM,
                        race_enabled,
                        32.0,
                        Some(egui::vec2(btn_width, btn_height.max(200.0))),
                    );

                    if race_enabled && response.clicked() {
                        if self.state == AppState::Idle {
                            self.start_game();
                        }
                        if let Some(event) = self.engine.register_click() {
                            self.process_event(event);
                        }
                        ctx.request_repaint();
                    }
                });

                ui.add_space(15.0);

                // Right panel
                ui.vertical(|ui| {
                    // Results area
                    let results_height = btn_height * 0.4;
                    egui::Frame::new()
                        .fill(theme::RESULTS_BG)
                        .stroke(egui::Stroke::new(1.0, theme::SURFACE_BORDER))
                        .corner_radius(6.0)
                        .inner_margin(egui::Margin::same(12))
                        .show(ui, |ui| {
                            ui.set_min_size(egui::vec2(ui.available_width(), results_height.max(80.0)));
                            egui::ScrollArea::vertical().show(ui, |ui| {
                                ui.label(
                                    egui::RichText::new(&self.results_text)
                                        .color(theme::TEXT_PRIMARY)
                                        .monospace()
                                        .size(15.0),
                                );
                            });
                        });

                    ui.add_space(12.0);

                    // Mode selection
                    ui.label(
                        egui::RichText::new("SELECT MODE")
                            .color(theme::TEXT_SECONDARY)
                            .strong()
                            .size(13.0),
                    );
                    ui.add_space(6.0);

                    let mode_enabled = self.state == AppState::Idle;

                    let sprint_resp = widgets::styled_radio(
                        ui,
                        "Sprint (10s)",
                        self.selected_mode == Some(GameMode::Sprint),
                        mode_enabled,
                    );
                    if mode_enabled && sprint_resp.clicked() {
                        self.selected_mode = Some(GameMode::Sprint);
                    }

                    let endurance_resp = widgets::styled_radio(
                        ui,
                        "Endurance (100s)",
                        self.selected_mode == Some(GameMode::Endurance),
                        mode_enabled,
                    );
                    if mode_enabled && endurance_resp.clicked() {
                        self.selected_mode = Some(GameMode::Endurance);
                    }

                    let marathon_resp = widgets::styled_radio(
                        ui,
                        "Marathon (Infinite)",
                        self.selected_mode == Some(GameMode::Marathon),
                        mode_enabled,
                    );
                    if mode_enabled && marathon_resp.clicked() {
                        self.selected_mode = Some(GameMode::Marathon);
                    }

                    ui.add_space(14.0);

                    // Play Again button
                    let play_again_enabled = self.state != AppState::Idle;
                    let play_resp = widgets::gradient_button(
                        ui,
                        "Play Again",
                        theme::BTN_TOP,
                        theme::BTN_BOTTOM,
                        play_again_enabled,
                        18.0,
                        Some(egui::vec2(ui.available_width(), 45.0)),
                    );
                    if play_again_enabled && play_resp.clicked() {
                        self.reset_ui();
                    }

                    ui.add_space(8.0);

                    // Exit button
                    let exit_resp = widgets::gradient_button(
                        ui,
                        "Exit",
                        theme::DANGER_TOP,
                        theme::DANGER_BOTTOM,
                        true,
                        18.0,
                        Some(egui::vec2(ui.available_width(), 45.0)),
                    );
                    if exit_resp.clicked() {
                        ctx.send_viewport_cmd(egui::ViewportCommand::Close);
                    }
                });
            });

            // Bottom bar
            ui.with_layout(egui::Layout::bottom_up(egui::Align::LEFT), |ui| {
                ui.horizontal(|ui| {
                    ui.label(
                        egui::RichText::new("Mouse Racer v1.0 (Rust)")
                            .color(theme::TEXT_SECONDARY)
                            .size(12.0),
                    );
                });
            });
        });
    }
}

fn show_instructions_window(ctx: &egui::Context, open: &mut bool) {
    let mut is_open = *open;
    egui::Window::new("Instructions")
        .open(&mut is_open)
        .resizable(false)
        .collapsible(false)
        .default_width(450.0)
        .show(ctx, |ui| {
            ui.vertical(|ui| {
                ui.heading(
                    egui::RichText::new("How to Play")
                        .color(theme::ACCENT)
                        .strong()
                        .size(20.0),
                );
                ui.add_space(8.0);

                ui.label("1. Select a game mode from the right panel.");
                ui.label("2. Click the big button to start the race.");
                ui.label("3. Click as fast as you can!");
                ui.label("4. Your results will appear when the game ends.");

                ui.add_space(12.0);
                ui.heading(
                    egui::RichText::new("Game Modes")
                        .color(theme::ACCENT)
                        .strong()
                        .size(18.0),
                );
                ui.add_space(6.0);

                ui.label(
                    egui::RichText::new("Sprint (10s)")
                        .color(theme::SUCCESS)
                        .strong(),
                );
                ui.label("  Timer counts down from 10. Click as many times as you can.");

                ui.add_space(4.0);
                ui.label(
                    egui::RichText::new("Endurance (100s)")
                        .color(theme::SUCCESS)
                        .strong(),
                );
                ui.label("  Timer counts up to 100. Test your sustained clicking stamina.");

                ui.add_space(4.0);
                ui.label(
                    egui::RichText::new("Marathon (Infinite)")
                        .color(theme::SUCCESS)
                        .strong(),
                );
                ui.label("  No time limit. The game ends when your clicks per second");
                ui.label("  drops below 75% of your peak. Keep up the pace!");

                ui.add_space(12.0);
                ui.heading(
                    egui::RichText::new("Stats")
                        .color(theme::ACCENT)
                        .strong()
                        .size(18.0),
                );
                ui.add_space(6.0);
                ui.label(
                    egui::RichText::new("TIME").color(theme::TEXT_SECONDARY).strong(),
                );
                ui.label("  Elapsed or remaining seconds.");
                ui.label(
                    egui::RichText::new("CLICKS").color(theme::TEXT_SECONDARY).strong(),
                );
                ui.label("  Total clicks so far.");
                ui.label(
                    egui::RichText::new("CPS").color(theme::TEXT_SECONDARY).strong(),
                );
                ui.label("  Clicks per second (updated each second).");
            });
        });
    *open = is_open;
}
