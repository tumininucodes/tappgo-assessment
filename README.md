# Spin Wheel Native Module & Widget

A comprehensive React Native solution offering a native Android Spin Wheel component and a synchronized Home Screen Widget. This project demonstrates bridging Kotlin native UI with React Native, featuring remote configuration and asset caching.

## Demo Reel

<video src="https://res.cloudinary.com/dc8ivje8d/video/upload/v1773137336/spinwheel_ikpfbg.mov" width="100%" controls>
  Your browser does not support the video tag.
</video>

## Key Deliverables

This project consists of three main parts:

1.  **[Kotlin Widget Library (`kotlin-spin-wheel`)]**
    *   The core native implementation written in Kotlin.
    *   Handles remote configuration fetching and local asset downloading.
    *   Implements the `SpinWheelView` for React Native and the `SpinWheelWidgetProvider` for the Android home screen.

2.  **[React Native Wrapper Package]**
    *   A pre-packaged tarball (`react-native-spin-wheel-1.0.0.tgz`) for easy installation in any React Native project.
    *   Exposes the native `SpinWheelView` to React Native.

3.  **[Demo App & APK]**
    *   `app-release.apk`: A ready-to-install Android application to test the widget functionality.
    *   `rn-app/`: The source code for the React Native demo application.

---

## Features
-   **Native Performance**: Smooth animations powered by native Android `RotateAnimation`.
-   **Dynamic Config**: Fetches config asset URLs from a remote JSON endpoint (google drive link).
-   **Asset Caching**: Automatically downloads and stores spin wheel images (`bg`, `wheel`, `frame`) locally for offline use.
-   **Home Screen Widget**: A fully functional Android widget that syncs state with the library and allows users to spin directly from their home screen.

---

## Installation

To use the Spin Wheel library in your React Native project:

```bash
# Install the local package
npm install ./react-native-spin-wheel-1.0.0.tgz

# Or using yarn
yarn add ./react-native-spin-wheel-1.0.0.tgz
```

---

## Usage

### React Native Component

```jsx
import { SpinWheelView } from 'kotlin-spin-wheel';

const App = () => {
  return (
    <SpinWheelView 
      style={{ width: 300, height: 300 }}
      configUrl="YOUR_CONFIG_JSON_URL"
    />
  );
};
```

### Android Home Screen Widget

The widget allows users to interact with the spin wheel without opening the app.

#### How to setup:
1.  Install the **[Demo APK](file:///Users/oluwatumininuojo/Downloads/Tappgo-assessment/app-release.apk)** or run the `rn-app`.
2.  Go to your Android Home Screen.
3.  **Long-press** on an empty space.
4.  Select **Widgets**.
5.  Search for **"SpinWheel App"**.
6.  Drag and drop the widget to your home screen.
7.  Tap the **"Tap to Spin!"** button to start the animation!

---

## Development

-   **Native Source**: `kotlin-spin-wheel/android`
-   **JS/TS Wrapper**: `kotlin-spin-wheel/src`
-   **Demo App**: `rn-app`

The library uses a foreground service (`SpinWheelAnimationService`) to handle widget animations reliably on Android 12+.
