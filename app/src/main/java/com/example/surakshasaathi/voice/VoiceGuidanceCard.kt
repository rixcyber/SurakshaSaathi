package com.example.surakshasaathi.voice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.surakshasaathi.riskengine.RiskResult
import com.example.surakshasaathi.theme.*

/**
 * Reusable Voice Guidance Card placed directly below safety verdict cards.
 * Provides elderly users with a single-tap voice readout of the safety advice
 * in English, Hindi, or Odia.
 */
@Composable
fun VoiceGuidanceCard(
    result: RiskResult,
    contextType: String = "item",
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceGuidanceManager.getInstance(context) }

    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val currentLanguage by voiceManager.currentLanguage.collectAsState()
    val autoSpeak by voiceManager.isAutoSpeakEnabled.collectAsState()

    // Smooth pulsing animation while speaking
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header: Voice Guidance label & language selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔊",
                        fontSize = 20.sp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Voice Guidance"
                            AppLanguage.HINDI -> "आवाज़ में सलाह"
                            AppLanguage.ODIA -> "ସ୍ୱରରେ ପରାମର୍ଶ"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Language quick pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        val isSelected = currentLanguage == lang
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SurakshaPrimary else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    voiceManager.setLanguage(lang)
                                    if (isSpeaking) {
                                        voiceManager.speakRiskResult(result, contextType, forceSpeak = true)
                                    }
                                },
                        ) {
                            Text(
                                text = lang.nativeName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Main Speak / Stop Button
            if (isSpeaking) {
                Button(
                    onClick = { voiceManager.stop() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scale(pulseScale),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StopRed,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Stop Voice Advice"
                            AppLanguage.HINDI -> "आवाज़ रोकें"
                            AppLanguage.ODIA -> "ସ୍ୱର ବନ୍ଦ କରନ୍ତୁ"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 17.sp,
                    )
                }
            } else {
                Button(
                    onClick = {
                        voiceManager.speakRiskResult(result, contextType, forceSpeak = true)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurakshaPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen",
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Listen to Safety Advice"
                            AppLanguage.HINDI -> "आवाज़ में सलाह सुनें"
                            AppLanguage.ODIA -> "ସ୍ୱରରେ ପରାମର୍ଶ ଶୁଣନ୍ତୁ"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 17.sp,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Subtitle hint explaining what will be said
            Text(
                text = voiceManager.buildSpokenMessage(result, contextType, currentLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
