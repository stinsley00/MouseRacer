use eframe::egui::{self, Color32, CornerRadius, FontId, Rect, Response, Sense, StrokeKind, Ui, Vec2};

use super::theme::{self, BTN_DISABLED_BOTTOM, BTN_DISABLED_TOP};

pub fn gradient_button(
    ui: &mut Ui,
    text: &str,
    top_color: Color32,
    bottom_color: Color32,
    enabled: bool,
    font_size: f32,
    desired_size: Option<Vec2>,
) -> Response {
    let size = desired_size.unwrap_or(Vec2::new(ui.available_width(), 50.0));
    let (rect, response) = ui.allocate_exact_size(size, Sense::click());

    if ui.is_rect_visible(rect) {
        let painter = ui.painter();
        let rounding = CornerRadius::same(9);
        let hovered = enabled && response.hovered();

        let (t, b) = if !enabled {
            (BTN_DISABLED_TOP, BTN_DISABLED_BOTTOM)
        } else if hovered {
            (theme::brighter(top_color, 30), theme::brighter(bottom_color, 20))
        } else {
            (top_color, bottom_color)
        };

        // Draw gradient via horizontal strips
        let steps = 20;
        for i in 0..steps {
            let frac_top = i as f32 / steps as f32;
            let frac_bot = (i + 1) as f32 / steps as f32;
            let strip = Rect::from_min_max(
                egui::pos2(rect.min.x, rect.min.y + rect.height() * frac_top),
                egui::pos2(rect.max.x, rect.min.y + rect.height() * frac_bot),
            );
            let color = theme::lerp_color(t, b, (frac_top + frac_bot) / 2.0);
            painter.rect_filled(strip, CornerRadius::ZERO, color);
        }

        // Draw a rounded border on top
        painter.rect_stroke(
            rect,
            rounding,
            egui::Stroke::new(
                1.5,
                if enabled {
                    Color32::from_white_alpha(30)
                } else {
                    Color32::from_white_alpha(10)
                },
            ),
            StrokeKind::Outside,
        );

        // Highlight on top half when enabled
        if enabled {
            let top_half = Rect::from_min_max(rect.min, egui::pos2(rect.max.x, rect.center().y));
            painter.rect_filled(top_half, CornerRadius::ZERO, Color32::from_white_alpha(15));
        }

        // Text
        let text_color = if enabled {
            Color32::WHITE
        } else {
            Color32::from_rgb(100, 100, 120)
        };
        painter.text(
            rect.center(),
            egui::Align2::CENTER_CENTER,
            text,
            FontId::proportional(font_size),
            text_color,
        );
    }

    if enabled {
        response
    } else {
        // Return a response that never reports clicked
        response
    }
}

pub fn styled_radio(ui: &mut Ui, text: &str, selected: bool, enabled: bool) -> Response {
    let desired_size = Vec2::new(ui.available_width(), 28.0);
    let (rect, response) = ui.allocate_exact_size(desired_size, Sense::click());

    if ui.is_rect_visible(rect) {
        let painter = ui.painter();
        let circle_radius = 8.0;
        let circle_center = egui::pos2(rect.min.x + circle_radius + 2.0, rect.center().y);

        // Outer circle
        let outer_color = if selected {
            theme::ACCENT
        } else {
            theme::SURFACE_BORDER
        };
        painter.circle_filled(circle_center, circle_radius, outer_color);

        // Inner circle (background)
        painter.circle_filled(circle_center, circle_radius - 2.0, Color32::from_rgb(20, 20, 45));

        // Selected dot
        if selected {
            painter.circle_filled(circle_center, circle_radius - 4.0, theme::ACCENT);
        }

        // Text
        let text_color = if enabled {
            theme::TEXT_PRIMARY
        } else {
            theme::TEXT_SECONDARY
        };
        painter.text(
            egui::pos2(rect.min.x + circle_radius * 2.0 + 10.0, rect.center().y),
            egui::Align2::LEFT_CENTER,
            text,
            FontId::proportional(15.0),
            text_color,
        );
    }

    response
}

pub fn paint_gradient_background(ui: &mut Ui) {
    let rect = ui.max_rect();
    let painter = ui.painter();
    let steps = 40;
    for i in 0..steps {
        let frac_top = i as f32 / steps as f32;
        let frac_bot = (i + 1) as f32 / steps as f32;
        let strip = Rect::from_min_max(
            egui::pos2(rect.min.x, rect.min.y + rect.height() * frac_top),
            egui::pos2(rect.max.x, rect.min.y + rect.height() * frac_bot),
        );
        let color = theme::lerp_color(theme::BG_TOP, theme::BG_BOTTOM, (frac_top + frac_bot) / 2.0);
        painter.rect_filled(strip, CornerRadius::ZERO, color);
    }
}
