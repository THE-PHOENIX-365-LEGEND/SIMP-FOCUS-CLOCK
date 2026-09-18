# SIMP-FOCUS-CLOCK

A modern Android productivity app designed to help you manage your time, stay focused, and build better study/work habits.

## ✨ Features

### ⏰ Alarm
- Set alarms using a specific clock time
- One-time alarms
- Daily alarms
- Weekday alarms
- Weekend alarms
- Custom repeat days
- Alarm labels
- Enable/disable alarms
- Delete alarms

### ⏱️ Stopwatch
- Start
- Pause
- Resume
- Reset
- Accurate elapsed-time tracking

### ⏳ Timer
- Quick presets
- Custom hours, minutes, and seconds
- Start
- Pause
- Resume
- Reset
- Timer completion notification

### 🎯 Focus Mode
- Select an installed application
- Set a Focus duration
- Set a Cooldown duration
- Block the selected application during Focus/Cooldown
- Multiple independent Focus sessions
- Support for up to 10 active app sessions
- Emergency unlock with a generated code
- Focus and Cooldown notifications

## 🛠️ Built With

- Kotlin
- Jetpack Compose
- Android SDK
- AndroidX
- DataStore
- AlarmManager
- Accessibility Service

## 📱 Requirements

- Android device
- Android Studio
- Kotlin-compatible Android development environment

Some Focus features require Accessibility Service permission.

Alarm functionality may require the **Alarms & reminders** permission depending on the Android version and device.

## 🚀 Getting Started

1. Clone this repository.
2. Open the project in Android Studio.
3. Allow Gradle to sync and download the required dependencies.
4. Connect an Android device or start an Android emulator.
5. Build and run the application.

## 🔐 Permissions

SIMP-FOCUS-CLOCK may request permissions required for:

- Notifications
- Exact alarms
- Accessibility Service
- Displaying Focus blocking UI

Permissions are used only for the corresponding application features.

## 📂 Project Structure

```text
app/
└── src/
    └── main/
        ├── java/com/example/simp_focus_clock/
        │   ├── alarm/
        │   ├── data/
        │   ├── manager/
        │   ├── model/
        │   ├── notification/
        │   ├── repository/
        │   ├── service/
        │   ├── stopwatch/
        │   ├── timer/
        │   └── ui/
        └── res/
## 📄 License

This project is licensed under the MIT License.

See the [LICENSE](LICENSE) file for details.
