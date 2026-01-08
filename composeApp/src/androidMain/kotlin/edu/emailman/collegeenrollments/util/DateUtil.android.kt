package edu.emailman.collegeenrollments.util

import java.time.LocalDate

actual fun getCurrentDateString(): String {
    return LocalDate.now().toString()
}
