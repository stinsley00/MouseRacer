use eframe::egui::{self, Color32, Visuals};

pub const BG_TOP: Color32 = Color32::from_rgb(15, 15, 35);
pub const BG_BOTTOM: Color32 = Color32::from_rgb(30, 30, 60);
pub const ACCENT: Color32 = Color32::from_rgb(0, 200, 255);
pub const SURFACE_BORDER: Color32 = Color32::from_rgb(60, 60, 100);
pub const TEXT_PRIMARY: Color32 = Color32::from_rgb(230, 235, 255);
pub const TEXT_SECONDARY: Color32 = Color32::from_rgb(140, 150, 180);
pub const BTN_TOP: Color32 = Color32::from_rgb(0, 180, 230);
pub const BTN_BOTTOM: Color32 = Color32::from_rgb(0, 100, 180);
pub const BTN_DISABLED_TOP: Color32 = Color32::from_rgb(50, 50, 70);
pub const BTN_DISABLED_BOTTOM: Color32 = Color32::from_rgb(35, 35, 55);
pub const DANGER_TOP: Color32 = Color32::from_rgb(180, 50, 50);
pub const DANGER_BOTTOM: Color32 = Color32::from_rgb(120, 30, 30);
pub const SUCCESS: Color32 = Color32::from_rgb(0, 220, 120);
pub const RESULTS_BG: Color32 = Color32::from_rgb(20, 20, 45);
pub const GAME_OVER_COLOR: Color32 = Color32::from_rgb(255, 80, 80);

pub fn brighter(color: Color32, amount: u8) -> Color32 {
    Color32::from_rgb(
        color.r().saturating_add(amount),
        color.g().saturating_add(amount),
        color.b().saturating_add(amount),
    )
}

pub fn lerp_color(a: Color32, b: Color32, t: f32) -> Color32 {
    let t = t.clamp(0.0, 1.0);
    Color32::from_rgb(
        (a.r() as f32 + (b.r() as f32 - a.r() as f32) * t) as u8,
        (a.g() as f32 + (b.g() as f32 - a.g() as f32) * t) as u8,
        (a.b() as f32 + (b.b() as f32 - a.b() as f32) * t) as u8,
    )
}

pub fn configure_visuals(ctx: &egui::Context) {
    let mut visuals = Visuals::dark();
    visuals.panel_fill = BG_TOP;
    visuals.window_fill = Color32::from_rgb(20, 20, 45);
    visuals.override_text_color = Some(TEXT_PRIMARY);
    ctx.set_visuals(visuals);
}
