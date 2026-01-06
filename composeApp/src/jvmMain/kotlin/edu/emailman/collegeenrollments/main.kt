package edu.emailman.collegeenrollments

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import edu.emailman.collegeenrollments.db.DatabaseDriverFactory
import edu.emailman.collegeenrollments.db.createDatabase

fun main() {
    val driverFactory = DatabaseDriverFactory()
    val database = createDatabase(driverFactory)

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "College Enrollments",
            state = rememberWindowState(width = 500.dp, height = 900.dp)
        ) {
            App(database)
        }
    }
}
