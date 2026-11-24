# Offline SubPlayer (Android)

An offline-capable Android video player built with **Kotlin**, **Jetpack Compose (Material 3)**, **Media3 ExoPlayer**, and **Google ML Kit Translation**.

## Features
- Local video playback via Storage Access Framework (`OpenDocument`) with Media3 ExoPlayer.
- Gesture controls: horizontal swipe to seek with live time preview, vertical swipe on the left half for screen brightness, and vertical swipe on the right half for media volume.
- `.srt` subtitle loading, parsing, and synchronized overlay rendering.
- Automatic offline subtitle translation using ML Kit Translation with cached translations and progress indicator.
- Searchable source and target language selection dialogs, language swap button, and auto-translate toggle.
- Export translated subtitles to a new `.srt` file via `CreateDocument` with automatic reload.
