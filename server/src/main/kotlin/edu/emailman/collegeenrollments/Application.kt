package edu.emailman.collegeenrollments

import edu.emailman.collegeenrollments.db.DatabaseFactory
import edu.emailman.collegeenrollments.routes.courseRoutes
import edu.emailman.collegeenrollments.routes.enrollmentRoutes
import edu.emailman.collegeenrollments.routes.studentRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8081
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        configureServer()
    }.start(wait = true)
}

fun Application.configureServer() {
    // Initialize database
    val database = DatabaseFactory.getDatabase()

    // Install CORS for web client
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost() // For development - restrict in production
    }

    // Install JSON serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // Configure routing
    routing {
        // Health check endpoint
        get("/") {
            call.respondText("College Enrollments API is running!")
        }

        get("/health") {
            call.respondText("OK")
        }

        // API routes
        studentRoutes(database)
        courseRoutes(database)
        enrollmentRoutes(database)
    }

    log.info("Server started on http://localhost:${environment.config.propertyOrNull("ktor.deployment.port")?.getString() ?: "8081"}")
}
