# College Enrollments

A Kotlin Multiplatform application for managing college student enrollments. Built with Compose Multiplatform, this app runs on Android, Desktop (JVM), and Web (WebAssembly) platforms with a shared Ktor backend server.

## Features

- **Student Management**: Add, edit, and delete students with name and email information
- **Course Management**: Create and manage courses with code, name, and credit hours
- **Enrollment Tracking**: Enroll students in courses and assign grades
- **Cross-Platform**: Single codebase runs on Android, Desktop, and Web
- **REST API**: Ktor server provides a centralized backend for all clients

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Clients                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Android   │  │   Desktop   │  │    Web (wasmJs)     │  │
│  │  Emulator:  │  │ localhost:  │  │    localhost:       │  │
│  │  10.0.2.2   │  │    8081     │  │       8081          │  │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘  │
│         │                │                     │             │
│         └────────────────┼─────────────────────┘             │
│                          │ HTTP/JSON                         │
│                          ▼                                   │
│              ┌───────────────────────┐                       │
│              │   Ktor Server:8081    │                       │
│              │   (REST API)          │                       │
│              └───────────┬───────────┘                       │
│                          │                                   │
│                          ▼                                   │
│              ┌───────────────────────┐                       │
│              │   SQLite (SQLDelight) │                       │
│              └───────────────────────┘                       │
└─────────────────────────────────────────────────────────────┘
```

## Technologies

- **Kotlin Multiplatform** - Shared business logic across platforms
- **Compose Multiplatform** - Declarative UI framework for Android, Desktop, and Web
- **Ktor Server** - Backend REST API with Netty engine
- **Ktor Client** - HTTP client for all platforms
- **SQLDelight** - Type-safe SQL database (server-side)
- **kotlinx.serialization** - JSON serialization
- **Material 3** - Modern Material Design components
- **Coroutines & Flow** - Reactive data streams

## Project Structure

```
CollegeEnrollments/
├── server/                      # Ktor backend server
│   └── src/main/kotlin/
│       ├── Application.kt       # Server entry point
│       ├── db/                  # Database factory
│       ├── model/               # DTOs and request/response classes
│       └── routes/              # REST API routes
│
└── composeApp/                  # Multiplatform UI client
    └── src/
        ├── commonMain/          # Shared code
        │   └── kotlin/
        │       ├── api/         # API client and repositories
        │       ├── ui/screen/   # UI screens
        │       └── App.kt       # Main app composable
        ├── androidMain/         # Android-specific code
        ├── jvmMain/             # Desktop-specific code
        └── wasmJsMain/          # Web-specific code
```

## API Endpoints

### Students
- `GET /api/students` - List all students
- `GET /api/students/{id}` - Get student by ID
- `POST /api/students` - Create student
- `PUT /api/students/{id}` - Update student
- `DELETE /api/students/{id}` - Delete student

### Courses
- `GET /api/courses` - List all courses
- `GET /api/courses/{id}` - Get course by ID
- `POST /api/courses` - Create course
- `PUT /api/courses/{id}` - Update course
- `DELETE /api/courses/{id}` - Delete course

### Enrollments
- `GET /api/enrollments` - List all enrollments with details
- `GET /api/enrollments/{id}` - Get enrollment by ID
- `POST /api/enrollments` - Create enrollment
- `PUT /api/enrollments/{id}` - Update grade
- `DELETE /api/enrollments/{id}` - Delete enrollment

## Building and Running

### Prerequisites
- JDK 17 or higher
- Android SDK (for Android builds)
- Node.js (for Web builds)

### 1. Start the Server (Required)

```shell
./gradlew :server:run
```

The server starts on `http://localhost:8081`

### 2. Run a Client

#### Web (WebAssembly)
```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```
Opens automatically in browser at `http://localhost:8080` (or next available port)

#### Desktop (JVM)
```shell
./gradlew :composeApp:run
```

#### Android
```shell
# Build and install on connected device/emulator
./gradlew :composeApp:installDebug

# Or just build the APK
./gradlew :composeApp:assembleDebug
```

Note: Android emulator uses `10.0.2.2` to connect to the host machine's localhost.

## Database Schema

The server uses SQLDelight with three main tables:

- **Student** - id, name, email
- **Course** - id, code, name, credits
- **Enrollment** - id, student_id, course_id, enrollment_date, grade

## Configuration

### Server Port
The server defaults to port 8081. Set the `PORT` environment variable to change it.

### Android Network Security
The Android app is configured to allow cleartext HTTP traffic to localhost addresses for development. For production, use HTTPS.

## Screenshots

The app features a bottom navigation bar with three screens:
- **Students** - Manage student records
- **Courses** - Manage course catalog
- **Enroll** - Create enrollments and assign grades

## License

This project is for educational purposes.
