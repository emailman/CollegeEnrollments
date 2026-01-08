package edu.emailman.collegeenrollments.ui.screen.web

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.emailman.collegeenrollments.api.Course
import edu.emailman.collegeenrollments.api.EnrollmentDetail
import edu.emailman.collegeenrollments.api.Student
import edu.emailman.collegeenrollments.api.viewmodel.EnrollmentApiViewModel
import edu.emailman.collegeenrollments.api.viewmodel.EnrollmentUiEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebEnrollmentScreen(viewModel: EnrollmentApiViewModel) {
    // Refresh data when screen enters composition to pick up changes from other screens
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val enrollments by viewModel.enrollments.collectAsState()
    val students by viewModel.students.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAddSheet by remember { mutableStateOf(false) }
    var showGradeSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiEvent) {
        uiEvent?.let { event ->
            when (event) {
                is EnrollmentUiEvent.ShowError -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
                is EnrollmentUiEvent.ShowSuccess -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                    showAddSheet = false
                    showGradeSheet = false
                }
            }
            viewModel.clearEvent()
        }
    }

    Box {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Enrollments") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        viewModel.cancelEditing()
                        showAddSheet = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Enrollment")
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            if (enrollments.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.height(64.dp).width(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No enrollments yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to enroll a student in a course",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(enrollments, key = { it.id }) { enrollment ->
                        EnrollmentCard(
                            enrollment = enrollment,
                            onEditGrade = {
                                viewModel.startEditingGrade(enrollment)
                                showGradeSheet = true
                            },
                            onDelete = { viewModel.deleteEnrollment(enrollment) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Add Enrollment Dialog - outside Scaffold
        if (showAddSheet) {
            Dialog(
                onDismissRequest = {
                    showAddSheet = false
                    viewModel.cancelEditing()
                }
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    AddEnrollmentForm(
                        selectedStudent = formState.selectedStudent,
                        selectedCourse = formState.selectedCourse,
                        students = students,
                        courses = courses,
                        showStudentPicker = formState.showStudentPicker,
                        showCoursePicker = formState.showCoursePicker,
                        isLoading = isLoading,
                        onShowStudentPicker = viewModel::showStudentPicker,
                        onSelectStudent = viewModel::selectStudent,
                        onShowCoursePicker = viewModel::showCoursePicker,
                        onSelectCourse = viewModel::selectCourse,
                        onSave = viewModel::saveEnrollment,
                        onCancel = {
                            showAddSheet = false
                            viewModel.cancelEditing()
                        }
                    )
                }
            }
        }

        // Edit Grade Dialog - outside Scaffold
        if (showGradeSheet) {
            Dialog(
                onDismissRequest = {
                    showGradeSheet = false
                    viewModel.cancelEditing()
                }
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    EditGradeForm(
                        grade = formState.grade,
                        isLoading = isLoading,
                        onGradeChange = viewModel::updateGrade,
                        onSave = viewModel::saveEnrollment,
                        onCancel = {
                            showGradeSheet = false
                            viewModel.cancelEditing()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EnrollmentCard(
    enrollment: EnrollmentDetail,
    onEditGrade: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = enrollment.studentName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = enrollment.courseCode,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = enrollment.courseName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Enrolled: ${enrollment.enrollmentDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = if (enrollment.grade != null)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = enrollment.grade ?: "No grade",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (enrollment.grade != null)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Row {
                IconButton(onClick = onEditGrade) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Grade",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AddEnrollmentForm(
    selectedStudent: Student?,
    selectedCourse: Course?,
    students: List<Student>,
    courses: List<Course>,
    showStudentPicker: Boolean,
    showCoursePicker: Boolean,
    isLoading: Boolean,
    onShowStudentPicker: () -> Unit,
    onSelectStudent: (Student) -> Unit,
    onShowCoursePicker: () -> Unit,
    onSelectCourse: (Course) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "New Enrollment",
            style = MaterialTheme.typography.headlineSmall
        )

        // Student Picker
        Text(
            text = "Student",
            style = MaterialTheme.typography.labelLarge
        )

        if (showStudentPicker) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(students) { student ->
                        ListItem(
                            headlineContent = { Text(student.name) },
                            supportingContent = { Text(student.email) },
                            modifier = Modifier.clickable {
                                onSelectStudent(student)
                            }
                        )
                        HorizontalDivider()
                    }
                    if (students.isEmpty()) {
                        item {
                            Text(
                                text = "No students available. Add students first.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) { onShowStudentPicker() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedStudent != null) {
                        Column {
                            Text(
                                text = selectedStudent.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = selectedStudent.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "Select a student",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        }

        // Course Picker
        Text(
            text = "Course",
            style = MaterialTheme.typography.labelLarge
        )

        if (showCoursePicker) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(courses) { course ->
                        ListItem(
                            headlineContent = { Text(course.name) },
                            supportingContent = { Text("${course.code} - ${course.credits} credits") },
                            modifier = Modifier.clickable {
                                onSelectCourse(course)
                            }
                        )
                        HorizontalDivider()
                    }
                    if (courses.isEmpty()) {
                        item {
                            Text(
                                text = "No courses available. Add courses first.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) { onShowCoursePicker() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedCourse != null) {
                        Column {
                            Text(
                                text = selectedCourse.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${selectedCourse.code} - ${selectedCourse.credits} credits",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "Select a course",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSave,
                enabled = !isLoading && selectedStudent != null && selectedCourse != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp).width(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enroll")
                }
            }
        }
    }
}

@Composable
private fun EditGradeForm(
    grade: String,
    isLoading: Boolean,
    onGradeChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit Grade",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = grade,
            onValueChange = onGradeChange,
            label = { Text("Grade") },
            placeholder = { Text("e.g., A, B+, C") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Text(
            text = "Leave empty to clear the grade",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSave,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp).width(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save")
                }
            }
        }
    }
}
