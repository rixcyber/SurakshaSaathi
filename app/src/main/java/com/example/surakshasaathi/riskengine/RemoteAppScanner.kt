package com.example.surakshasaathi.riskengine

import android.content.Context
import android.content.pm.PackageManager

data class DangerousAppDefinition(
    val name: String,
    val packageName: String,
    val category: String,
    val description: String,
    val scamUse: String,
    val emoji: String,
)

data class DetectedDangerousApp(
    val definition: DangerousAppDefinition,
    val isInstalled: Boolean,
    val isSimulated: Boolean = false,
)

object RemoteAppScanner {

    val knownRemoteAccessApps = listOf(
        DangerousAppDefinition(
            name = "AnyDesk",
            packageName = "com.anydesk.anydeskandroid",
            category = "Remote Screen Control",
            description = "Allows an unknown caller to view your screen and tap buttons on your phone.",
            scamUse = "Used in fake bank customer care and electricity bill fraud to steal OTPs.",
            emoji = "🖥️",
        ),
        DangerousAppDefinition(
            name = "TeamViewer QuickSupport",
            packageName = "com.teamviewer.quicksupport.market",
            category = "Remote Screen Sharing",
            description = "Shares your entire phone screen with a remote computer.",
            scamUse = "Scammers ask seniors to install this to 'verify' accounts, then steal money.",
            emoji = "📱",
        ),
        DangerousAppDefinition(
            name = "TeamViewer Host",
            packageName = "com.teamviewer.host.market",
            category = "Unattended Remote Access",
            description = "Gives remote access to your device even when you are not actively using it.",
            scamUse = "Used by fraudsters to control devices without the owner realizing.",
            emoji = "🕹️",
        ),
        DangerousAppDefinition(
            name = "RustDesk",
            packageName = "com.rustdesk.rustdesk",
            category = "Remote Desktop",
            description = "Remote desktop client capable of screen mirroring and device input control.",
            scamUse = "Often recommended by callers claiming to fix phone or bank issues.",
            emoji = "💻",
        ),
        DangerousAppDefinition(
            name = "AirDroid Remote Support",
            packageName = "com.sand.airdroid",
            category = "Remote Device Management",
            description = "Enables file transfer, notification access, and screen mirroring remotely.",
            scamUse = "Fraudsters use this to silently intercept bank SMS alerts and OTPs.",
            emoji = "📡",
        ),
        DangerousAppDefinition(
            name = "Splashtop Remote",
            packageName = "com.splashtop.remote.pad.v2",
            category = "Screen Mirroring",
            description = "Streams phone display to remote viewers over the internet.",
            scamUse = "Used in imposter scams to monitor banking credentials as you type.",
            emoji = "🪟",
        ),
        DangerousAppDefinition(
            name = "Zoho Assist",
            packageName = "com.zoho.assist.client",
            category = "Remote Support",
            description = "Remote technician tool commonly misused by unauthorized third parties.",
            scamUse = "Imposters posing as official tech support trick users into installing it.",
            emoji = "🛠️",
        ),
    )

    /**
     * Scan device package manager for any installed remote access apps.
     */
    fun scanDevice(context: Context): List<DetectedDangerousApp> {
        val pm = context.packageManager
        return knownRemoteAccessApps.map { appDef ->
            val isInstalled = try {
                pm.getPackageInfo(appDef.packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
            DetectedDangerousApp(
                definition = appDef,
                isInstalled = isInstalled,
                isSimulated = false,
            )
        }
    }
}
