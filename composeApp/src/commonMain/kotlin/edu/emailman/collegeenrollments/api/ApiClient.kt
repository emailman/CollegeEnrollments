package edu.emailman.collegeenrollments.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient(
    private val baseUrl: String = "http://localhost:8081"
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    // Student API
    suspend fun getAllStudents(): List<Student> {
        return client.get("$baseUrl/api/students").body()
    }

    suspend fun getStudent(id: Long): Student {
        return client.get("$baseUrl/api/students/$id").body()
    }

    suspend fun createStudent(request: CreateStudentRequest): Student {
        return client.post("$baseUrl/api/students") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateStudent(id: Long, request: UpdateStudentRequest): Student {
        return client.put("$baseUrl/api/students/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteStudent(id: Long) {
        client.delete("$baseUrl/api/students/$id")
    }

    // Course API
    suspend fun getAllCourses(): List<Course> {
        return client.get("$baseUrl/api/courses").body()
    }

    suspend fun getCourse(id: Long): Course {
        return client.get("$baseUrl/api/courses/$id").body()
    }

    suspend fun createCourse(request: CreateCourseRequest): Course {
        return client.post("$baseUrl/api/courses") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateCourse(id: Long, request: UpdateCourseRequest): Course {
        return client.put("$baseUrl/api/courses/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteCourse(id: Long) {
        client.delete("$baseUrl/api/courses/$id")
    }

    // Enrollment API
    suspend fun getAllEnrollments(): List<EnrollmentDetail> {
        return client.get("$baseUrl/api/enrollments").body()
    }

    suspend fun getEnrollment(id: Long): EnrollmentDetail {
        return client.get("$baseUrl/api/enrollments/$id").body()
    }

    suspend fun createEnrollment(request: CreateEnrollmentRequest): EnrollmentDetail {
        return client.post("$baseUrl/api/enrollments") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateEnrollmentGrade(id: Long, request: UpdateGradeRequest): EnrollmentDetail {
        return client.put("$baseUrl/api/enrollments/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteEnrollment(id: Long) {
        client.delete("$baseUrl/api/enrollments/$id")
    }

    fun close() {
        client.close()
    }
}
