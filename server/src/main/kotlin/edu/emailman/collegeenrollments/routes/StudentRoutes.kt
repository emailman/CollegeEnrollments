package edu.emailman.collegeenrollments.routes

import edu.emailman.collegeenrollments.db.CollegeDatabase
import edu.emailman.collegeenrollments.model.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.studentRoutes(database: CollegeDatabase) {
    val queries = database.collegeEnrollmentsQueries

    route("/api/students") {
        // GET all students
        get {
            val students = queries.selectAllStudents().executeAsList().map { student ->
                StudentDto(
                    id = student.id,
                    name = student.name,
                    email = student.email
                )
            }
            call.respond(students)
        }

        // GET student by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid student ID"))
                return@get
            }

            val student = queries.selectStudentById(id).executeAsOneOrNull()
            if (student == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Student not found"))
                return@get
            }

            call.respond(StudentDto(
                id = student.id,
                name = student.name,
                email = student.email
            ))
        }

        // POST create student
        post {
            val request = call.receive<CreateStudentRequest>()

            // Check for duplicate email
            val existing = queries.selectStudentByEmail(request.email).executeAsOneOrNull()
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Email already exists"))
                return@post
            }

            queries.insertStudent(request.name, request.email)
            val id = queries.lastInsertId().executeAsOne()

            call.respond(HttpStatusCode.Created, StudentDto(
                id = id,
                name = request.name,
                email = request.email
            ))
        }

        // PUT update student
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid student ID"))
                return@put
            }

            val existing = queries.selectStudentById(id).executeAsOneOrNull()
            if (existing == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Student not found"))
                return@put
            }

            val request = call.receive<UpdateStudentRequest>()

            // Check for duplicate email (excluding current student)
            val emailExists = queries.selectStudentByEmail(request.email).executeAsOneOrNull()
            if (emailExists != null && emailExists.id != id) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Email already exists"))
                return@put
            }

            queries.updateStudent(request.name, request.email, id)

            call.respond(StudentDto(
                id = id,
                name = request.name,
                email = request.email
            ))
        }

        // DELETE student
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid student ID"))
                return@delete
            }

            val existing = queries.selectStudentById(id).executeAsOneOrNull()
            if (existing == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Student not found"))
                return@delete
            }

            queries.deleteStudent(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
