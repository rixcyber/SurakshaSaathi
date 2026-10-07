package com.example.surakshasaathi.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.surakshasaathi.theme.*
import com.example.surakshasaathi.voice.AppLanguage
import com.example.surakshasaathi.voice.VoiceAssistantDialog
import com.example.surakshasaathi.voice.VoiceGuidanceManager

@Composable
fun HomeScreen(
    onCallShield: () -> Unit,
    onMessageShield: () -> Unit,
    onLinkShield: () -> Unit,
    onAppShield: () -> Unit,
    onQrShield: () -> Unit,
    onScreenShield: () -> Unit,
    onCommunityReport: () -> Unit,
    onDemoHub: () -> Unit,
    onTrustedContacts: () -> Unit,
    onEmergency: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceGuidanceManager.getInstance(context) }
    val currentLanguage by voiceManager.currentLanguage.collectAsState()

    var showVoiceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))

        // Language Switcher Chips at the very top for effortless elder access
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "🌐",
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 6.dp),
            )
            AppLanguage.entries.forEach { lang ->
                val isSelected = currentLanguage == lang
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) SurakshaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { voiceManager.setLanguage(lang) },
                ) {
                    Text(
                        text = lang.nativeName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // App header with shield branding
        Text(
            text = "🛡️",
            fontSize = 48.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Suraksha Saathi"
                AppLanguage.HINDI -> "सुरक्षा साथी"
                AppLanguage.ODIA -> "ସୁରକ୍ଷା ସାଥୀ"
            },
            style = MaterialTheme.typography.displayMedium,
            color = SurakshaPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Your Safety Companion"
                AppLanguage.HINDI -> "आपका सुरक्षा साथी"
                AppLanguage.ODIA -> "ଆପଣଙ୍କ ସୁରକ୍ଷା ସାଥୀ"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(20.dp))

        // "Ask Saathi: Is This Safe?" Voice Assistant Featured Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .clickable { showVoiceDialog = true },
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                SurakshaPrimary.copy(alpha = 0.12f),
                                SurakshaPrimaryLight.copy(alpha = 0.22f),
                            )
                        )
                    )
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SurakshaPrimary, SurakshaPrimaryDark)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "🎙️ Ask: \"Is This Safe?\""
                            AppLanguage.HINDI -> "🎙️ पूछें: \"क्या यह सुरक्षित है?\""
                            AppLanguage.ODIA -> "🎙️ ପଚାରନ୍ତୁ: \"ଏହା ସୁରକ୍ଷିତ କି?\""
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SurakshaPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Tap to speak or check common scam doubts"
                            AppLanguage.HINDI -> "संदेह दूर करने के लिए बोलें या टैप करें"
                            AppLanguage.ODIA -> "ସନ୍ଦେହ ଦୂର କରିବାକୁ କୁହନ୍ତୁ କିମ୍ବା ଟ୍ୟାପ୍ କରନ୍ତୁ"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = "🔊",
                    fontSize = 24.sp,
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Question prompt
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurakshaPrimaryLight.copy(alpha = 0.10f),
            tonalElevation = 0.dp,
        ) {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "Are you unsure about something?\nTap below to check if it is safe."
                    AppLanguage.HINDI -> "क्या आपको किसी चीज़ पर शक है?\nजांचने के लिए नीचे टैप करें।"
                    AppLanguage.ODIA -> "ଆପଣଙ୍କର କୌଣସି ସନ୍ଦେହ ଅଛି କି?\nସୁରକ୍ଷିତ କି ନାହିଁ ଜାଣିବାକୁ ତଳେ ଟ୍ୟାପ୍ କରନ୍ତୁ।"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = SurakshaPrimaryDark,
                modifier = Modifier.padding(14.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        // Main protection buttons
        ShieldButton(
            icon = Icons.Default.Call,
            label = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Call Protection"
                AppLanguage.HINDI -> "कॉल सुरक्षा"
                AppLanguage.ODIA -> "କଲ୍ ସୁରକ୍ଷା"
            },
            subtitle = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Check if a call is safe"
                AppLanguage.HINDI -> "जांचें कि कॉल सुरक्षित है या नहीं"
                AppLanguage.ODIA -> "କଲ୍ ସୁରକ୍ଷିତ କି ନାହିଁ ଯାଞ୍ଚ କରନ୍ତୁ"
            },
            color = CallShieldColor,
            onClick = onCallShield,
        )

        Spacer(Modifier.height(14.dp))

        ShieldButton(
            icon = Icons.Default.Email,
            label = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Message Protection"
                AppLanguage.HINDI -> "संदेश सुरक्षा"
                AppLanguage.ODIA -> "ମେସେଜ୍ ସୁରକ୍ଷା"
            },
            subtitle = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Check if a message is safe"
                AppLanguage.HINDI -> "जांचें कि मैसेज सुरक्षित है या नहीं"
                AppLanguage.ODIA -> "ମେସେଜ୍ ସୁରକ୍ଷିତ କି ନାହିଁ ଯାଞ୍ଚ କରନ୍ତୁ"
            },
            color = MessageShieldColor,
            onClick = onMessageShield,
        )

        Spacer(Modifier.height(14.dp))

        ShieldButton(
            icon = LinkIcon,
            label = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Link Protection"
                AppLanguage.HINDI -> "लिंक सुरक्षा"
                AppLanguage.ODIA -> "ଲିଙ୍କ୍ ସୁରକ୍ଷା"
            },
            subtitle = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Check if a link is safe"
                AppLanguage.HINDI -> "जांचें कि लिंक सुरक्षित है या नहीं"
                AppLanguage.ODIA -> "ଲିଙ୍କ୍ ସୁରକ୍ଷିତ କି ନାହିଁ ଯାଞ୍ଚ କରନ୍ତୁ"
            },
            color = LinkShieldColor,
            onClick = onLinkShield,
        )

        Spacer(Modifier.height(14.dp))

        ShieldButton(
            icon = Icons.Default.PhoneAndroid,
            label = when (currentLanguage) {
                AppLanguage.ENGLISH -> "App Protection"
                AppLanguage.HINDI -> "ऐप सुरक्षा"
                AppLanguage.ODIA -> "ଆପ୍ ସୁରକ୍ଷା"
            },
            subtitle = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Check for dangerous remote apps"
                AppLanguage.HINDI -> "खतरनाक रिमोट ऐप्स की जांच करें"
                AppLanguage.ODIA -> "ବିପଦଜନକ ରିମୋଟ୍ ଆପ୍ସ ଯାଞ୍ଚ କରନ୍ତୁ"
            },
            color = AppShieldColor,
            onClick = onAppShield,
        )

        Spacer(Modifier.height(14.dp))

        ShieldButton(
            icon = Icons.Default.QrCodeScanner,
            label = when (currentLanguage) {
                AppLanguage.ENGLISH -> "QR & Payment Protection"
                AppLanguage.HINDI -> "QR व भुगतान सुरक्षा"
                AppLanguage.ODIA -> "QR ଓ ପେମେଣ୍ଟ୍ ସୁରକ୍ଷା"
            },
            subtitle = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Verify UPI requests & QR codes"
                AppLanguage.HINDI -> "UPI अनुरोध और QR कोड सत्यापित करें"
                AppLanguage.ODIA -> "UPI ଅନୁରୋଧ ଓ QR କୋଡ୍ ଯାଞ୍ଚ କରନ୍ତୁ"
            },
            color = QrShieldColor,
            onClick = onQrShield,
        )

        Spacer(Modifier.height(14.dp))

        ShieldButton(
            icon = Icons.Default.Image,
            label = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Screen Protection"
                AppLanguage.HINDI -> "स्क्रीन सुरक्षा"
                AppLanguage.ODIA -> "ସ୍କ୍ରିନ୍ ସୁରକ୍ଷା"
            },
            subtitle = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Check screenshots & suspicious popups"
                AppLanguage.HINDI -> "स्क्रीनशॉट व संदिग्ध पॉपअप की जांच करें"
                AppLanguage.ODIA -> "ସ୍କ୍ରିନସଟ୍ ବା ପପ୍-ଅପ୍ ଯାଞ୍ଚ କରନ୍ତୁ"
            },
            color = SurakshaPrimaryDark,
            onClick = onScreenShield,
        )

        Spacer(Modifier.height(24.dp))

        // Family and Emergency
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ActionCard(
                emoji = "👨‍👩‍👦",
                label = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "Ask Family"
                    AppLanguage.HINDI -> "परिवार से पूछें"
                    AppLanguage.ODIA -> "ପରିବାରକୁ ପଚାରନ୍ତୁ"
                },
                color = FamilyColor,
                onClick = onTrustedContacts,
                modifier = Modifier.weight(1f),
            )
            ActionCard(
                emoji = "🚨",
                label = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "Fraud Help"
                    AppLanguage.HINDI -> "धोखाधड़ी सहायता"
                    AppLanguage.ODIA -> "ଠକେଇ ସହାୟତା"
                },
                color = EmergencyColor,
                onClick = onEmergency,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(14.dp))

        // Community Report & Competition Demo Hub
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ActionCard(
                emoji = "📢",
                label = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "Report Scam"
                    AppLanguage.HINDI -> "रिपोर्ट करें"
                    AppLanguage.ODIA -> "ଠକେଇ ରିପୋର୍ଟ"
                },
                color = StopRed,
                onClick = onCommunityReport,
                modifier = Modifier.weight(1f),
            )
            ActionCard(
                emoji = "🏆",
                label = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "Demo Hub"
                    AppLanguage.HINDI -> "डेमो हब"
                    AppLanguage.ODIA -> "ଡେମୋ ହବ୍"
                },
                color = SurakshaPrimary,
                onClick = onDemoHub,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(32.dp))
    }

    // Modal Voice Assistant Dialog
    if (showVoiceDialog) {
        VoiceAssistantDialog(
            onDismiss = { showVoiceDialog = false },
            onAskFamily = {
                showVoiceDialog = false
                onTrustedContacts()
            },
        )
    }
}

@Composable
private fun ShieldButton(
    icon: ImageVector,
    label: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(color, color.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = "→",
                fontSize = 24.sp,
                color = color,
            )
        }
    }
}

@Composable
private fun ActionCard(
    emoji: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale",
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = emoji,
                fontSize = 36.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val LinkIcon: ImageVector
    get() = Icons.Default.Link
