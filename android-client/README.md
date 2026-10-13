# Qwenton Apex Android Client

Native Android application for **Qwenton Apex**, a privacy-first assistant designed to interface with the local Termux Python server.

## Overview
- **Application ID / Namespace**: `ai.qwenton.apex`
- **UI Stack**: Jetpack Compose, Material 3 Dark theme
- **Architecture**: MVVM with Clean Architecture principles
- **Persistence**: Room SQLite database
- **Networking**: Retrofit 2 REST + OkHttp WebSocket
- **Voice**: Android SpeechRecognizer (PTT) + TextToSpeech (TTS)

## Project Structure
```text
android-client/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   └── libs.versions.toml
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/ai/qwenton/apex/
        │   │   ├── MainActivity.kt
        │   │   ├── QwentonApp.kt
        │   │   ├── domain/ (Models, Repositories, Policies)
        │   │   ├── ui/ (Chat, Terminal, Skills, History, Settings)
        │   │   └── ui/theme/ (Color, Theme, Type)
        │   └── res/
        └── test/
```

## Build Instructions
```bash
# Debug build
./gradlew assembleDebug

# Run unit tests
./gradlew test
```
