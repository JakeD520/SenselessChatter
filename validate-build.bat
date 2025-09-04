@echo off
echo 🔍 Validating Android Studio project...

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo ❌ gradlew.bat not found. Generate it with: gradle wrapper
    exit /b 1
)

echo ✅ Running gradle clean...
call gradlew.bat clean

echo ✅ Running gradle build (debug)...
call gradlew.bat assembleDebug

echo 🎉 Build completed! Check output above for any errors.
