package edu.emailman.collegeenrollments.db.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import edu.emailman.collegeenrollments.Enrollment
import edu.emailman.collegeenrollments.SelectEnrollmentsByCourse
import edu.emailman.collegeenrollments.SelectEnrollmentsByStudent
import edu.emailman.collegeenrollments.db.CollegeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface EnrollmentRepository {
    fun getAllEnrollments(): Flow<List<Enrollment>>
    fun getEnrollmentById(id: Long): Flow<Enrollment?>
    fun getEnrollmentsByStudent(studentId: Long): Flow<List<SelectEnrollmentsByStudent>>
    fun getEnrollmentsByCourse(courseId: Long): Flow<List<SelectEnrollmentsByCourse>>
    suspend fun insertEnrollment(studentId: Long, courseId: Long, enrollmentDate: String, grade: String?)
    suspend fun updateEnrollmentGrade(id: Long, grade: String?)
    suspend fun deleteEnrollment(id: Long)
    suspend fun deleteEnrollmentByStudentAndCourse(studentId: Long, courseId: Long)
    suspend fun getEnrollmentCount(): Long
    suspend fun getStudentCountInCourse(courseId: Long): Long
    suspend fun getCourseCountForStudent(studentId: Long): Long
}

class EnrollmentRepositoryImpl(
    private val database: CollegeDatabase
) : EnrollmentRepository {

    private val queries = database.collegeEnrollmentsQueries

    override fun getAllEnrollments(): Flow<List<Enrollment>> {
        return queries.selectAllEnrollments().asFlow().mapToList(Dispatchers.IO)
    }

    override fun getEnrollmentById(id: Long): Flow<Enrollment?> {
        return queries.selectEnrollmentById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    override fun getEnrollmentsByStudent(studentId: Long): Flow<List<SelectEnrollmentsByStudent>> {
        return queries.selectEnrollmentsByStudent(studentId).asFlow().mapToList(Dispatchers.IO)
    }

    override fun getEnrollmentsByCourse(courseId: Long): Flow<List<SelectEnrollmentsByCourse>> {
        return queries.selectEnrollmentsByCourse(courseId).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun insertEnrollment(
        studentId: Long,
        courseId: Long,
        enrollmentDate: String,
        grade: String?
    ) {
        withContext(Dispatchers.IO) {
            queries.insertEnrollment(studentId, courseId, enrollmentDate, grade)
        }
    }

    override suspend fun updateEnrollmentGrade(id: Long, grade: String?) {
        withContext(Dispatchers.IO) {
            queries.updateEnrollmentGrade(grade, id)
        }
    }

    override suspend fun deleteEnrollment(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteEnrollment(id)
        }
    }

    override suspend fun deleteEnrollmentByStudentAndCourse(studentId: Long, courseId: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteEnrollmentByStudentAndCourse(studentId, courseId)
        }
    }

    override suspend fun getEnrollmentCount(): Long {
        return withContext(Dispatchers.IO) {
            queries.countEnrollments().executeAsOne()
        }
    }

    override suspend fun getStudentCountInCourse(courseId: Long): Long {
        return withContext(Dispatchers.IO) {
            queries.countStudentsInCourse(courseId).executeAsOne()
        }
    }

    override suspend fun getCourseCountForStudent(studentId: Long): Long {
        return withContext(Dispatchers.IO) {
            queries.countCoursesForStudent(studentId).executeAsOne()
        }
    }
}
