# GitHub APK Build Guide

This project is configured to build on GitHub Actions without committing the Android SDK or Gradle binaries to the repository.

## 1. Create the repository

Create an empty GitHub repository, for example:

`ScreenTranslator`

Do not add a README, `.gitignore`, or license from GitHub if you are uploading this project as-is; those files are already included.

## 2. Upload the project

The repository root must contain:

```text
ScreenTranslatorFresh/
  app/
  .github/
  build.gradle.kts
  settings.gradle.kts
  gradle.properties
```

If you want the repository itself to be the project root, move the contents of `ScreenTranslatorFresh/` into the repository root before committing.

### Git command example

```bash
git init
git add .
git commit -m "Initial Screen Translator Android project"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/ScreenTranslator.git
git push -u origin main
```

## 3. Get the APK

After the push:

1. Open the repository on GitHub.
2. Open **Actions**.
3. Select **Build Android APK**.
4. Open the completed workflow run.
5. Under **Artifacts**, download `ScreenTranslator-debug-apk`.
6. Extract the downloaded artifact and install `app-debug.apk` on your Android phone.

The workflow installs:

- Java 17
- Gradle 8.9
- Android SDK platform 35
- Android Build Tools 35.0.0

## 4. Manual build from GitHub Actions

You can also use **Actions → Build Android APK → Run workflow** without making a source change.

## 5. Release tags

Pushing a tag such as:

```bash
git tag v0.1.0
git push origin v0.1.0
```

runs the release workflow. It produces an unsigned release APK and attaches it to the GitHub Release.

### Important: release signing

The release workflow intentionally produces an **unsigned** APK. For personal testing, the debug APK is the easiest option.

Before distributing the app publicly, create an Android signing key and store the signing material as GitHub Actions secrets. Do not commit a keystore or passwords to the repository. The release workflow can then be upgraded to produce a signed release APK.

## 6. Local Android Studio build

Open the project directory in Android Studio with Android SDK 35 installed. Android Studio can sync the Kotlin/Android Gradle project and build the debug APK.

## 7. What the workflow does not do

It does not publish the APK to Google Play, and it does not require a Google Play account. It only builds and stores the APK in GitHub Actions.
