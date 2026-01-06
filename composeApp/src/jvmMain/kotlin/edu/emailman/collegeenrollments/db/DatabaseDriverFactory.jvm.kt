package edu.emailman.collegeenrollments.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".collegeenrollments")
        databasePath.mkdirs()
        val databaseFile = File(databasePath, "college_enrollments.db")

        val isNewDatabase = !databaseFile.exists()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")

        if (isNewDatabase) {
            CollegeDatabase.Schema.create(driver)
        }

        return driver
    }
}
