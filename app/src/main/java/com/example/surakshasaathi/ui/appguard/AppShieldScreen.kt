package com.example.surakshasaathi.ui.appguard

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.surakshasaathi.riskengine.DangerousAppDefinition
import com.example.surakshasaathi.riskengine.DetectedDangerousApp
import com.example.surakshasaathi.riskengine.RemoteAppScanner
import com.example.surakshasaathi.riskengine.RiskEvidence
import com.example.surakshasaathi.riskengine.RiskResult
import com.example.surakshasaathi.riskengine.RiskVerdict
import com.example.surakshasaathi.theme.*
import com.example.surakshasaathi.voice.VoiceGuidanceCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShieldScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }
    var detectedApps by remember { mutableStateOf<List<DetectedDangerousApp>>(emptyList()) }

    // Initial scan on first open
    LaunchedEffect(Unit) {
        val results = RemoteAppScanner.scanDevice(context)
        detectedApps = results
        scanCompleted = true
    }

    val installedThreats = detectedApps.filter { it.isInstalled }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    "App Protection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            // Header icon badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(AppShieldColor, AppShieldColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("📱", fontSize = 36.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Remote App Check",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Scammers ask seniors to install screen-sharing apps like AnyDesk to control their phone.\nLet's check if any are installed.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // Scan action button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isScanning = true
                        delay(600) // Brief animation delay for senior visual feedback
                        detectedApps = RemoteAppScanner.scanDevice(context)
                        isScanning = false
                        scanCompleted = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppShieldColor,
                ),
                enabled = !isScanning,
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Scanning Device...",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 18.sp,
                    )
                } else {
                    Text(
                        text = "🔍  Scan My Phone Now",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 18.sp,
                    )
                }
            }

            // Results Section
            AnimatedVisibility(
                visible = scanCompleted && !isScanning,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(24.dp))

                    if (installedThreats.isNotEmpty()) {
                        // High Risk Alert Card
                        ThreatDetectedBanner(
                            threatCount = installedThreats.size,
                        )

                        Spacer(Modifier.height(16.dp))

                        installedThreats.forEach { threat ->
                            ThreatAppCard(
                                app = threat,
                                onUninstall = {
                                    val intent = Intent(Intent.ACTION_DELETE).apply {
                                        data = Uri.parse("package:${threat.definition.packageName}")
                                    }
                                    context.startActivity(intent)
                                },
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    } else {
                        // Safe Card
                        SafeDeviceBanner(scannedCount = detectedApps.size)
                    }

                    val appRiskResult = remember(installedThreats) {
                        if (installedThreats.isNotEmpty()) {
                            RiskResult(
                                score = 90,
                                verdict = RiskVerdict.STOP,
                                evidenceList = installedThreats.map {
                                    RiskEvidence("Dangerous remote access app installed: ${it.definition.name}", 90, "app")
                                }
                            )
                        } else {
                            RiskResult(
                                score = 0,
                                verdict = RiskVerdict.PROCEED,
                                evidenceList = emptyList()
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    VoiceGuidanceCard(result = appRiskResult, contextType = "app scan")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Golden Rules Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = VerifyAmberLight,
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", fontSize = 22.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Safety Rule for Seniors",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VerifyAmber,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "• Never install an app if a caller tells you to.\n" +
                            "• Banks, Electricity boards, and Police will NEVER ask you to install AnyDesk or TeamViewer.\n" +
                            "• If an unknown caller says \"install this app to get your refund\", immediately HANG UP.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VerifyAmber.copy(alpha = 0.9f),
                        lineHeight = 24.sp,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Demo Simulation Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🧪 Demo / Test Simulation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Simulate how Suraksha Saathi detects dangerous remote-control apps during a test.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                val anyDeskDef = RemoteAppScanner.knownRemoteAccessApps.first()
                                detectedApps = listOf(
                                    DetectedDangerousApp(
                                        definition = anyDeskDef,
                                        isInstalled = true,
                                        isSimulated = true,
                                    )
                                )
                                scanCompleted = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text("Simulate AnyDesk", textAlign = TextAlign.Center)
                        }

                        OutlinedButton(
                            onClick = {
                                detectedApps = RemoteAppScanner.scanDevice(context)
                                scanCompleted = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text("Reset Scan", textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ThreatDetectedBanner(threatCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = StopRedLight,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🛑", fontSize = 42.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "STOP! DANGER DETECTED",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = StopRed,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Found $threatCount dangerous remote screen-sharing app(s). Scammers use this to steal bank OTPs. Please uninstall below immediately.",
                style = MaterialTheme.typography.bodyLarge,
                color = StopRed.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SafeDeviceBanner(scannedCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SafeGreenLight,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("✅", fontSize = 42.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "SAFE — All Clear!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SafeGreen,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Scanned $scannedCount known remote access apps. None are installed on your phone.",
                style = MaterialTheme.typography.bodyLarge,
                color = SafeGreen.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ThreatAppCard(
    app: DetectedDangerousApp,
    onUninstall: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StopRedLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(app.definition.emoji, fontSize = 26.sp)
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.definition.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = StopRed,
                    )
                    Text(
                        text = app.definition.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (app.isSimulated) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VerifyAmberLight,
                    ) {
                        Text(
                            text = "DEMO",
                            style = MaterialTheme.typography.labelSmall,
                            color = VerifyAmber,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = app.definition.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "⚠️ How scammers use it: ${app.definition.scamUse}",
                style = MaterialTheme.typography.bodySmall,
                color = StopRed,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onUninstall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StopRed,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Uninstall",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Uninstall ${app.definition.name} Now",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 16.sp,
                    color = Color.White,
                )
            }
        }
    }
}
