package com.example.surakshasaathi.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.surakshasaathi.riskengine.RiskVerdict
import com.example.surakshasaathi.theme.*

/**
 * Senior-friendly Voice Assistant modal:
 * "Ask Saathi: Is This Safe?" ("साथी से पूछें: क्या यह सुरक्षित है?")
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantDialog(
    onDismiss: () -> Unit,
    onAskFamily: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val assistant = remember { VoiceAssistantManager.getInstance(context) }
    val voiceManager = remember { VoiceGuidanceManager.getInstance(context) }

    val isListening by assistant.isListening.collectAsState()
    val speechError by assistant.speechError.collectAsState()
    val currentResult by assistant.currentInquiryResult.collectAsState()
    val currentLanguage by voiceManager.currentLanguage.collectAsState()
    val isSpeaking by voiceManager.isSpeaking.collectAsState()

    var recognizedQueryText by remember { mutableStateOf("") }

    // Microphone permission request
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            assistant.startListening(currentLanguage) { text ->
                recognizedQueryText = text
            }
        }
    }

    // Mic ripple animation
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    ModalBottomSheet(
        onDismissRequest = {
            assistant.stopListening()
            voiceManager.stop()
            onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header with language selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Ask Saathi: Is This Safe?"
                            AppLanguage.HINDI -> "साथी से पूछें: क्या यह सुरक्षित है?"
                            AppLanguage.ODIA -> "ସାଥୀଙ୍କୁ ପଚାରନ୍ତୁ: ଏହା ସୁରକ୍ଷିତ କି?"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SurakshaPrimary,
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Speak or tap a doubt to verify"
                            AppLanguage.HINDI -> "बोलें या संदेह की जांच करें"
                            AppLanguage.ODIA -> "କହି କିମ୍ବା ଟ୍ୟାପ୍ କରି ଯାଞ୍ଚ କରନ୍ତୁ"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = {
                    assistant.stopListening()
                    voiceManager.stop()
                    onDismiss()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Language picker chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                AppLanguage.entries.forEach { lang ->
                    val isSelected = currentLanguage == lang
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SurakshaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                voiceManager.setLanguage(lang)
                                currentResult?.let { res ->
                                    assistant.analyzeQuery(res.query, lang)
                                }
                            },
                    ) {
                        Text(
                            text = "${lang.nativeName} (${lang.displayName})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Large Microphone Circle
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(micScale)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            if (isListening) listOf(StopRed, StopRed.copy(alpha = 0.7f))
                            else listOf(SurakshaPrimary, SurakshaPrimaryDark)
                        )
                    )
                    .clickable {
                        if (isListening) {
                            assistant.stopListening()
                        } else {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                assistant.startListening(currentLanguage) { text ->
                                    recognizedQueryText = text
                                }
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp),
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (isListening) {
                    when (currentLanguage) {
                        AppLanguage.ENGLISH -> "Listening… Speak now"
                        AppLanguage.HINDI -> "सुन रहे हैं… अब बोलें"
                        AppLanguage.ODIA -> "ଶୁଣୁଛି… ଏବେ କୁହନ୍ତୁ"
                    }
                } else {
                    when (currentLanguage) {
                        AppLanguage.ENGLISH -> "Tap microphone to speak"
                        AppLanguage.HINDI -> "बोलने के लिए माइक दबाएं"
                        AppLanguage.ODIA -> "କହିବାକୁ ମାଇକ୍ ଦବାନ୍ତୁ"
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isListening) StopRed else MaterialTheme.colorScheme.onSurface,
            )

            speechError?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = StopRed,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(20.dp))

            // Display live analysis result if available
            currentResult?.let { result ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = when (result.riskResult.verdict) {
                        RiskVerdict.STOP -> StopRedLight
                        RiskVerdict.VERIFY -> VerifyAmberLight
                        RiskVerdict.PROCEED -> SafeGreenLight
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = when (result.riskResult.verdict) {
                                RiskVerdict.STOP -> "🛑  STOP / रुकें"
                                RiskVerdict.VERIFY -> "⚠️  VERIFY / जांचें"
                                RiskVerdict.PROCEED -> "✅  PROCEED / सुरक्षित"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = when (result.riskResult.verdict) {
                                RiskVerdict.STOP -> StopRed
                                RiskVerdict.VERIFY -> VerifyAmber
                                RiskVerdict.PROCEED -> SafeGreen
                            },
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "\"${result.query}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = result.spokenExplanation,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Button(
                                onClick = {
                                    voiceManager.speakText(result.spokenExplanation)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurakshaPrimary,
                                ),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, "Listen")
                                Spacer(Modifier.width(6.dp))
                                Text("Listen Again")
                            }

                            OutlinedButton(
                                onClick = onAskFamily,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text("👨‍👩‍👦 Ask Family")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Quick scenario tap chips for elderly convenience
            Text(
                text = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "Or tap a common question:"
                    AppLanguage.HINDI -> "या किसी आम सवाल पर टैप करें:"
                    AppLanguage.ODIA -> "କିମ୍ବା ସାଧାରଣ ପ୍ରଶ୍ନ ବାଛନ୍ତୁ:"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))

            VoiceAssistantManager.QUICK_PROMPTS.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            assistant.analyzeQuery(prompt.queryText, currentLanguage)
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(prompt.emoji, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> prompt.title
                                    AppLanguage.HINDI -> prompt.titleHi
                                    AppLanguage.ODIA -> prompt.titleOr
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text("➔", color = SurakshaPrimary, fontSize = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
