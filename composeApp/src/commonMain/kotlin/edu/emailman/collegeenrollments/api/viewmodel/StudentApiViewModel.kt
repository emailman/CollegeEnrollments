package edu.emailman.collegeenrollments.api.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.emailman.collegeenrollments.api.Student
import edu.emailman.collegeenrollments.api.StudentApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentFormState(
    val name: String = "",
    val email: String = "",
    val isEditing: Boolean = false,
    val editingStudentId: Long? = null
)

sealed class StudentUiEvent {
    data class ShowError(val message: String) : StudentUiEvent()
    data class ShowSuccess(val message: String) : StudentUiEvent()
}

class StudentApiViewModel(
    private val studentRepository: StudentApiRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _formState = MutableStateFlow(StudentFormState())
    val formState: StateFlow<StudentFormState> = _formState.asStateFlow()

    private val _uiEvent = MutableStateFlow<StudentUiEvent?>(null)
    val uiEvent: StateFlow<StudentUiEvent?> = _uiEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadStudents()
    }

    fun refresh() {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            try {
                _students.value = studentRepository.getAllStudents()
            } catch (e: Exception) {
                _uiEvent.value = StudentUiEvent.ShowError("Failed to load students: ${e.message}")
            }
        }
    }

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun updateEmail(email: String) {
        _formState.value = _formState.value.copy(email = email)
    }

    fun startEditing(student: Student) {
        _formState.value = StudentFormState(
            name = student.name,
            email = student.email,
            isEditing = true,
            editingStudentId = student.id
        )
    }

    fun cancelEditing() {
        _formState.value = StudentFormState()
    }

    fun clearEvent() {
        _uiEvent.value = null
    }

    fun saveStudent() {
        val state = _formState.value

        if (state.name.isBlank()) {
            _uiEvent.value = StudentUiEvent.ShowError("Name is required")
            return
        }

        if (state.email.isBlank()) {
            _uiEvent.value = StudentUiEvent.ShowError("Email is required")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (state.isEditing && state.editingStudentId != null) {
                    studentRepository.updateStudent(
                        id = state.editingStudentId,
                        name = state.name.trim(),
                        email = state.email.trim()
                    )
                    _uiEvent.value = StudentUiEvent.ShowSuccess("Student updated successfully")
                } else {
                    studentRepository.insertStudent(
                        name = state.name.trim(),
                        email = state.email.trim()
                    )
                    _uiEvent.value = StudentUiEvent.ShowSuccess("Student added successfully")
                }
                _formState.value = StudentFormState()
                loadStudents()
            } catch (e: Exception) {
                val message = if (e.message?.contains("409") == true || e.message?.contains("Conflict") == true) {
                    "A student with this email already exists"
                } else {
                    e.message ?: "An error occurred"
                }
                _uiEvent.value = StudentUiEvent.ShowError(message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                studentRepository.deleteStudent(student.id)
                _uiEvent.value = StudentUiEvent.ShowSuccess("Student deleted successfully")
                loadStudents()
            } catch (e: Exception) {
                _uiEvent.value = StudentUiEvent.ShowError(e.message ?: "Failed to delete student")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
