package com.example.surakshasaathi.ui.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.surakshasaathi.riskengine.LocalReputationDb
import com.example.surakshasaathi.theme.*
import kotlinx.coroutines.launch
import com.example.surakshasaathi.voice.AppLanguage
import com.example.surakshasaathi.voice.VoiceGuidanceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityReportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceGuidanceManager.getInstance(context) }
    val currentLanguage by voiceManager.currentLanguage.collectAsState()

    var targetInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("number") } // "number" or "domain"
    var selectedCategory by remember { mutableStateOf("Fake Bank") }
    var notesInput by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val categories = listOf(
        "Fake Bank" to "🏦",
        "Fake Police / CBI" to "👮",
        "Fake Relative" to "👨‍👩‍👦",
        "Investment Scam" to "📈",
        "Payment Scam" to "💳",
        "Other" to "⚠️",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.ENGLISH -> "Report a Scam"
                        AppLanguage.HINDI -> "धोखाधड़ी की रिपोर्ट करें"
                        AppLanguage.ODIA -> "ଠକେଇ ରିପୋର୍ଟ କରନ୍ତୁ"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(StopRed, StopRed.copy(alpha = 0.7f))
                    )
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text("📢", fontSize = 34.sp)
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Community Safety Shield",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Report suspicious numbers and links so other seniors are protected.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            // ── Cloud Threat Intelligence Sync Card ──
            val syncStatus by LocalReputationDb.syncStatus.collectAsState()
            var isSyncing by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            var showServerConfig by remember { mutableStateOf(false) }
            var serverUrlInput by remember { mutableStateOf(com.example.surakshasaathi.network.SurakshaApiClient.baseUrl) }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                tonalElevation = 2.dp,
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("☁️", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Cloud Threat Engine",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = syncStatus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (syncStatus.contains("Synced")) SafeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isSyncing = true
                                    LocalReputationDb.syncThreatUpdates()
                                    isSyncing = false
                                }
                            },
                            enabled = !isSyncing,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Text("Sync Now", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Backend: FastAPI + PostgreSQL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(
                            onClick = { showServerConfig = !showServerConfig },
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text(if (showServerConfig) "Hide Config" else "Configure URL", fontSize = 11.sp)
                        }
                    }

                    if (showServerConfig) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = {
                                serverUrlInput = it
                                com.example.surakshasaathi.network.SurakshaApiClient.baseUrl = it.trim()
                            },
                            label = { Text("Server URL", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Type selector: Number vs Domain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FilterChip(
                    selected = selectedType == "number",
                    onClick = { selectedType = "number" },
                    label = { Text("📞 Phone Number") },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = selectedType == "domain",
                    onClick = { selectedType = "domain" },
                    label = { Text("🔗 Website Link") },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))

            // Input field
            OutlinedTextField(
                value = targetInput,
                onValueChange = {
                    targetInput = it
                    submitted = false
                },
                label = {
                    Text(if (selectedType == "number") "Phone number (+91...)" else "Website link (example.xyz)")
                },
                placeholder = {
                    Text(if (selectedType == "number") "+91 98765 43210" else "sbi-kyc-update.xyz")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
            )

            Spacer(Modifier.height(16.dp))

            // Category selection
            Text(
                text = "Select Scam Category:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.chunked(2).forEach { rowCats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowCats.forEach { (cat, emoji) ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) StopRed else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedCategory = cat },
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(emoji, fontSize = 18.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Description notes
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("What did they say? (Optional)") },
                placeholder = { Text("e.g. Asked for OTP claiming to be electricity officer") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3,
            )

            Spacer(Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    if (targetInput.isNotBlank()) {
                        LocalReputationDb.addCommunityReport(
                            LocalReputationDb.CommunityReport(
                                target = targetInput.trim(),
                                targetType = selectedType,
                                category = selectedCategory,
                                description = notesInput.trim(),
                            )
                        )
                        submitted = true
                        targetInput = ""
                        notesInput = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StopRed,
                ),
                enabled = targetInput.isNotBlank(),
            ) {
                Text(
                    text = "🚨  Submit Community Report",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 17.sp,
                )
            }

            AnimatedVisibility(visible = submitted) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SafeGreenLight,
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Report saved successfully! Other users will now be warned against this threat.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SafeGreen,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // List of Recent Threat Reports
            Text(
                text = "Recent Community Intelligence:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))

            val recentReports = LocalReputationDb.getRecentCommunityReports()
            if (recentReports.isEmpty()) {
                Text(
                    text = "No community reports submitted in this session yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                recentReports.forEach { rep ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = rep.target,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = rep.category,
                                    color = StopRed,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            if (rep.description.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = rep.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
