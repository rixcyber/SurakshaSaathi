package com.example.surakshasaathi.ui.trustedcontacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
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
import com.example.surakshasaathi.theme.*

data class TrustedContact(
    val name: String,
    val relation: String,
    val phone: String,
    val emoji: String,
)

// Demo trusted contacts
private val demoContacts = listOf(
    TrustedContact("Rahul", "Son", "+919999999901", "👨"),
    TrustedContact("Priya", "Daughter", "+919999999902", "👩"),
    TrustedContact("Ajay", "Relative", "+919999999903", "👤"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustedContactsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

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
                    "Ask My Family",
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
            // Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(FamilyColor, FamilyColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("👨‍👩‍👦", fontSize = 36.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Contact Your Family",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "When you are unsure about something,\ncall someone you trust.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // Trusted contacts
            demoContacts.forEach { contact ->
                TrustedContactCard(
                    contact = contact,
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${contact.phone}")
                        }
                        context.startActivity(intent)
                    },
                )
                Spacer(Modifier.height(14.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Helpful advice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SafeGreenLight,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Remember",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SafeGreen,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "• Never share your OTP with anyone.\n" +
                            "• Banks will never ask for your PIN on a call.\n" +
                            "• If unsure, always ask your family first.\n" +
                            "• It is okay to hang up and call back later.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SafeGreen.copy(alpha = 0.85f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrustedContactCard(
    contact: TrustedContact,
    onCall: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FamilyColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(contact.emoji, fontSize = 28.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = contact.relation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Call button
            FilledIconButton(
                onClick = onCall,
                modifier = Modifier.size(52.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = FamilyColor,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "Call ${contact.name}",
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}
