use eframe::egui;

use crate::game_engine::GameMode;
use crate::high_scores::HighScoreBoard;

use super::theme;

pub fn show_high_scores_window(
    ctx: &egui::Context,
    open: &mut bool,
    board: &HighScoreBoard,
    selected_tab: &mut GameMode,
) {
    let mut is_open = *open;
    egui::Window::new("High Scores")
        .open(&mut is_open)
        .resizable(true)
        .collapsible(false)
        .default_width(580.0)
        .default_height(400.0)
        .show(ctx, |ui| {
            // Mode tabs
            ui.horizontal(|ui| {
                for (mode, label) in [
                    (GameMode::Sprint, "Sprint"),
                    (GameMode::Endurance, "Endurance"),
                    (GameMode::Marathon, "Marathon"),
                ] {
                    let is_selected = *selected_tab == mode;
                    let text = if is_selected {
                        egui::RichText::new(label)
                            .color(theme::ACCENT)
                            .strong()
                            .size(15.0)
                    } else {
                        egui::RichText::new(label)
                            .color(theme::TEXT_SECONDARY)
                            .size(15.0)
                    };
                    if ui.selectable_label(is_selected, text).clicked() {
                        *selected_tab = mode;
                    }
                }
            });

            ui.separator();
            ui.add_space(4.0);

            let entries = board.top_n(*selected_tab, 10);

            if entries.is_empty() {
                ui.add_space(20.0);
                ui.label(
                    egui::RichText::new("No scores recorded yet. Play a game!")
                        .color(theme::TEXT_SECONDARY)
                        .size(15.0),
                );
                return;
            }

            // Table header
            egui::Grid::new("high_scores_grid")
                .num_columns(7)
                .spacing([12.0, 6.0])
                .striped(true)
                .show(ui, |ui| {
                    let header = |ui: &mut egui::Ui, text: &str| {
                        ui.label(
                            egui::RichText::new(text)
                                .color(theme::TEXT_SECONDARY)
                                .strong()
                                .size(13.0),
                        );
                    };
                    header(ui, "#");
                    header(ui, "Name");
                    header(ui, "Clicks");
                    header(ui, "Time");
                    header(ui, "Avg CPS");
                    header(ui, "Max CPS");
                    header(ui, "Date");
                    ui.end_row();

                    for (i, entry) in entries.iter().enumerate() {
                        let rank_color = match i {
                            0 => egui::Color32::from_rgb(255, 215, 0),   // gold
                            1 => egui::Color32::from_rgb(192, 192, 192), // silver
                            2 => egui::Color32::from_rgb(205, 127, 50),  // bronze
                            _ => theme::TEXT_PRIMARY,
                        };

                        ui.label(
                            egui::RichText::new(format!("{}", i + 1))
                                .color(rank_color)
                                .strong()
                                .size(14.0),
                        );
                        ui.label(
                            egui::RichText::new(&entry.name)
                                .color(theme::TEXT_PRIMARY)
                                .size(14.0),
                        );
                        ui.label(
                            egui::RichText::new(entry.total_clicks.to_string())
                                .color(theme::ACCENT)
                                .strong()
                                .size(14.0),
                        );
                        ui.label(
                            egui::RichText::new(format!("{:.0}s", entry.seconds))
                                .color(theme::TEXT_PRIMARY)
                                .size(14.0),
                        );
                        ui.label(
                            egui::RichText::new(format!("{:.2}", entry.avg_cps))
                                .color(theme::TEXT_PRIMARY)
                                .size(14.0),
                        );
                        ui.label(
                            egui::RichText::new(format!("{:.1}", entry.max_cps))
                                .color(theme::SUCCESS)
                                .size(14.0),
                        );
                        // Show just the date portion for compactness
                        let date_short = entry.date.split('T').next().unwrap_or(&entry.date);
                        ui.label(
                            egui::RichText::new(date_short)
                                .color(theme::TEXT_SECONDARY)
                                .size(12.0),
                        );
                        ui.end_row();
                    }
                });
        });
    *open = is_open;
}
