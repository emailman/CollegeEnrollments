package edu.emailman.collegeenrollments.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.emailman.collegeenrollments.Course
import edu.emailman.collegeenrollments.Student
import edu.emailman.collegeenrollments.db.repository.CourseRepository
import edu.emailman.collegeenrollments.db.repository.EnrollmentRepository
import edu.emailman.collegeenrollments.db.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EnrollmentDisplay(
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

data class EnrollmentFormState(
    val selectedStudent: Student? = null,
    val selectedCourse: Course? = null,
    val showStudentPicker: Boolean = false,
    val showCoursePicker: Boolean = false,
    val isEditing: Boolean = false,
    val editingEnrollmentId: Long? = null,
    val grade: String = ""
)

sealed class EnrollmentUiEvent {
    data class ShowError(val message: String) : EnrollmentUiEvent()
    data class ShowSuccess(val message: String) : EnrollmentUiEvent()
}

class EnrollmentViewModel(
    private val enrollmentRepository: EnrollmentRepository,
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    // Use MutableStateFlow for manual data management (JVM SQLDelight doesn't auto-notify)
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _enrollments = MutableStateFlow<List<EnrollmentDisplay>>(emptyList())
    val enrollments: StateFlow<List<EnrollmentDisplay>> = _enrollments.asStateFlow()

    init {
        // Load initial data
        loadAllData()
    }

    /**
     * Public method to refresh all data.
     * Call this when navigating to the enrollment screen to pick up
     * any changes made to students/courses on other screens.
     */
    fun refresh() {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            loadStudents()
            loadCourses()
            loadEnrollments()
        }
    }

    private suspend fun loadStudents() {
        _students.value = studentRepository.getAllStudentsList()
    }

    private suspend fun loadCourses() {
        _courses.value = courseRepository.getAllCoursesList()
    }

    private suspend fun loadEnrollments() {
        val enrollmentList = enrollmentRepository.getAllEnrollmentsList()
        val studentMap = _students.value.associateBy { it.id }
        val courseMap = _courses.value.associateBy { it.id }

        _enrollments.value = enrollmentList.mapNotNull { enrollment ->
            val student = studentMap[enrollment.studentId]
            val course = courseMap[enrollment.courseId]

            if (student != null && course != null) {
                EnrollmentDisplay(
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
            } else null
        }.sortedWith(compareBy({ it.studentName }, { it.courseCode }))
    }

    private fun refreshEnrollments() {
        viewModelScope.launch {
            loadStudents()
            loadCourses()
            loadEnrollments()
        }
    }

    private val _formState = MutableStateFlow(EnrollmentFormState())
    val formState: StateFlow<EnrollmentFormState> = _formState.asStateFlow()

    private val _uiEvent = MutableStateFlow<EnrollmentUiEvent?>(null)
    val uiEvent: StateFlow<EnrollmentUiEvent?> = _uiEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun showStudentPicker() {
        _formState.value = _formState.value.copy(showStudentPicker = true)
    }

    fun selectStudent(student: Student) {
        _formState.value = _formState.value.copy(
            selectedStudent = student,
            showStudentPicker = false
        )
    }

    fun showCoursePicker() {
        _formState.value = _formState.value.copy(showCoursePicker = true)
    }

    fun selectCourse(course: Course) {
        _formState.value = _formState.value.copy(
            selectedCourse = course,
            showCoursePicker = false
        )
    }

    fun updateGrade(grade: String) {
        _formState.value = _formState.value.copy(grade = grade.uppercase())
    }

    fun startEditingGrade(enrollment: EnrollmentDisplay) {
        _formState.value = EnrollmentFormState(
            isEditing = true,
            editingEnrollmentId = enrollment.id,
            grade = enrollment.grade ?: ""
        )
    }

    fun cancelEditing() {
        _formState.value = EnrollmentFormState()
    }

    fun clearEvent() {
        _uiEvent.value = null
    }

    fun saveEnrollment() {
        val state = _formState.value

        if (state.isEditing && state.editingEnrollmentId != null) {
            // Update grade
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val grade = state.grade.trim().ifEmpty { null }
                    enrollmentRepository.updateEnrollmentGrade(state.editingEnrollmentId, grade)
                    refreshEnrollments()
                    _uiEvent.value = EnrollmentUiEvent.ShowSuccess("Grade updated successfully")
                    _formState.value = EnrollmentFormState()
                } catch (e: Exception) {
                    _uiEvent.value = EnrollmentUiEvent.ShowError(e.message ?: "Failed to update grade")
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            // Create new enrollment
            val student = state.selectedStudent
            val course = state.selectedCourse

            if (student == null) {
                _uiEvent.value = EnrollmentUiEvent.ShowError("Please select a student")
                return
            }

            if (course == null) {
                _uiEvent.value = EnrollmentUiEvent.ShowError("Please select a course")
                return
            }

            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val today = getCurrentDate()
                    enrollmentRepository.insertEnrollment(
                        studentId = student.id,
                        courseId = course.id,
                        enrollmentDate = today,
                        grade = null
                    )
                    refreshEnrollments()
                    _uiEvent.value = EnrollmentUiEvent.ShowSuccess(
                        "${student.name} enrolled in ${course.code}"
                    )
                    _formState.value = EnrollmentFormState()
                } catch (e: Exception) {
                    val message = if (e.message?.contains("UNIQUE constraint") == true) {
                        "${student.name} is already enrolled in ${course.code}"
                    } else {
                        e.message ?: "Failed to create enrollment"
                    }
                    _uiEvent.value = EnrollmentUiEvent.ShowError(message)
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun deleteEnrollment(enrollment: EnrollmentDisplay) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                enrollmentRepository.deleteEnrollment(enrollment.id)
                refreshEnrollments()
                _uiEvent.value = EnrollmentUiEvent.ShowSuccess(
                    "${enrollment.studentName} removed from ${enrollment.courseCode}"
                )
            } catch (e: Exception) {
                _uiEvent.value = EnrollmentUiEvent.ShowError(e.message ?: "Failed to delete enrollment")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getCurrentDate(): String {
        // Simple date format: YYYY-MM-DD using Java time
        val now = java.time.LocalDate.now()
        return now.toString()
    }
}
