# 📱 FilePicker - Modern Android File Upload Application

A modern Android application built with Clean Architecture, MVVM, Jetpack Compose, and Firebase integration for seamless file picking and uploading experience.

## 🎯 Features

- **📁 Multi-file Selection**: Select multiple files of any type (images, videos, documents) from device storage
- **📤 File Management**: Manage and organize selected files with real-time progress tracking
- **🔔 Push Notifications**: Firebase Cloud Messaging integration for background notifications
- **📊 Progress Tracking**: Real-time progress with visual indicators and notifications
- **🌙 Material 3 Design**: Modern UI following Google's latest design guidelines
- **🔐 Permission Handling**: Runtime permissions for Android 13+ compatibility
- **📱 Background Processing**: Operations continue even when app is in background
- **🌐 Web Integration**: Built-in WebView for web-based operations

## 🏗️ Architecture

This project follows **Clean Architecture** principles with a clear separation of concerns:

```
📱 FilePicker/
├── 🎯 Domain Layer (Business Logic)
│   ├── models/          # FileItem, UploadStatus
│   ├── repositories/    # FileRepository, NotificationRepository
│   └── usecases/       # UploadFilesUseCase
│
├── 📊 Data Layer (Data Management)
│   ├── remote/          # FileIoService, FirestoreDataSource
│   ├── repositories/    # FileRepositoryImpl, NotificationRepositoryImpl
│   └── notification/    # AppFirebaseMessagingService
│
└── 🎨 Presentation Layer (UI)
    ├── screens/         # FilePickerScreen, UploadedFilesScreen
    ├── components/      # FileItemCard, UploadProgressCard
    └── viewmodels/      # FilePickerViewModel
```

### Architecture Benefits

- **🧩 Separation of Concerns**: Clear boundaries between layers
- **🧪 Testability**: Easy to unit test business logic
- **🔧 Maintainability**: Simple to modify and extend
- **📈 Scalability**: Easy to add new features
- **🔄 Dependency Inversion**: Dependencies point inward

## 🛠️ Key Technologies & Libraries

### Core Framework
- **Jetpack Compose BOM** `2025.07.00` - Modern declarative UI framework
- **Hilt Android** `2.57` - Google's official Dependency Injection framework
- **Firebase BOM** `34.1.0` - Firebase services integration

### Notable Libraries
- **Accompanist Permissions** `0.37.3` - Runtime permission handling
- **Coil Compose** `2.7.0` - Image loading and caching
- **OkHttp** `5.1.0` - HTTP client for network operations

*For complete dependency list, see `gradle/libs.versions.toml`*

## 📸 Screenshots

### Main Screen
![Main Screen]
*File selection interface with Material 3 design*

### File Selection
![File Selection]
*Multi-file picker with permission handling*

### Upload Progress
![Upload Progress]
*Real-time upload progress with notifications*

### Push Notifications
![Push Notifications]
*Firebase Cloud Messaging integration*

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 24+ (API level 24)
- Kotlin 2.2.0+
- Gradle 8.11.1+

### Development Practices
This project follows **Atomic Commits** and **Semantic Commit Messages**:
- **Atomic Commits**: Each commit represents a single, complete change
- **Semantic Commits**: Structured commit messages following conventional commits format
  - `feat:` New features
  - `fix:` Bug fixes
  - `docs:` Documentation changes
  - `style:` Code style improvements
  - `refactor:` Code refactoring
  - `test:` Adding or updating tests
  - `chore:` Maintenance tasks

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/FilePicker.git
   cd FilePicker
   ```

2. **Add Firebase configuration**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add Android app with package name: `com.bengisusahin.filepicker`
   - Download `google-services.json` and place it in `app/` directory

3. **Build and run**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

### Firebase Setup

1. **Enable Cloud Messaging**
   - Go to Firebase Console > Project Settings
   - Add Android app if not already added
   - Download `google-services.json`

2. **Configure FCM**
   - Go to Cloud Messaging section
   - Create notification channels
   - Test push notifications

## 🔧 Configuration

### Build Configuration
```kotlin
android {
    namespace = "com.bengisusahin.filepicker"
    compileSdk = 36
    minSdk = 24
    targetSdk = 36
    
    buildFeatures {
        compose = true
    }
}
```

### Version Management
All library versions are centrally managed in `gradle/libs.versions.toml`:
```toml
[versions]
agp = "8.11.1"
kotlin = "2.2.0"
composeBom = "2025.07.00"
hiltAndroid = "2.57"
firebaseBom = "34.1.0"
```

## 📱 Usage

### Basic File Management
1. Open the app
2. Grant necessary permissions
3. Tap "Select Files"
4. Choose files from your device storage
5. Manage selected files
6. Monitor progress indicators
7. Receive completion notifications

### Push Notifications
- **App Open**: Notifications handled by FCM service
- **App Background**: System shows notifications automatically
- **App Closed**: Firebase handles notification display

## 🔒 Security

- **Anonymous Authentication**: Firebase Auth for user identification
- **File Validation**: File size and type checking
- **Permission Handling**: Runtime permission requests

## 📊 Performance

- **Coroutines**: Asynchronous operations
- **Flow**: Reactive programming
- **Lazy Loading**: Efficient list rendering
- **Memory Management**: Proper resource cleanup

```


## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Google**: For Android, Compose, and Firebase
- **Square**: For OkHttp and Retrofit
- **JetBrains**: For Kotlin language
- **Material Design**: For design guidelines



⭐ **Star this repository if you find it helpful!**
