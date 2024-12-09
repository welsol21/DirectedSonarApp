# Directed Sonar App

**Directed Sonar App** is a mobile application for Android devices designed to measure distances using sound waves. This innovative app combines advanced audio processing with an intuitive user interface, offering reliable results and robust functionality. To ensure accurate measurements, the application requires a functioning speaker and microphone on the device.

---

## Features

### 1. **Distance Measurement**
- **Customizable Measurement Parameters:**
  - **Frequency:** Set the frequency of the sound signal.
  - **Signal Duration:** Adjust the length of each signal.
  - **Signal Count:** Determine how many signals to transmit in a series.
- **Real-Time Feedback:** Displays live progress during measurements.
- **Optional Notes:** Users can add descriptive notes for each series of measurements.
- **Automatic Storage:** Measurement results are saved to a local database for future reference.
- **Interactive Charts:**
  - Visualize results directly in the app using dynamically generated charts.
  - See trends and patterns based on measurement data grouped by user-defined notes.
  - Graphs include calculated median lines and customizable axes.
  - Clear, user-friendly representation of data without unnecessary clutter.

---

### 2. **Record Management**
- **On-the-Fly Editing:**
  - Each record stored in the database can be quickly edited by clicking on the respective note field.
  - Changes are reflected immediately in the app and saved automatically.
- **Bulk Deletion:**
  - Users can select multiple records for deletion through an intuitive selection UI.
  - A dynamically appearing "Delete Selected" button enables batch deletion.
  - Effortless management of stored records, even in large datasets.

---

### 3. **Settings Customization**
- **Adjust Key Parameters:** Modify frequency, duration, signal count, and sample rate directly in the app’s settings.
- **User Preferences:** Changes persist using Android's `SharedPreferences`, ensuring a consistent experience across app sessions.

---

## Applied Technologies
- **Jetpack Compose:** A modern UI toolkit for building Android applications with fully declarative components.
- **Room Database:** Manages local storage of measurements with robust query capabilities.
- **MPAndroidChart:** Provides powerful, customizable charting components for visualizing measurement data.
- **Coroutines:** Ensures smooth, asynchronous operations for audio processing and database interactions.
- **LiveData:** Automatically updates the UI in response to changes in the database.
- **ViewModel Architecture:** Separates UI logic from business logic for maintainability and scalability.
- **AudioTrack & AudioRecord:** Handles low-level audio playback and recording for accurate distance measurement.

---

## User Guide

### Home Screen
1. Enter an optional note for the measurement session.
2. Press the **Start Measurement** button to begin.
3. View real-time progress and results for each signal.

### History Screen
- Browse, sort, and filter through saved measurement records.
- Edit individual notes directly from the screen.
- Use pagination to navigate through large datasets.

### Graph Screen
- View visual representations of measurement data grouped by notes.
- Analyze trends using median lines.
- Focused, uncluttered axes to enhance readability.

### Settings Screen
- Adjust default measurement parameters (frequency, duration, signal count, and sample rate).
- All changes are saved automatically.

---

## Limitations and Warnings

1. **Device Requirements:**
   - The app requires a functioning speaker and microphone to perform distance measurements effectively.
   - Please ensure these components are operational on your device before using the app.

2. **Measurement Range:**
   - The app has been tested and optimized for distances up to **30 cm**. Beyond this range, results may vary in accuracy due to environmental and hardware factors.

---

## Installation
1. Clone the repository:  
   ```bash
   git clone https://github.com/welsol21/directed-sonar-app.git
   ```
2. Open the project in Android Studio.
3. Build and run the application on an emulator or physical device.

## License

This project is licensed under the MIT License. See the LICENSE file for details.
