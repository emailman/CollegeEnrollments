package edu.emailman.collegeenrollments.api

class EnrollmentApiRepository(private val apiClient: ApiClient) {

    suspend fun getAllEnrollments(): List<EnrollmentDetail> {
        return apiClient.getAllEnrollments()
    }

    suspend fun getEnrollment(id: Long): EnrollmentDetail {
        return apiClient.getEnrollment(id)
    }

    suspend fun insertEnrollment(
        studentId: Long,
        courseId: Long,
        enrollmentDate: String,
        grade: String?
    ): EnrollmentDetail {
        return apiClient.createEnrollment(
            CreateEnrollmentRequest(studentId, courseId, enrollmentDate, grade)
        )
    }

    suspend fun updateEnrollmentGrade(id: Long, grade: String?): EnrollmentDetail {
        return apiClient.updateEnrollmentGrade(id, UpdateGradeRequest(grade))
    }

    suspend fun deleteEnrollment(id: Long) {
        apiClient.deleteEnrollment(id)
    }
}
