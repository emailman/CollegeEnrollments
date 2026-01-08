package edu.emailman.collegeenrollments.api

import kotlinx.serialization.Serializable

@Serializable
data class Student(
    val id: Long = 0,
    val name: String,
    val email: String
)

@Serializable
data class Course(
    val id: Long = 0,
    val name: String,
    val code: String,
    val credits: Long = 3
)

@Serializable
data class Enrollment(
    val id: Long = 0,
    val studentId: Long,
    val courseId: Long,
    val enrollmentDate: String,
    val grade: String? = null
)

@Serializable
data class EnrollmentDetail(
    val id: Long,
    val studentId: Long,
    val studentName: String,
    val studentEmail: String,
    val courseId: Long,
    val courseName: String,
    val courseCode: String,
    val credits: Long,
    val enrollmentDate: String,
    val grade: String?
)

@Serializable
data class CreateStudentRequest(
    val name: String,
    val email: String
)

@Serializable
data class UpdateStudentRequest(
    val name: String,
    val email: String
)

@Serializable
data class CreateCourseRequest(
    val name: String,
    val code: String,
    val credits: Long = 3
)

@Serializable
data class UpdateCourseRequest(
    val name: String,
    val code: String,
    val credits: Long
)

@Serializable
data class CreateEnrollmentRequest(
    val studentId: Long,
    val courseId: Long,
    val enrollmentDate: String,
    val grade: String? = null
)

@Serializable
data class UpdateGradeRequest(
    val grade: String?
)

@Serializable
data class ErrorResponse(
    val message: String
)
