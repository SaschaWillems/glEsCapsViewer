# OpenGL ES Viewer - Modern Edition

A modern Android application for viewing and analyzing OpenGL ES hardware capabilities of your device.

## ✨ Features

- 📱 **Device Information** - Display detailed device specs
- 🎮 **OpenGL ES Info** - View renderer, vendor, and version details
- 📊 **Capabilities Browser** - Explore GLES 2.0, 3.0, 3.1, and 3.2 capabilities
- 🔌 **Extensions Viewer** - See all supported extensions and formats
- 🎨 **Material Design 3** - Modern and beautiful UI
- 📈 **Real-time Updates** - Refresh information anytime

## 🏗️ Architecture

### Tech Stack
- **Language**: Java 11
- **Android SDK**: API 26 (Min) - API 34 (Target)
- **UI Framework**: AndroidX + Material Design 3
- **Build System**: Gradle 8.1.2

### Project Structure

```
app/src/main/
├── java/de/saschawillems/glescapsviewer/
│   ├── ui/
│   │   ├── MainActivity.java          # Main activity with tabs
│   │   └── fragment/
│   │       ├── DeviceFragment.java    # Device information
│   │       ├── GLInfoFragment.java    # OpenGL ES info
│   │       ├── CapabilitiesFragment.java  # Capabilities list
│   │       └── ExtensionsFragment.java    # Extensions viewer
│   ├── adapter/
│   │   └── ViewPagerAdapter.java      # Fragment pager adapter
│   └── util/
│       └── GLESInfoCollector.java     # OpenGL ES data collector
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   ├── fragment_device.xml
│   │   ├── fragment_gl_info.xml
│   │   ├── item_info_card.xml
│   │   ├── item_section_header.xml
│   │   └── menu/
│   │       └── menu_main.xml
│   ├── values/
│   │   ├── strings.xml
│   │   ├── colors.xml
│   │   └── themes.xml
│   └── drawable/
│       ├── ic_device.xml
│       ├── ic_opengl.xml
│       ├── ic_capabilities.xml
│       ├── ic_extensions.xml
│       └── ic_launcher.xml
└── AndroidManifest.xml
```

## 📋 Dependencies

```gradle
// AndroidX Core
- androidx.appcompat:appcompat:1.6.1
- androidx.core:core:1.12.0
- androidx.constraintlayout:constraintlayout:2.1.4

// Material Design 3
- com.google.android.material:material:1.11.0

// AndroidX Lifecycle
- androidx.lifecycle:lifecycle-runtime:2.6.2
- androidx.lifecycle:lifecycle-viewmodel:2.6.2
- androidx.lifecycle:lifecycle-livedata:2.6.2

// AndroidX RecyclerView
- androidx.recyclerview:recyclerview:1.3.2

// Kotlin Coroutines
- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1
- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1

// Utilities
- com.google.code.gson:gson:2.10.1
- com.jakewharton.timber:timber:5.0.1
```

## 🚀 Getting Started

### Requirements
- Android Studio 2023.1+
- Android SDK 34
- Java 11+

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Hnoodhlite/GLES-viewer.git
   cd GLES-viewer
   ```

2. Open in Android Studio

3. Sync Gradle files

4. Build and run on your Android device (API 26+)

## 📊 Information Displayed

### Device Tab
- Device Name
- OS Version
- CPU Cores
- CPU Speed
- Screen Resolution
- CPU Architecture

### OpenGL ES Tab
- Vendor
- Renderer
- Version
- Shading Language
- EGL Information

### Capabilities Tab
- OpenGL ES 2.0 capabilities
- OpenGL ES 3.0 capabilities
- OpenGL ES 3.1 capabilities (if supported)
- OpenGL ES 3.2 capabilities (if supported)

### Extensions Tab
- OpenGL ES extensions
- Compressed texture formats
- Shader binary formats
- Program binary formats
- EGL extensions

## 🎨 Material Design 3

This app uses the latest Material Design 3 theming system with:
- Dynamic color support
- Dark and light theme variants
- Modern components (Material Cards, Tabs, etc.)
- Improved typography hierarchy

## 📄 License

GNU Lesser General Public License v3.0

See [LICENSE](LICENSE) for details.

## 👤 Original Author

Sascha Willems (www.saschawillems.de)

## 🔄 Version History

### v1.0.0 (Modern Update)
- Completely redesigned UI with Material Design 3
- Removed upload feature (view-only mode)
- Updated all libraries to latest versions
- Improved code structure with proper separation of concerns
- Added proper Fragment-based navigation
- Better error handling and logging

## 💡 Tips

- Use "Refresh" menu option to update device information
- Information is collected in a background thread to prevent UI freezing
- All data is stored in memory and not persisted
- Perfect for troubleshooting graphics issues or comparing device capabilities

## 🐛 Known Issues

None currently. Please report any issues on GitHub.

## 🙏 Contributing

Feel free to submit issues and enhancement requests!

---

**Made with ❤️ for Android Developers**
