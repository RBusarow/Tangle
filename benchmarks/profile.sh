#!/bin/bash

ITERATIONS=10

# e.g., 10-25-2020_20:45:16
NOW="$(date +'%m-%d-%Y_%R')"

# relative path to where reports are created.  Each report creates its own sub-directory
REPORT_DIR=reports/profile_$NOW

# relative path to the root directory of this project
PROJECT_ROOT=../build/benchmark-project

# where the sandboxed install of Gradle will go
PROFILER_GRADLE_HOME=~/.gradle-profiler-user-home

gradle-profiler \
  --benchmark \
  --gradle-user-home $PROFILER_GRADLE_HOME \
  --scenario-file profiler.scenarios \
  --iterations $ITERATIONS \
  --project-dir "$PROJECT_ROOT" \
  --output-dir "$REPORT_DIR" \
  --no-daemon
