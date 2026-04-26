# AirPlay Receiver for Google TV

An Android TV (Google TV) application that acts as an AirPlay receiver, allowing Apple devices (iPhone, iPad, Mac) to stream screen mirrors and multimedia content to your Google TV or Android TV device over the local network.

## What is AirPlay?

AirPlay is Apple's proprietary protocol for streaming media content between devices. This app implements the AirPlay receiver protocol, enabling your TV to appear as an available AirPlay destination on Apple devices.

## Features

- **Screen Mirroring**: Mirror your iPhone, iPad, or Mac screen directly to TV
- **Video Streaming**: Stream movies, TV shows, and videos from Apple apps
- **Audio Streaming**: Play music and podcasts with high-quality audio
- **Photo Streaming**: View photos and videos from your photo library
- **Universal Control**: Control playback from your Apple device while content displays on TV
- **NSD (Network Service Discovery)**: Automatic discovery on the local network

## Supported Apple Devices

- iPhone (iOS 14+)
- iPad (iPadOS 14+)
- Mac (macOS Monterey+)
- Other AirPlay-enabled devices

## Requirements

- Google TV or Android TV device
- Android SDK 24+ (Android 7.0 Nougat or later)
- CMake for native code build
- Android Studio Arctic Fox or later

## How to Use

1. Install the app on your Google TV/Android TV device
2. Ensure your TV and Apple device are on the same Wi-Fi network
3. On your Apple device, open the Control Center or Settings
4. Select your TV device from the AirPlay list
5. Start streaming or screen mirroring

## Build from Source

```bash
# Clone the repository
git clone https://github.com/unaivv/ariplay-receiver.git
cd ariplay-receiver

# Build debug APK
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

## Install via ADB

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Technical Details

This app uses native C/C++ code (via JNI) to implement the AirPlay protocol, including:
- HTTP/HTTPS streaming
- RTSP (Real Time Streaming Protocol)
- FairPlay DRM support
- Audio/Video decoding

## License

MIT

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.