# Screen Translator — Fresh v0.1

A clean Android prototype centered on a floating translator bubble and a right-side translation panel.

## Current flow

1. Open the app.
2. Allow overlay permission.
3. Tap **Start translator** and grant screen-capture permission.
4. A floating **文** bubble appears above other apps.
5. Tap the bubble to open the right-side panel.
6. Tap **Translate screen**.

## Architecture

- `TranslatorOverlayService`: capture + overlay lifecycle.
- `TranslationEngine`: backend interface.
- `MlKitTranslationEngine`: temporary on-device backend.

The translation backend is intentionally isolated so a future fully independent OCR/translation implementation can replace it without redesigning the UI.

## GitHub build

See [GITHUB_BUILD.md](GITHUB_BUILD.md). The repository includes GitHub Actions workflows that build the debug APK automatically.

## Android requirements

- Android 8.0 (API 26) or newer
- Target/compile SDK 35
- Java 17 for the Android build

## Current limitation

The v0.1 backend uses on-device ML Kit. It does not call the Google Translate web/API, but ML Kit is still a Google library. Replacing this backend with fully independent offline OCR and translation is a planned next step.
