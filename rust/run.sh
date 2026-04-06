#!/bin/bash
set -e

echo "Building Mouse Racer (Rust)..."
cargo build --release

echo "Running Mouse Racer..."
cargo run --release
