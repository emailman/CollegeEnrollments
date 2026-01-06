# College Enrollments

A Kotlin Multiplatform application for managing college student enrollments. Built with Compose Multiplatform, this app runs on both Android and Desktop (JVM) platforms.

## Features

- **Student Management**: Add, edit, and delete students with name and email information
- **Course Management**: Create and manage courses with code, name, and credit hours
- **Enrollment Tracking**: Enroll students in courses and assign grades
- **Cross-Platform**: Single codebase runs on Android and Desktop

## Technologies

- **Kotlin Multiplatform** - Shared business logic across platforms
- **Compose Multiplatform** - Declarative UI framework for Android and Desktop
- **SQLDelight** - Type-safe SQL database with multiplatform support
- **Material 3** - Modern Material Design components
- **Coroutines & Flow** - Reactive data streams

## Project Structure

```
composeApp/
├── src/
│   ├── commonMain/          # Shared code for all platforms
│   │   └── kotlin/
│   │       └── edu/emailman/collegeenrollments/
│   │           ├── db/              # Database and repositories
│   │           ├── ui/
│   │           │   ├── screen/      # UI screens
│   │           │   └── viewmodel/   # ViewModels
│   │           └── App.kt           # Main app composable
│   ├── androidMain/         # Android-specific code
│   └── jvmMain/             # Desktop-specific code
```

## Building and Running

### Android

Build the debug APK:
```shell
./gradlew :composeApp:assembleDebug
```

Or use the Android run configuration in Android Studio.

### Desktop (JVM)

Run the desktop application:
```shell
./gradlew :composeApp:run
```

Create distribution packages:
```shell
./gradlew :composeApp:package
```

## Database Schema

The app uses SQLDelight with three main tables:

- **Student** - id, name, email
- **Course** - id, code, name, credits
- **Enrollment** - id, student_id, course_id, enrollment_date, grade

## License

This project is for educational purposes.
