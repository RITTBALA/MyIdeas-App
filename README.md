# 💡 MyIdeas App (Idea Store & Git Commit Graph)

A premium, modern Android application built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, and **GitHub REST API Integration**. Designed for importing, managing, and exporting structured JSON thoughts and multi-node Git commit graph projects with a **Linear / GitHub-style dark engineering design** (`#0D1117`, `#161B22`, `#30363D`).

---

## 🚀 Features

### 1. Dual-Schema AI Context Support
- **Schema v1 (Single Ideas):** Title, summary, category (`DEV`, `DIY`, `HARDWARE`, `WORKFLOW`, `STUDY`, `OTHER`), hashtag list, actionable checklist item (`First Step`), key points list.
- **Schema v2 (Multi-Node Projects):** Project title, detailed description, GitHub repository slug, category, current micro-step, project status (`ACTIVE`, `COMPLETED`), node graph history, and real Git commits.
- **Clipboard Auto-Parsing:** Automatically detects, validates, and parses Markdown JSON blocks (` ```json ... ``` `) copied from AI assistants (ChatGPT, Claude, Gemini) or manual inputs.

### 2. Dual-View Tabbed Architecture
- **`[ 💡 Concept (N) ]`:** Visualizes the AI concept decision tree (`DECISION`, `TASK_DONE`, `MILESTONE`, `OPEN_QUESTION`) connected with continuous vertical Git graph canvas lines.
- **`[ ⌥ Git Activity (N) ]`:** Displays real code development commits fetched from GitHub.

### 3. Dedicated GitHub REST API Integration
- **Repo Linking:** Connect repositories using `owner/repo`, `https://github.com/owner/repo`, or `git@github.com:...` formats.
- **Live Git Log Fetching:** Communicates directly with `GET https://api.github.com/repos/{owner}/{repo}/commits` to retrieve commit SHA, commit title/body, author, timestamp, and web URLs.
- **Auto Sync & Browser Direct Link:** Manual sync button with spinning rotation indicator and direct commit links opening in the browser.

### 4. Interactive Git Graph Dialog
- Continuous vertical Git line canvas using `IntrinsicSize.Min` and `fillMaxSize()` to connect all commit nodes without gaps.
- Sequential, clipped frame-by-frame expand and collapse animations (`clipToBounds` & `animateContentSize`).

### 5. "Copy JSON for AI" Context Exporting
- Instantly wrap ideas or multi-node projects into formatted JSON Markdown code blocks and copy them to the clipboard to continue conversation flows with LLM assistants seamlessly.

### 6. Zero-Flash Dark Mode & Splash Screen
- Built with Android 12+ `androidx.core.splashscreen` and dark window backgrounds (`#0D1117`) to eliminate white flashes on app startup.

---

## 🛠️ Tech Stack & Libraries

| Category | Technology |
|---|---|
| **Language** | Kotlin 2.1.0 |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Database** | Room 2.8.5 with KSP (Version 5 Schema) |
| **Serialization** | `kotlinx.serialization` 1.8.0 |
| **Network API** | GitHub REST API v3 |
| **System Splash** | `androidx.core:core-splashscreen` |
| **Architecture** | MVVM with `StateFlow` & `Coroutines` |
| **Compile / Min SDK** | Compile SDK 37 / Min SDK 24 |

---

## 📄 Supported JSON Schemas

### Schema v1: Single Idea
```json
{
  "schema_version": 1,
  "title": "ESP32 Ultra-Low-Power Telemetry Node",
  "summary": "Battery powered environmental sensor hub with BLE advertisement.",
  "category": "HARDWARE",
  "tags": ["esp32", "ble", "sensors"],
  "first_step": "Order ESP32-C3 breakout boards",
  "key_points": [
    "Deep sleep current < 10uA",
    "Periodic BME280 sensor reads"
  ],
  "created_at": "2026-10-17T21:00:00Z"
}
```

### Schema v2: Multi-Node Project
```json
{
  "schema_version": 2,
  "project_id": "esp32-ble-sensor-hub",
  "project_title": "ESP32 BLE Sensor Hub",
  "project_description": "Ultra-low-power environmental telemetry node leveraging ESP32-C3.",
  "github_repo": "username/esp32-ble-sensor-hub",
  "category": "HARDWARE",
  "export_type": "INIT",
  "current_head": {
    "next_micro_step": "Draft PlatformIO configuration",
    "status": "ACTIVE"
  },
  "nodes": [
    {
      "node_id": "node-1",
      "type": "DECISION",
      "tag": "HARDWARE",
      "title": "Select ESP32-C3 over Classic ESP32",
      "detail": "Opted for RISC-V based ESP32-C3 for superior sleep efficiency.",
      "timestamp": "2026-10-17T21:19:00Z"
    }
  ],
  "commits": [
    {
      "sha": "a1b2c3d",
      "message": "Initial commit: PlatformIO setup",
      "author": "Developer",
      "timestamp": "2026-10-17T22:00:00Z"
    }
  ]
}
```

---

## 🏗️ Building and Running

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 17 or JDK 21
- Android SDK 37

### Build Commands
Clone the repository and build the debug APK:
```bash
git clone https://github.com/RITTBALA/MyIdeas-App.git
cd MyIdeas-App
./gradlew assembleDebug
```

Run unit tests:
```bash
./gradlew testDebugUnitTest
```

---

## 📂 Project Structure

```text
app/src/main/java/hu/bme/aut/android/myideas/
├── data/
│   ├── local/          # Room Entities (Idea, Project, Node, GitCommit), DAOs, AppDatabase v5
│   ├── model/          # DTOs (IdeaDto, ProjectDto, GitCommitDto)
│   ├── network/        # GithubLogService (GitHub REST API client)
│   ├── parser/         # ClipboardParser (Markdown JSON v1 & v2 parser)
│   └── repository/     # IdeaRepository (Single source of truth)
├── ui/
│   ├── components/     # Compose UI Cards, Canvas Timelines, BottomSheets, Dialogs
│   ├── screens/        # IdeaBoardScreen (Main host layout with tabs)
│   ├── theme/          # Color tokens, Typography, Dark MaterialTheme
│   └── viewmodel/      # IdeaBoardViewModel & StateFlow UI state
└── MainActivity.kt     # Single Activity host with Dark Splash Screen
```

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
