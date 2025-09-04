#!/usr/bin/env bash
# Quick Android build validation script

echo "🔍 Validating Android Studio project..."

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found. Generate it with: gradle wrapper"
    exit 1
fi

echo "✅ Running gradle clean..."
./gradlew clean

echo "✅ Running gradle build (debug)..."
./gradlew assembleDebug

echo "🎉 Build completed! Check output above for any errors."
