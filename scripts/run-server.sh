#!/bin/bash
set -e

cd "$(dirname "$0")/.."

./gradlew :server:run