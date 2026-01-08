package edu.emailman.collegeenrollments

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // For Android emulator, use 10.0.2.2 to access host machine's localhost
        // For real device on same network, use the host machine's IP address
        val baseUrl = "http://10.0.2.2:8081"

        setContent {
            App(baseUrl)
        }
    }
}
