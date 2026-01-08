package edu.emailman.collegeenrollments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import edu.emailman.collegeenrollments.api.*
import edu.emailman.collegeenrollments.api.viewmodel.*
import edu.emailman.collegeenrollments.ui.screen.web.*
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class Screen(val title: String, val icon: ImageVector) {
    Students("Students", Icons.Default.Person),
    Courses("Courses", Icons.Default.Book),
    Enrollments("Enroll", Icons.Default.School)
}

@Composable
@Preview
fun App(baseUrl: String = "http://localhost:8081") {
    // Create API client and repositories
    val apiClient = remember { ApiClient(baseUrl) }
    val studentRepository = remember { StudentApiRepository(apiClient) }
    val courseRepository = remember { CourseApiRepository(apiClient) }
    val enrollmentRepository = remember { EnrollmentApiRepository(apiClient) }

    // Create ViewModels
    val studentViewModel = remember { StudentApiViewModel(studentRepository) }
    val courseViewModel = remember { CourseApiViewModel(courseRepository) }
    val enrollmentViewModel = remember {
        EnrollmentApiViewModel(enrollmentRepository, studentRepository, courseRepository)
    }

    MaterialTheme {
        var currentScreen by remember { mutableStateOf(Screen.Students) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    Screen.entries.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentScreen == screen,
                            onClick = {
                                currentScreen = screen
                                // Refresh data when switching tabs
                                when (screen) {
                                    Screen.Students -> studentViewModel.refresh()
                                    Screen.Courses -> courseViewModel.refresh()
                                    Screen.Enrollments -> enrollmentViewModel.refresh()
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    Screen.Students -> WebStudentScreen(studentViewModel)
                    Screen.Courses -> WebCourseScreen(courseViewModel)
                    Screen.Enrollments -> WebEnrollmentScreen(enrollmentViewModel)
                }
            }
        }
    }
}
