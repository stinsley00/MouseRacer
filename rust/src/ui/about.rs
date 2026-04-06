use eframe::egui;

use super::theme;

pub fn show_about_window(ctx: &egui::Context, open: &mut bool) {
    let mut is_open = *open;
    egui::Window::new("About: Mouse Racer 1.0")
        .open(&mut is_open)
        .resizable(false)
        .collapsible(false)
        .default_width(400.0)
        .show(ctx, |ui| {
            ui.vertical(|ui| {
                ui.heading(
                    egui::RichText::new("Mouse Racer")
                        .color(theme::ACCENT)
                        .strong()
                        .size(22.0),
                );
                ui.label("A simple click-racing desktop application.");
                ui.add_space(8.0);
                ui.horizontal(|ui| {
                    ui.label(egui::RichText::new("Product Version:").strong());
                    ui.label("1.0 (Rust)");
                });
                ui.horizontal(|ui| {
                    ui.label(egui::RichText::new("Vendor:").strong());
                    ui.label("Mouse Racer Team");
                });
                ui.horizontal(|ui| {
                    ui.label(egui::RichText::new("Framework:").strong());
                    ui.label("egui / eframe");
                });
                ui.add_space(12.0);
            });
        });
    *open = is_open;
}
