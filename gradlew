#!/usr/bin/env sh
# Minimal Gradle wrapper launcher (requires gradle-wrapper.jar).
# If missing, regenerate via: gradle wrapper
DIR="$(cd "$(dirname "$0")" && pwd)"
java -jar "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
