package com.example.surakshasaathi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.surakshasaathi.services.NotificationHelper
import com.example.surakshasaathi.theme.SurakshaSaathiTheme
import com.example.surakshasaathi.voice.VoiceGuidanceManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channels for Call Shield and Message Shield warnings
        NotificationHelper.createNotificationChannels(this)

        // Warm up Voice Guidance TTS engine
        VoiceGuidanceManager.getInstance(this)

        enableEdgeToEdge()
        setContent {
            SurakshaSaathiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainNavigation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VoiceGuidanceManager.getInstance(this).stop()
    }
}
