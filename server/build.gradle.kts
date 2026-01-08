plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    application
}

group = "edu.emailman.collegeenrollments"
version = "1.0.0"

application {
    mainClass.set("edu.emailman.collegeenrollments.ApplicationKt")
}

dependencies {
    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Logging
    implementation(libs.logback.classic)

    // SQLDelight
    implementation(libs.sqldelight.sqlite.driver)
    implementation(libs.sqldelight.coroutines)

    // Kotlinx
    implementation(libs.kotlinx.serialization.json)
}

sqldelight {
    databases {
        create("CollegeDatabase") {
            packageName.set("edu.emailman.collegeenrollments.db")
            verifyMigrations.set(false)
        }
    }
}

// Skip migration verification task due to Windows SQLite driver issue
tasks.matching { it.name.contains("verifyMainCollegeDatabaseMigration") }.configureEach {
    enabled = false
}
