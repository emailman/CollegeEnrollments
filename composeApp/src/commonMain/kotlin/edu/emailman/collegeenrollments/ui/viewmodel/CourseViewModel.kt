package edu.emailman.collegeenrollments.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.emailman.collegeenrollments.Course
import edu.emailman.collegeenrollments.db.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

class CourseViewModel(
    private val courseRepository: CourseRepository
) : ViewModel() {

    val courses: StateFlow<List<Course>> = courseRepository.getAllCourses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _formState = MutableStateFlow(CourseFormState())
    val formState: StateFlow<CourseFormState> = _formState.asStateFlow()

    private val _uiEvent = MutableStateFlow<CourseUiEvent?>(null)
    val uiEvent: StateFlow<CourseUiEvent?> = _uiEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun updateCode(code: String) {
        _formState.value = _formState.value.copy(code = code.uppercase())
    }

    fun updateCredits(credits: String) {
        if (credits.isEmpty() || credits.all { it.isDigit() }) {
            _formState.value = _formState.value.copy(credits = credits)
        }
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
        if (credits == null || credits < 1 || credits > 12) {
            _uiEvent.value = CourseUiEvent.ShowError("Credits must be between 1 and 12")
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
                    val existingCourse = courseRepository.getCourseByCode(state.code.trim().uppercase())
                    if (existingCourse != null) {
                        _uiEvent.value = CourseUiEvent.ShowError("A course with this code already exists")
                        _isLoading.value = false
                        return@launch
                    }

                    courseRepository.insertCourse(
                        name = state.name.trim(),
                        code = state.code.trim().uppercase(),
                        credits = credits
                    )
                    _uiEvent.value = CourseUiEvent.ShowSuccess("Course added successfully")
                }
                _formState.value = CourseFormState()
            } catch (e: Exception) {
                _uiEvent.value = CourseUiEvent.ShowError(e.message ?: "An error occurred")
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
            } catch (e: Exception) {
                _uiEvent.value = CourseUiEvent.ShowError(e.message ?: "Failed to delete course")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
