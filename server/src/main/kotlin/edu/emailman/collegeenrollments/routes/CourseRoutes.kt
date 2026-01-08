package edu.emailman.collegeenrollments.routes

import edu.emailman.collegeenrollments.db.CollegeDatabase
import edu.emailman.collegeenrollments.model.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.courseRoutes(database: CollegeDatabase) {
    val queries = database.collegeEnrollmentsQueries

    route("/api/courses") {
        // GET all courses
        get {
            val courses = queries.selectAllCourses().executeAsList().map { course ->
                CourseDto(
                    id = course.id,
                    name = course.name,
                    code = course.code,
                    credits = course.credits
                )
            }
            call.respond(courses)
        }

        // GET course by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid course ID"))
                return@get
            }

            val course = queries.selectCourseById(id).executeAsOneOrNull()
            if (course == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Course not found"))
                return@get
            }

            call.respond(CourseDto(
                id = course.id,
                name = course.name,
                code = course.code,
                credits = course.credits
            ))
        }

        // POST create course
        post {
            val request = call.receive<CreateCourseRequest>()

            // Check for duplicate code
            val existing = queries.selectCourseByCode(request.code).executeAsOneOrNull()
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Course code already exists"))
                return@post
            }

            queries.insertCourse(request.name, request.code, request.credits)
            val id = queries.lastInsertId().executeAsOne()

            call.respond(HttpStatusCode.Created, CourseDto(
                id = id,
                name = request.name,
                code = request.code,
                credits = request.credits
            ))
        }

        // PUT update course
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid course ID"))
                return@put
            }

            val existing = queries.selectCourseById(id).executeAsOneOrNull()
            if (existing == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Course not found"))
                return@put
            }

            val request = call.receive<UpdateCourseRequest>()

            // Check for duplicate code (excluding current course)
            val codeExists = queries.selectCourseByCode(request.code).executeAsOneOrNull()
            if (codeExists != null && codeExists.id != id) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Course code already exists"))
                return@put
            }

            queries.updateCourse(request.name, request.code, request.credits, id)

            call.respond(CourseDto(
                id = id,
                name = request.name,
                code = request.code,
                credits = request.credits
            ))
        }

        // DELETE course
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid course ID"))
                return@delete
            }

            val existing = queries.selectCourseById(id).executeAsOneOrNull()
            if (existing == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Course not found"))
                return@delete
            }

            queries.deleteCourse(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
