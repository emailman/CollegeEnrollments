package edu.emailman.collegeenrollments.api.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.emailman.collegeenrollments.api.Course
import edu.emailman.collegeenrollments.api.CourseApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CourseFormState(
    val name: String = "",
    val code: String = "",
    val credits: String = "3",
    val isEditing: Boolean = false,
    val editingCourseId: Long? = null
)

sealed class CourseUiEvent {
    data class ShowError(val message: String) : CourseUiEvent()
    data class ShowSuccess(val message: String) : CourseUiEvent()
}

class CourseApiViewModel(
    private val courseRepository: CourseApiRepository
) : ViewModel() {

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _formState = MutableStateFlow(CourseFormState())
    val formState: StateFlow<CourseFormState> = _formState.asStateFlow()

    private val _uiEvent = MutableStateFlow<CourseUiEvent?>(null)
    val uiEvent: StateFlow<CourseUiEvent?> = _uiEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCourses()
    }

    fun refresh() {
        loadCourses()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            try {
                _courses.value = courseRepository.getAllCourses()
            } catch (e: Exception) {
                _uiEvent.value = CourseUiEvent.ShowError("Failed to load courses: ${e.message}")
            }
        }
    }

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun updateCode(code: String) {
        _formState.value = _formState.value.copy(code = code.uppercase())
    }

    fun updateCredits(credits: String) {
        _formState.value = _formState.value.copy(credits = credits)
    }

    fun startEditing(course: Course) {
        _formState.value = CourseFormState(
            name = course.name,
            code = course.code,
            credits = course.credits.toString(),
            isEditing = true,
            editingCourseId = course.id
        )
    }

    fun cancelEditing() {
        _formState.value = CourseFormState()
    }

    fun clearEvent() {
        _uiEvent.value = null
    }

    fun saveCourse() {
        val state = _formState.value

        if (state.name.isBlank()) {
            _uiEvent.value = CourseUiEvent.ShowError("Course name is required")
            return
        }

        if (state.code.isBlank()) {
            _uiEvent.value = CourseUiEvent.ShowError("Course code is required")
            return
        }

        val credits = state.credits.toLongOrNull()
        if (credits == null || credits < 1 || credits > 6) {
            _uiEvent.value = CourseUiEvent.ShowError("Credits must be between 1 and 6")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (state.isEditing && state.editingCourseId != null) {
                    courseRepository.updateCourse(
                        id = state.editingCourseId,
                        name = state.name.trim(),
                        code = state.code.trim().uppercase(),
                        credits = credits
                    )
                    _uiEvent.value = CourseUiEvent.ShowSuccess("Course updated successfully")
                } else {
                    courseRepository.insertCourse(
                        name = state.name.trim(),
                        code = state.code.trim().uppercase(),
                        credits = credits
                    )
                    _uiEvent.value = CourseUiEvent.ShowSuccess("Course added successfully")
                }
                _formState.value = CourseFormState()
                loadCourses()
            } catch (e: Exception) {
                val message = if (e.message?.contains("409") == true || e.message?.contains("Conflict") == true) {
                    "A course with this code already exists"
                } else {
                    e.message ?: "An error occurred"
                }
                _uiEvent.value = CourseUiEvent.ShowError(message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                courseRepository.deleteCourse(course.id)
                _uiEvent.value = CourseUiEvent.ShowSuccess("Course deleted successfully")
                loadCourses()
            } catch (e: Exception) {
                _uiEvent.value = CourseUiEvent.ShowError(e.message ?: "Failed to delete course")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
