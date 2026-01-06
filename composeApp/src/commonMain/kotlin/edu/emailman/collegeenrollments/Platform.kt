package edu.emailman.collegeenrollments

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform