# Mouse Racer

Mouse Racer is a simple yet addictive Java Swing application designed to test and improve your clicking speed. Challenge yourself across three distinct game modes and see if you can achieve "hyperclick" status!

## Features

- **Three Game Modes**:
    - **Sprint Mode (10s)**: A high-intensity 10-second dash to get as many clicks as possible.
    - **Endurance Mode (100s)**: A test of stamina over a 100-second period.
    - **Marathon Mode (Infinite)**: Keep clicking! The game ends if your clicking speed drops below 75% of your peak performance.
- **Real-time Statistics**: Track your total clicks and elapsed time during the race.
- **Detailed Results**: After each game, see your total clicks, maximum clicks per second (CPS), and average CPS, along with a personalized taunt based on your performance.
- **Modular Architecture**: The game logic is cleanly separated from the UI using a listener pattern, making the code easy to maintain and extend.

## How to Run

### Prerequisites

- **Java Development Kit (JDK) 8 or higher**: Ensure you have a JDK installed and the `java` and `javac` commands are available in your path.

### Building and Running

You can use the provided `run.sh` script to compile and launch the application in one step:

```bash
./run.sh
```

Alternatively, you can manually build and run the project:

1.  **Compile the project**:
    Navigate to the project root and compile the source files.
    ```bash
    javac -d out src/mouseracer/*.java
    ```

2.  **Copy the resources**:
    Copy the resources to the output directory to ensure the GUI works correctly.
    ```bash
    mkdir -p out/mouseracer/resources
    cp -r src/mouseracer/resources/* out/mouseracer/resources/
    ```

3.  **Run the application**:
    ```bash
    java -cp out mouseracer.MouseRacerApp
    ```

*Note: Since this project was originally built using an IDE-specific structure (like NetBeans/IntelliJ), it's easiest to open the project folder in your favorite IDE to run it directly.*

## Testing

```bash
javac -d out src/mouseracer/*.java
java -cp out mouseracer.TestRunner
```

Runs 35 unit tests covering:
- **GameEngine** — all 3 modes, tick timing, CPS tracking, taunts, edge cases
- **HighScoreBoard** — in-memory operations, JSON persistence, corrupt file handling, special characters

The Java engine is async (uses `ScheduledExecutorService`), so tick-based tests use `CountDownLatch` to synchronize with the background timer. The 100-second Endurance full-game test is skipped by default for fast runs.

## Architecture Information

The project follows a modular design:
- **`GameEngine.java`**: Contains all the core game logic, high-precision background timer using `ScheduledExecutorService`, and thread-safe click counters. It is UI-agnostic and communicates via the `GameListener` interface.
- **`MouseRacerView.java`**: The main Swing-based UI. It implements `GameListener` to update labels and display results. It handles both immediate click feedback and periodic timer updates asynchronously on the Event Dispatch Thread (EDT).
- **`MouseRacerApp.java`**: The entry point for the application, initializing the main frame and application lifecycle.

## CI/CD & Releases

This project uses **GitHub Actions** to build and test on Linux, macOS, and Windows on every push and pull request.

### Downloading builds

1. Go to the **Actions** tab in the GitHub repository.
2. Select a completed workflow run.
3. Download the JAR from the **Artifacts** section at the bottom of the run.

### Creating a release

To publish a GitHub Release with downloadable binaries:

```bash
git tag v1.0.0
git push origin v1.0.0
```

This triggers the release job, which attaches the JAR and native Rust binaries to a new GitHub Release. Users can then download them from the **Releases** page.

## About

Mouse Racer was modularized and simplified to improve code quality while preserving the original high-speed clicking action.
