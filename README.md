# Musee Rodin Companion Android

Personal Android clone of the iOS Musee Rodin Companion app.

## Scope

- Single Activity, Jetpack Compose Material 3 only.
- Five top-level destinations: Places, Works, Paths, Search, Notes.
- Compact screens use bottom navigation; expanded screens use a navigation rail.
- Read-only museum content is bundled as JSON assets generated from `../shared-assets/content`.
- Local user state is stored separately with Room: notes, favorites, seen works, route progress, and playback progress.
- Read-aloud uses Android `TextToSpeech`; the app does not create, bundle, download, cache, or generate audio files.
- Work imagery uses deterministic work-ID placeholders. Downloaded work images are intentionally not bundled in this phase.
- Citations are low-emphasis chips that open source URLs with `Intent.ACTION_VIEW`.
- No login, sync, analytics, AI chat, embeddings, network AI calls, PDF-reader UX, Play Store scaffolding, or bundled audio.

## Versions

Requested core versions are configured:

- Kotlin `2.4.10`
- Android Gradle Plugin `9.3.0`
- Gradle wrapper distribution `9.6.1`
- Compose BOM `2026.06.00`
- `minSdk 26`, `compileSdk 36`, `targetSdk 36`

Companion AndroidX versions pinned in `gradle/libs.versions.toml`:

- Room `2.8.4`
- Navigation Compose `2.9.5`
- Lifecycle `2.9.4`
- Activity Compose `1.12.0`
- Kotlinx Serialization JSON `1.9.0`

Gradle `9.5` was requested, but it was not present on Gradle's official distribution service on 2026-07-23. The project uses Gradle `9.6.1`, the current stable version reported by `https://services.gradle.org/versions/current`.

AGP 9 has built-in Kotlin support, so the project intentionally does not apply `org.jetbrains.kotlin.android`. Kotlin `2.4.10` is supplied through the buildscript classpath and Kotlin compiler plugins. Room code generation uses KSP `2.3.10`, matching Kotlin's current KSP guidance for Kotlin `2.4.10`.

Room was bumped from `2.8.3` to `2.8.4`, the latest release in Google Maven, after `2.8.3` failed to read Kotlin `2.4.10` metadata during annotation processing. The project then moved from kapt to KSP because kapt-based Room processing still failed against Kotlin `2.4.10` metadata.

These companion versions were initially unverified locally because this machine had no Gradle dependency cache and no standalone Gradle binary.

## Local Tooling Notes

Observed on this machine:

- Android SDK packages exist at `/opt/homebrew/share/android-commandlinetools`.
- `~/Library/Android/sdk` is absent.
- Standalone `gradle` is absent from `PATH`.
- No exact JDK 17 toolchain was detected; the build keeps Java 17 source/target compatibility and uses Android Studio's bundled Java.
- `local.properties` points `sdk.dir` at the Homebrew Android SDK path.

The Gradle wrapper metadata and wrapper jar are checked in, so CI and fresh clones can use `./gradlew` directly.

## Commands

Sync bundled app content from the workspace asset package:

```sh
cd ..
python3 tools/sync_app_assets.py --target android --write
```

Sync both platform apps from the same package:

```sh
cd ..
python3 tools/sync_app_assets.py --target all --write
```

Build debug APK:

```sh
cd android
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:assembleDebug
```

Run JVM unit tests:

```sh
cd android
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:testDebugUnitTest
```

Run connected Compose robot tests:

```sh
cd android
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:connectedDebugAndroidTest
```

Capture screenshots to `android/artifacts/screenshots`:

```sh
cd android
tools/capture_screenshots.sh
```

If no emulator is running, create or start an API 36 emulator using the installed Homebrew SDK tools before running connected tests.

## GitHub Release APK

The repository includes `.github/workflows/android-release.yml`.

The workflow builds a signed release APK, uploads it as a workflow artifact, and creates or updates a GitHub Release with the APK attached. It runs automatically for tags matching `v*` and can also be triggered manually:

```sh
gh workflow run android-release.yml -f version=v0.1.0 -f prerelease=false
```

Required GitHub Secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

The local release keystore and password files live under `signing/`, which is intentionally ignored by Git. Losing the keystore means future APK updates cannot use the same signing identity.

## Test Coverage

JVM tests cover:

- JSON decoding from bundled assets.
- Citation coverage and source resolution.
- Route/audio-stop/link integrity.
- Deterministic search across works, topics, routes, source chunks, and local notes.
- User repository behavior for notes, favorites, seen, route progress, and playback progress.
- Narration state transitions and controller behavior with a fake speech engine.

Connected Compose tests include robot classes for:

- Places, Works, WorkDetail, TopicDetail, Paths, PathDetail, Search, Notes, NoteEditor.
- Reusable WorkRow, CitationChip, ReadAloudButton, PlaceholderPanel, ConfidenceChip, MetadataGrid, and TagChip components.

Robots interact only through content descriptions, labels, test tags, and semantics.
