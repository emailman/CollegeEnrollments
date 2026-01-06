package edu.emailman.collegeenrollments.db

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DatabaseDriverFactory): CollegeDatabase {
    val driver = driverFactory.createDriver()
    return CollegeDatabase(driver)
}
