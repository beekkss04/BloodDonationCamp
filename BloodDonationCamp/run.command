#!/bin/bash
# Double-click on Mac to compile and run the project
cd "$(dirname "$0")"
javac *.java && java MainGUI
