package edu.emailman.collegeenrollments.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.emailman.collegeenrollments.Student
import edu.emailman.collegeenrollments.db.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

class StudentViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {

    val students: StateFlow<List<Student>> = studentRepository.getAllStudents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _formState = MutableStateFlow(StudentFormState())
    val formState: StateFlow<StudentFormState> = _formState.asStateFlow()

    private val _uiEvent = MutableStateFlow<StudentUiEvent?>(null)
    val uiEvent: StateFlow<StudentUiEvent?> = _uiEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
                    val existingStudent = studentRepository.getStudentByEmail(state.email.trim())
                    if (existingStudent != null) {
                        _uiEvent.value = StudentUiEvent.ShowError("A student with this email already exists")
                        _isLoading.value = false
                        return@launch
                    }

                    studentRepository.insertStudent(
                        name = state.name.trim(),
                        email = state.email.trim()
                    )
                    _uiEvent.value = StudentUiEvent.ShowSuccess("Student added successfully")
                }
                _formState.value = StudentFormState()
            } catch (e: Exception) {
                _uiEvent.value = StudentUiEvent.ShowError(e.message ?: "An error occurred")
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
            } catch (e: Exception) {
                _uiEvent.value = StudentUiEvent.ShowError(e.message ?: "Failed to delete student")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
