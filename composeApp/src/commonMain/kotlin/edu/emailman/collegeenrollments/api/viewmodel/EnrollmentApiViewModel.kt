package edu.emailman.collegeenrollments.api.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.emailman.collegeenrollments.api.*
import edu.emailman.collegeenrollments.util.getCurrentDateString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class EnrollmentApiViewModel(
    private val enrollmentRepository: EnrollmentApiRepository,
    private val studentRepository: StudentApiRepository,
    private val courseRepository: CourseApiRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _enrollments = MutableStateFlow<List<EnrollmentDetail>>(emptyList())
    val enrollments: StateFlow<List<EnrollmentDetail>> = _enrollments.asStateFlow()

    private val _formState = MutableStateFlow(EnrollmentFormState())
    val formState: StateFlow<EnrollmentFormState> = _formState.asStateFlow()

    private val _uiEvent = MutableStateFlow<EnrollmentUiEvent?>(null)
    val uiEvent: StateFlow<EnrollmentUiEvent?> = _uiEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAllData()
    }

    fun refresh() {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            try {
                _students.value = studentRepository.getAllStudents()
                _courses.value = courseRepository.getAllCourses()
                _enrollments.value = enrollmentRepository.getAllEnrollments()
            } catch (e: Exception) {
                _uiEvent.value = EnrollmentUiEvent.ShowError("Failed to load data: ${e.message}")
            }
        }
    }

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

    fun startEditingGrade(enrollment: EnrollmentDetail) {
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
                    _uiEvent.value = EnrollmentUiEvent.ShowSuccess("Grade updated successfully")
                    _formState.value = EnrollmentFormState()
                    loadAllData()
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
                    val today = getCurrentDateString()
                    enrollmentRepository.insertEnrollment(
                        studentId = student.id,
                        courseId = course.id,
                        enrollmentDate = today,
                        grade = null
                    )
                    _uiEvent.value = EnrollmentUiEvent.ShowSuccess(
                        "${student.name} enrolled in ${course.code}"
                    )
                    _formState.value = EnrollmentFormState()
                    loadAllData()
                } catch (e: Exception) {
                    val message = if (e.message?.contains("409") == true || e.message?.contains("Conflict") == true) {
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

    fun deleteEnrollment(enrollment: EnrollmentDetail) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                enrollmentRepository.deleteEnrollment(enrollment.id)
                _uiEvent.value = EnrollmentUiEvent.ShowSuccess(
                    "${enrollment.studentName} removed from ${enrollment.courseCode}"
                )
                loadAllData()
            } catch (e: Exception) {
                _uiEvent.value = EnrollmentUiEvent.ShowError(e.message ?: "Failed to delete enrollment")
            } finally {
                _isLoading.value = false
            }
        }
    }

}
