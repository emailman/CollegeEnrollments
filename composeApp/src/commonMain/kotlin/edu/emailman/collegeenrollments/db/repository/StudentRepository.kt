package edu.emailman.collegeenrollments.db.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import edu.emailman.collegeenrollments.Student
import edu.emailman.collegeenrollments.db.CollegeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface StudentRepository {
    fun getAllStudents(): Flow<List<Student>>
    fun getStudentById(id: Long): Flow<Student?>
    suspend fun getStudentByEmail(email: String): Student?
    suspend fun insertStudent(name: String, email: String)
    suspend fun updateStudent(id: Long, name: String, email: String)
    suspend fun deleteStudent(id: Long)
    suspend fun getStudentCount(): Long
}

class StudentRepositoryImpl(
    private val database: CollegeDatabase
) : StudentRepository {

    private val queries = database.collegeEnrollmentsQueries

    override fun getAllStudents(): Flow<List<Student>> {
        return queries.selectAllStudents().asFlow().mapToList(Dispatchers.IO)
    }

    override fun getStudentById(id: Long): Flow<Student?> {
        return queries.selectStudentById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    override suspend fun getStudentByEmail(email: String): Student? {
        return withContext(Dispatchers.IO) {
            queries.selectStudentByEmail(email).executeAsOneOrNull()
        }
    }

    override suspend fun insertStudent(name: String, email: String) {
        withContext(Dispatchers.IO) {
            queries.insertStudent(name, email)
        }
    }

    override suspend fun updateStudent(id: Long, name: String, email: String) {
        withContext(Dispatchers.IO) {
            queries.updateStudent(name, email, id)
        }
    }

    override suspend fun deleteStudent(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteStudent(id)
        }
    }

    override suspend fun getStudentCount(): Long {
        return withContext(Dispatchers.IO) {
            queries.countStudents().executeAsOne()
        }
    }
}
