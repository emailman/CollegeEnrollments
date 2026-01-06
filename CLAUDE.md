# College Enrollments Project

## Overview
A Kotlin Multiplatform application for managing college student enrollments. Runs on Android and Desktop (JVM).

## Tech Stack
- Kotlin Multiplatform
- Compose Multiplatform (Material 3)
- SQLDelight for database
- MVVM architecture with ViewModels and Repositories

## Project Structure
```
composeApp/src/
├── commonMain/          # Shared code
│   ├── kotlin/.../
│   │   ├── App.kt                    # Main app with bottom navigation
│   │   ├── db/
│   │   │   ├── DatabaseDriverFactory.kt
│   │   │   └── repository/           # StudentRepository, CourseRepository, EnrollmentRepository
│   │   └── ui/
│   │       ├── screen/               # StudentScreen, CourseScreen, EnrollmentScreen
│   │       └── viewmodel/            # ViewModels for each screen
│   └── sqldelight/.../CollegeEnrollments.sq
├── androidMain/         # Android-specific (DatabaseDriverFactory, MainActivity)
└── jvmMain/             # Desktop-specific (DatabaseDriverFactory, main.kt)
```

## Key Implementation Details

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
- FAB onClick must set `showBottomSheet = true` to open forms
- `onDismissRequest` and `onCancel` must reset sheet state to `false` for FAB to work repeatedly
- Web targets (js, wasmJs) removed due to SQLDelight incompatibility

## Build Commands
```shell
# Desktop
./gradlew :composeApp:run

# Android
./gradlew :composeApp:assembleDebug
```

## GitHub
Repository: https://github.com/emailman/CollegeEnrollments
