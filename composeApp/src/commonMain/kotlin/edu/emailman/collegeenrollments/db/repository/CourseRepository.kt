package edu.emailman.collegeenrollments.db.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import edu.emailman.collegeenrollments.Course
import edu.emailman.collegeenrollments.db.CollegeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface CourseRepository {
    fun getAllCourses(): Flow<List<Course>>
    fun getCourseById(id: Long): Flow<Course?>
    suspend fun getCourseByCode(code: String): Course?
    suspend fun insertCourse(name: String, code: String, credits: Long)
    suspend fun updateCourse(id: Long, name: String, code: String, credits: Long)
    suspend fun deleteCourse(id: Long)
    suspend fun getCourseCount(): Long
}

class CourseRepositoryImpl(
    private val database: CollegeDatabase
) : CourseRepository {

    private val queries = database.collegeEnrollmentsQueries

    override fun getAllCourses(): Flow<List<Course>> {
        return queries.selectAllCourses().asFlow().mapToList(Dispatchers.IO)
    }

    override fun getCourseById(id: Long): Flow<Course?> {
        return queries.selectCourseById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    override suspend fun getCourseByCode(code: String): Course? {
        return withContext(Dispatchers.IO) {
            queries.selectCourseByCode(code).executeAsOneOrNull()
        }
    }

    override suspend fun insertCourse(name: String, code: String, credits: Long) {
        withContext(Dispatchers.IO) {
            queries.insertCourse(name, code, credits)
        }
    }

    override suspend fun updateCourse(id: Long, name: String, code: String, credits: Long) {
        withContext(Dispatchers.IO) {
            queries.updateCourse(name, code, credits, id)
        }
    }

    override suspend fun deleteCourse(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteCourse(id)
        }
    }

    override suspend fun getCourseCount(): Long {
        return withContext(Dispatchers.IO) {
            queries.countCourses().executeAsOne()
        }
    }
}
