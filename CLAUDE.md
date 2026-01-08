# College Enrollments Project

## Overview
A Kotlin Multiplatform application for managing college student enrollments. Runs on Android, Desktop (JVM), and Web (wasmJs) with a shared Ktor backend.

## Tech Stack
- Kotlin Multiplatform
- Compose Multiplatform (Material 3)
- Ktor Server (backend with SQLDelight database)
- Ktor Client (HTTP client for all platforms)
- kotlinx.serialization for JSON
- MVVM architecture with ViewModels and Repositories

## Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                        Clients                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Android   │  │   Desktop   │  │    Web (wasmJs)     │  │
│  │  (10.0.2.2) │  │ (localhost) │  │    (localhost)      │  │
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

## Project Structure
```
CollegeEnrollments/
├── server/                      # Ktor backend server
│   └── src/main/kotlin/.../
│       ├── Application.kt       # Server entry point
│       ├── db/
│       │   └── DatabaseFactory.kt
│       ├── model/               # DTOs and request/response classes
│       └── routes/              # REST API routes
│           ├── StudentRoutes.kt
│           ├── CourseRoutes.kt
│           └── EnrollmentRoutes.kt
│
└── composeApp/                  # Multiplatform UI client
    └── src/
        ├── commonMain/          # Shared code
        │   └── kotlin/.../
        │       ├── App.kt       # Main app with bottom navigation
        │       ├── api/         # API client layer
        │       │   ├── ApiClient.kt
        │       │   ├── Models.kt
        │       │   └── Repositories (StudentApi, CourseApi, EnrollmentApi)
        │       │   └── viewmodel/
        │       │       └── API-based ViewModels
        │       └── ui/screen/web/  # Web-compatible screens
        ├── androidMain/         # Android entry point
        ├── jvmMain/             # Desktop entry point
        └── wasmJsMain/          # Web entry point
```

## API Endpoints

### Students
- `GET /api/students` - List all students
- `GET /api/students/{id}` - Get student by ID
- `POST /api/students` - Create student (body: `{name, email}`)
- `PUT /api/students/{id}` - Update student (body: `{name, email}`)
- `DELETE /api/students/{id}` - Delete student

### Courses
- `GET /api/courses` - List all courses
- `GET /api/courses/{id}` - Get course by ID
- `POST /api/courses` - Create course (body: `{code, name, credits}`)
- `PUT /api/courses/{id}` - Update course (body: `{code, name, credits}`)
- `DELETE /api/courses/{id}` - Delete course

### Enrollments
- `GET /api/enrollments` - List all enrollments (with student/course details)
- `GET /api/enrollments/{id}` - Get enrollment by ID
- `POST /api/enrollments` - Create enrollment (body: `{studentId, courseId, enrollmentDate}`)
- `PUT /api/enrollments/{id}` - Update grade (body: `{grade}`)
- `DELETE /api/enrollments/{id}` - Delete enrollment

## Key Implementation Details

### Server Configuration
- Default port: 8081
- CORS enabled for all origins (development mode)
- SQLite database file: `server/college.db`

### Client Configuration
- Android emulator: `http://10.0.2.2:8081` (maps to host's localhost)
- JVM Desktop: `http://localhost:8081`
- Web: `http://localhost:8081`

### Window Size (Desktop)
- JVM window: 500x900 dp (set in `jvmMain/main.kt`)

### Database Tables
- Student: id, name, email
- Course: id, code, name, credits
- Enrollment: id, student_id, course_id, enrollment_date, grade

### UI Patterns
- Bottom navigation with 3 tabs: Students, Courses, Enrollments
- ModalBottomSheet for add/edit forms
- Empty states with icons (Person, Book, School)
- Forms use `verticalScroll` with 48dp bottom padding

### Known Issues Fixed
- **WasmJs kotlinx-datetime**: `toLocalDateTime()` and `todayIn()` not available on wasmJs - implemented manual date calculation from epoch seconds
- **WasmJs Platform expect/actual**: Added `Platform.wasmJs.kt` with WasmPlatform implementation

## Current Status
- **Server**: Ktor REST API with SQLDelight database - fully functional
- **Desktop (JVM)**: Fully functional with API client
- **Android**: Fully functional with API client (uses 10.0.2.2 for emulator)
- **Web (wasmJs)**: Fully functional with API client

## Build Commands
```shell
# Start the server (required for all clients)
./gradlew :server:run

# Desktop client
./gradlew :composeApp:run

# Android client
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug

# Web client (opens in browser)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

## Running the Full Stack
1. Start the server: `./gradlew :server:run`
2. Run any client:
   - Web: `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` (opens http://localhost:8085)
   - Desktop: `./gradlew :composeApp:run`
   - Android: `./gradlew :composeApp:installDebug`

## Potential Enhancements
- Search/filter on student/course lists
- Dashboard with statistics
- Grade picker dropdown (A, B+, B, etc.) instead of free text
- Student detail view showing all enrollments
- Course detail view showing all enrolled students
- Authentication/authorization
- Production CORS configuration

## GitHub
Repository: https://github.com/emailman/CollegeEnrollments
