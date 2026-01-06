package edu.emailman.collegeenrollments.db.repository

import edu.emailman.collegeenrollments.db.CollegeDatabase

class RepositoryProvider(database: CollegeDatabase) {
    val studentRepository: StudentRepository = StudentRepositoryImpl(database)
    val courseRepository: CourseRepository = CourseRepositoryImpl(database)
    val enrollmentRepository: EnrollmentRepository = EnrollmentRepositoryImpl(database)
}
