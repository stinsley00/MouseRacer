mod game_engine;
mod high_scores;
mod ui;

use ui::app::MouseRacerApp;

fn main() -> eframe::Result<()> {
    let options = eframe::NativeOptions {
        viewport: eframe::egui::ViewportBuilder::default()
            .with_inner_size([1100.0, 700.0])
            .with_min_inner_size([800.0, 500.0])
            .with_title("Mouse Racer"),
        ..Default::default()
    };

    eframe::run_native(
        "Mouse Racer",
        options,
        Box::new(|cc| Ok(Box::new(MouseRacerApp::new(cc)))),
    )
}
