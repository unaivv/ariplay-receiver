# AirPlay Receiver

Android app that acts as an AirPlay receiver, allowing iOS devices to stream audio/video to the Android device.

## Features

- AirPlay protocol support
- Audio streaming
- Video streaming
- NSD (Network Service Discovery) for automatic device discovery

## Requirements

- Android SDK 24+
- CMake for native code build
- Android Studio Arctic Fox or later

## Build

```bash
./gradlew assembleDebug
```

## Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## License

MIT