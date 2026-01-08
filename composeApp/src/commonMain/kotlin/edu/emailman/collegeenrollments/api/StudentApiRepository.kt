package edu.emailman.collegeenrollments.api

class StudentApiRepository(private val apiClient: ApiClient) {

    suspend fun getAllStudents(): List<Student> {
        return apiClient.getAllStudents()
    }

    suspend fun getStudent(id: Long): Student {
        return apiClient.getStudent(id)
    }

    suspend fun insertStudent(name: String, email: String): Student {
        return apiClient.createStudent(CreateStudentRequest(name, email))
    }

    suspend fun updateStudent(id: Long, name: String, email: String): Student {
        return apiClient.updateStudent(id, UpdateStudentRequest(name, email))
    }

    suspend fun deleteStudent(id: Long) {
        apiClient.deleteStudent(id)
    }
}
