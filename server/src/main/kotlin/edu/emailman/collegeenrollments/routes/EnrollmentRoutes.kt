package edu.emailman.collegeenrollments.routes

import edu.emailman.collegeenrollments.db.CollegeDatabase
import edu.emailman.collegeenrollments.model.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.enrollmentRoutes(database: CollegeDatabase) {
    val queries = database.collegeEnrollmentsQueries

    route("/api/enrollments") {
        // GET all enrollments with details
        get {
            val enrollments = queries.selectAllEnrollments().executeAsList()
            val students = queries.selectAllStudents().executeAsList().associateBy { it.id }
            val courses = queries.selectAllCourses().executeAsList().associateBy { it.id }

            val enrollmentDetails = enrollments.mapNotNull { enrollment ->
                val student = students[enrollment.studentId] ?: return@mapNotNull null
                val course = courses[enrollment.courseId] ?: return@mapNotNull null

                EnrollmentDetailDto(
                    id = enrollment.id,
                    studentId = enrollment.studentId,
                    studentName = student.name,
                    studentEmail = student.email,
                    courseId = enrollment.courseId,
                    courseName = course.name,
                    courseCode = course.code,
                    credits = course.credits,
                    enrollmentDate = enrollment.enrollmentDate,
                    grade = enrollment.grade
                )
            }.sortedWith(compareBy({ it.studentName }, { it.courseCode }))

            call.respond(enrollmentDetails)
        }

        // GET enrollment by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid enrollment ID"))
                return@get
            }

            val enrollment = queries.selectEnrollmentById(id).executeAsOneOrNull()
            if (enrollment == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Enrollment not found"))
                return@get
            }

            val student = queries.selectStudentById(enrollment.studentId).executeAsOneOrNull()
            val course = queries.selectCourseById(enrollment.courseId).executeAsOneOrNull()

            if (student == null || course == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Student or course not found"))
                return@get
            }

            call.respond(EnrollmentDetailDto(
                id = enrollment.id,
                studentId = enrollment.studentId,
                studentName = student.name,
                studentEmail = student.email,
                courseId = enrollment.courseId,
                courseName = course.name,
                courseCode = course.code,
                credits = course.credits,
                enrollmentDate = enrollment.enrollmentDate,
                grade = enrollment.grade
            ))
        }

        // POST create enrollment
        post {
            val request = call.receive<CreateEnrollmentRequest>()

            // Verify student exists
            val student = queries.selectStudentById(request.studentId).executeAsOneOrNull()
            if (student == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Student not found"))
                return@post
            }

            // Verify course exists
            val course = queries.selectCourseById(request.courseId).executeAsOneOrNull()
            if (course == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Course not found"))
                return@post
            }

            try {
                queries.insertEnrollment(
                    request.studentId,
                    request.courseId,
                    request.enrollmentDate,
                    request.grade
                )
                val id = queries.lastInsertId().executeAsOne()

                call.respond(HttpStatusCode.Created, EnrollmentDetailDto(
                    id = id,
                    studentId = request.studentId,
                    studentName = student.name,
                    studentEmail = student.email,
                    courseId = request.courseId,
                    courseName = course.name,
                    courseCode = course.code,
                    credits = course.credits,
                    enrollmentDate = request.enrollmentDate,
                    grade = request.grade
                ))
            } catch (e: Exception) {
                if (e.message?.contains("UNIQUE constraint") == true) {
                    call.respond(HttpStatusCode.Conflict,
                        ErrorResponse("${student.name} is already enrolled in ${course.code}"))
                } else {
                    throw e
                }
            }
        }

        // PUT update enrollment grade
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid enrollment ID"))
                return@put
            }

            val existing = queries.selectEnrollmentById(id).executeAsOneOrNull()
            if (existing == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Enrollment not found"))
                return@put
            }

            val request = call.receive<UpdateGradeRequest>()
            queries.updateEnrollmentGrade(request.grade, id)

            val student = queries.selectStudentById(existing.studentId).executeAsOneOrNull()!!
            val course = queries.selectCourseById(existing.courseId).executeAsOneOrNull()!!

            call.respond(EnrollmentDetailDto(
                id = id,
                studentId = existing.studentId,
                studentName = student.name,
                studentEmail = student.email,
                courseId = existing.courseId,
                courseName = course.name,
                courseCode = course.code,
                credits = course.credits,
                enrollmentDate = existing.enrollmentDate,
                grade = request.grade
            ))
        }

        // DELETE enrollment
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid enrollment ID"))
                return@delete
            }

            val existing = queries.selectEnrollmentById(id).executeAsOneOrNull()
            if (existing == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Enrollment not found"))
                return@delete
            }

            queries.deleteEnrollment(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
