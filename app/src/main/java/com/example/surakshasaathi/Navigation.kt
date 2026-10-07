package com.example.surakshasaathi

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.surakshasaathi.ui.appguard.AppShieldScreen
import com.example.surakshasaathi.ui.callguard.CallShieldScreen
import com.example.surakshasaathi.ui.community.CommunityReportScreen
import com.example.surakshasaathi.ui.demo.DemoHubScreen
import com.example.surakshasaathi.ui.emergency.EmergencyScreen
import com.example.surakshasaathi.ui.home.HomeScreen
import com.example.surakshasaathi.ui.linkguard.LinkShieldScreen
import com.example.surakshasaathi.ui.messageguard.MessageShieldScreen
import com.example.surakshasaathi.ui.qrguard.QrShieldScreen
import com.example.surakshasaathi.ui.screenshield.ScreenShieldScreen
import com.example.surakshasaathi.ui.trustedcontacts.TrustedContactsScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                HomeScreen(
                    onCallShield = { backStack.add(CallShieldScreen) },
                    onMessageShield = { backStack.add(MessageShieldScreen) },
                    onLinkShield = { backStack.add(LinkShieldScreen) },
                    onAppShield = { backStack.add(AppShieldScreen) },
                    onQrShield = { backStack.add(QrShieldScreen) },
                    onScreenShield = { backStack.add(ScreenShieldScreen) },
                    onCommunityReport = { backStack.add(CommunityReportScreen) },
                    onDemoHub = { backStack.add(DemoHubScreen) },
                    onTrustedContacts = { backStack.add(TrustedContactsScreen) },
                    onEmergency = { backStack.add(EmergencyScreen) },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            entry<CallShieldScreen> {
                CallShieldScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            entry<MessageShieldScreen> {
                MessageShieldScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToResult = { /* Results shown inline */ },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            entry<LinkShieldScreen> {
                LinkShieldScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToResult = { /* Results shown inline */ },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            entry<AppShieldScreen> {
                AppShieldScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            entry<QrShieldScreen> {
                QrShieldScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            entry<ScreenShieldScreen> {
                ScreenShieldScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            entry<CommunityReportScreen> {
                CommunityReportScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            entry<DemoHubScreen> {
                DemoHubScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToLinkShield = { backStack.add(LinkShieldScreen) },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            entry<TrustedContactsScreen> {
                TrustedContactsScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            entry<EmergencyScreen> {
                EmergencyScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
        },
    )
}
