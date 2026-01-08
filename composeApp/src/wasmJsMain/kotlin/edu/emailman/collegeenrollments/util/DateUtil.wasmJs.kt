package edu.emailman.collegeenrollments.util

@OptIn(kotlin.js.ExperimentalJsExport::class)
private fun jsGetCurrentDate(): JsString = js("new Date().toISOString().split('T')[0]")

@OptIn(kotlin.js.ExperimentalJsExport::class)
actual fun getCurrentDateString(): String {
    return jsGetCurrentDate().toString()
}
