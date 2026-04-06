# Mouse Racer (Rust)

A Rust rewrite of the Java Swing Mouse Racer application, using **egui/eframe**.

## Build & Run

```bash
# Debug build
cargo build

# Release build (recommended for smooth gameplay)
cargo build --release

# Run
cargo run --release
```

## System Requirements

- **Rust** 1.70+ (via rustup)
- **Linux**: `libgtk-3-dev`, `libxcb-render0-dev`, `libxcb-shape0-dev`, `libxcb-xfixes0-dev`, `libxkbcommon-dev`, `libssl-dev`
- **macOS / Windows**: No extra dependencies

## Game Modes

- **Sprint (10s)** — Timer counts down from 10. Click as fast as you can.
- **Endurance (100s)** — Timer counts up to 100. Sustain your clicking.
- **Marathon (Infinite)** — Game ends when your CPS drops below 75% of your peak.

## Project Structure

```
src/
├── main.rs           — Entry point
├── game_engine.rs    — Core game logic (mode-independent)
└── ui/
    ├── mod.rs        — UI module root
    ├── app.rs        — Main eframe::App implementation
    ├── theme.rs      — Color constants and styling
    ├── widgets.rs    — Custom gradient buttons, radio buttons, backgrounds
    └── about.rs      — About dialog window
```
