package edu.emailman.collegeenrollments.api

class CourseApiRepository(private val apiClient: ApiClient) {

    suspend fun getAllCourses(): List<Course> {
        return apiClient.getAllCourses()
    }

    suspend fun getCourse(id: Long): Course {
        return apiClient.getCourse(id)
    }

    suspend fun insertCourse(name: String, code: String, credits: Long): Course {
        return apiClient.createCourse(CreateCourseRequest(name, code, credits))
    }

    suspend fun updateCourse(id: Long, name: String, code: String, credits: Long): Course {
        return apiClient.updateCourse(id, UpdateCourseRequest(name, code, credits))
    }

    suspend fun deleteCourse(id: Long) {
        apiClient.deleteCourse(id)
    }
}
