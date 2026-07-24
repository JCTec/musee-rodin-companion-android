package com.museerodin.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.museerodin.companion.ui.RodinApp
import com.museerodin.companion.ui.theme.RodinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MuseeRodinApplication
        setContent {
            RodinTheme {
                RodinApp(
                    contentRepository = app.contentRepository,
                    userRepository = app.userRepository,
                    narrationController = app.narrationController,
                )
            }
        }
    }
}

