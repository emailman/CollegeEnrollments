package edu.emailman.collegeenrollments.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

object DatabaseFactory {
    private var database: CollegeDatabase? = null

    fun getDatabase(): CollegeDatabase {
        if (database == null) {
            // Create database directory if it doesn't exist
            val dbDir = File("data")
            if (!dbDir.exists()) {
                dbDir.mkdirs()
            }

            val dbFile = File("data/college_enrollments.db")
            val isNewDb = !dbFile.exists()

            val driver = JdbcSqliteDriver("jdbc:sqlite:data/college_enrollments.db")

            // Only create tables if this is a new database
            if (isNewDb) {
                CollegeDatabase.Schema.create(driver)
            }

            database = CollegeDatabase(driver)
        }
        return database!!
    }
}
