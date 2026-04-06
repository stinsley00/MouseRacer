# Mouse Racer

A desktop click-speed game with three game modes, real-time stats, and a persistent leaderboard. Built twice: once in **Java Swing** and once in **Rust (egui/eframe)** — both share identical game mechanics.

## Game Modes

| Mode | Duration | Rule |
|------|----------|------|
| **Sprint** | 10 seconds | Click as fast as you can before time runs out |
| **Endurance** | 100 seconds | Sustain your clicking over a long session |
| **Marathon** | Infinite | Game ends when your CPS drops below 75% of your peak |

Each round tracks total clicks, elapsed time, max CPS, and average CPS. A performance-based taunt is displayed at the end.

## Project Structure

```
.
├── java/          # Java Swing implementation (JDK 8+)
├── rust/          # Rust egui/eframe implementation (Rust 1.70+)
└── .github/
    └── workflows/
        └── build.yml   # CI/CD: builds, tests, and releases for both
```

## Quick Start

### Java

Requires **JDK 8+** with `java` and `javac` on your PATH.

```bash
cd java
./run.sh
```

Or manually:

```bash
cd java
mkdir -p out
javac -d out src/mouseracer/*.java
mkdir -p out/mouseracer/resources
cp -r src/mouseracer/resources/* out/mouseracer/resources/
java -cp out mouseracer.MouseRacerApp
```

### Rust

Requires **Rust 1.70+** (via [rustup](https://rustup.rs)).

On Linux, install these system dependencies first:

```bash
sudo apt-get install libgtk-3-dev libxcb-render0-dev libxcb-shape0-dev \
  libxcb-xfixes0-dev libxkbcommon-dev libssl-dev
```

macOS and Windows need no extra dependencies.

```bash
cd rust
cargo run --release
```

## Testing

### Java (35 unit tests)

```bash
cd java
javac -d out src/mouseracer/*.java
java -cp out mouseracer.TestRunner
```

Covers GameEngine (all modes, timing, CPS, taunts) and HighScoreBoard (persistence, JSON handling, corruption recovery).

### Rust

```bash
cd rust
cargo test --release
```

## Architecture

Both implementations share the same design:

- **GameEngine** — mode-independent game logic with tick-based timing and CPS tracking. Communicates state changes via a listener/observer pattern.
- **UI layer** — renders the game state and forwards clicks to the engine. Java uses Swing on the EDT; Rust uses egui immediate-mode rendering.
- **HighScoreBoard** — JSON-backed leaderboard persisted to the user's data directory, with per-mode rankings.

See [java/README.md](java/README.md) and [rust/README.md](rust/README.md) for implementation-specific details.

## CI/CD & Releases

GitHub Actions builds and tests both implementations on Linux, macOS, and Windows on every push and PR.

To create a release with downloadable binaries:

```bash
git tag v1.0.0
git push origin v1.0.0
```

This attaches native Rust binaries (Linux, macOS, Windows) and the Java JAR to a GitHub Release.

## License

See repository for license details.
