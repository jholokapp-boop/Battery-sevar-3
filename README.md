# Battery Saver (Android)

A pure native Android application built with **Kotlin** and **Jetpack Compose**, engineered for extreme battery longevity, background task suspension, live hardware telemetry, and stunning futuristic neon animations.

## Key Features

- **Turbo Battery Saver Mode**:
  - Suspends and kills background app processes via `ActivityManager.killBackgroundProcesses`.
  - Blocks phantom wake-locks, background sync, and unnecessary GPS polling.
  - Ultra-lightweight footprint (~2MB RAM) so only the user-selected active app runs.
  - Animated 4-step system calibration with progress indicators, confetti celebration, and live stopwatch tracking extra battery uptime gained.
- **Deep Android Hardware Telemetry**:
  - Real-time battery level and state listening (`Intent.ACTION_BATTERY_CHANGED`).
  - Charging source detection (AC Wall Charger, USB, Wireless Fast Pad).
  - Accurate battery temperature (°C), voltage (V), health status, and battery technology (Li-ion).
  - Dynamic discharge and charge duration estimations.
- **Electric Hyper-Charging Screen**:
  - Full-screen electric neon charging animation with orbiting energy particles and dynamic arcs.
  - Live decimal percentage readout (e.g. 78.4%).
  - Fast Charge 65W telemetry display.
- **Animated Splash Screen**:
  - Glowing neon battery enclosure with dynamic liquid level counter (0% to 100%).
  - Rotating energy aura and pulsing waves.
  - 6-stage hardware diagnostics sequencer with instant "SKIP" option.
- **App Power Management**:
  - Per-app power consumption breakdown with active screen time and background drain.
  - Search and segmented sorting (% Usage, Active Time, Alphabetical).
  - App detail sheet with direct "Force Stop" capability.
- **24-Hour Power Telemetry & History**:
  - Interactive smooth 24-hour battery curve with gradient fill.
  - Min, Avg, and Max metrics.
  - Chronological power events log.
- **Power Optimization Settings**:
  - Pure Black OLED / Dark Mode toggle to eliminate display pixel power draw.
  - Extreme Background Killer profile.
  - Low Battery (20%) and Full Charge (100%) alerts.
  - Telemetry reset options.

## Tech Stack
- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM with Kotlin Coroutines and StateFlow
- **Platform**: Android SDK 36 (minSdk 26)