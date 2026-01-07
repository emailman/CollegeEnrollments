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
- **JVM SLF4J warning**: Added `slf4j-simple` dependency to `jvmMain`
- **JVM kotlinx-datetime ClassNotFoundException**: Use `java.time.LocalDate` instead (available on both JVM and Android)
- **JVM SQLDelight Flow doesn't auto-notify**: EnrollmentViewModel uses MutableStateFlow with manual `loadEnrollments()` calls after database mutations instead of relying on SQLDelight's Flow

### JVM/Desktop Architecture Note
SQLDelight's JDBC driver on JVM does NOT automatically notify Flow subscribers when data changes (unlike Android driver). The workaround is:
1. Repositories have both `getAllXxx(): Flow` and `suspend getAllXxxList(): List` methods
2. ViewModels use MutableStateFlow and manually reload data after insert/update/delete operations
3. This pattern is used in `EnrollmentViewModel`; `StudentViewModel` and `CourseViewModel` may need similar updates if their lists don't refresh

## Current Status
- **All core features complete**: Students, Courses, Enrollments CRUD working
- **Desktop (JVM)**: Fully functional
- **Android**: Should work but needs testing after recent JVM fixes

## Potential Enhancements
- Search/filter on student/course lists
- Dashboard with statistics
- Grade picker dropdown (A, B+, B, etc.) instead of free text
- Student detail view showing all enrollments
- Apply MutableStateFlow pattern to StudentViewModel/CourseViewModel if needed

## Build Commands
```shell
# Desktop
./gradlew :composeApp:run

# Android
./gradlew :composeApp:assembleDebug
```

## GitHub
Repository: https://github.com/emailman/CollegeEnrollments
