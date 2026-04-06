#!/bin/bash

# Exit on any error
set -e

# Directory for compiled classes and resources
OUT_DIR="out"

echo "Compiling Mouse Racer..."
mkdir -p "$OUT_DIR"
javac -d "$OUT_DIR" src/mouseracer/*.java

echo "Copying resources to the output directory..."
# Copy resources while maintaining package structure for resources to be found at runtime
mkdir -p "$OUT_DIR/mouseracer/resources"
cp -r src/mouseracer/resources/* "$OUT_DIR/mouseracer/resources/"

echo "Successfully compiled! Running Mouse Racer..."
java -cp "$OUT_DIR" mouseracer.MouseRacerApp
