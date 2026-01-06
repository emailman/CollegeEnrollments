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
import edu.emailman.collegeenrollments.db.CollegeDatabase
import edu.emailman.collegeenrollments.db.repository.RepositoryProvider
import edu.emailman.collegeenrollments.ui.screen.CourseScreen
import edu.emailman.collegeenrollments.ui.screen.EnrollmentScreen
import edu.emailman.collegeenrollments.ui.screen.StudentScreen
import edu.emailman.collegeenrollments.ui.viewmodel.CourseViewModel
import edu.emailman.collegeenrollments.ui.viewmodel.EnrollmentViewModel
import edu.emailman.collegeenrollments.ui.viewmodel.StudentViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class Screen(val title: String, val icon: ImageVector) {
    Students("Students", Icons.Default.Person),
    Courses("Courses", Icons.Default.Book),
    Enrollments("Enroll", Icons.Default.School)
}

@Composable
@Preview
fun App(database: CollegeDatabase) {
    val repositoryProvider = remember { RepositoryProvider(database) }
    val studentViewModel = remember { StudentViewModel(repositoryProvider.studentRepository) }
    val courseViewModel = remember { CourseViewModel(repositoryProvider.courseRepository) }
    val enrollmentViewModel = remember {
        EnrollmentViewModel(
            enrollmentRepository = repositoryProvider.enrollmentRepository,
            studentRepository = repositoryProvider.studentRepository,
            courseRepository = repositoryProvider.courseRepository
        )
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
                            onClick = { currentScreen = screen }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    Screen.Students -> StudentScreen(studentViewModel)
                    Screen.Courses -> CourseScreen(courseViewModel)
                    Screen.Enrollments -> EnrollmentScreen(enrollmentViewModel)
                }
            }
        }
    }
}
