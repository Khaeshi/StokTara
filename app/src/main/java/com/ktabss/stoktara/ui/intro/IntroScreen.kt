package com.ktabss.stoktara.ui.intro

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktabss.stoktara.data.AppGraph
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.data.Profile
import com.ktabss.stoktara.ui.components.IconSquareButton
import com.ktabss.stoktara.ui.components.softCard
import com.ktabss.stoktara.ui.theme.Ink
import com.ktabss.stoktara.ui.theme.Paper
import com.ktabss.stoktara.ui.theme.Placeholder
import com.ktabss.stoktara.ui.theme.Primary
import kotlinx.coroutines.flow.StateFlow

class IntroViewModel(private val repo: InventoryRepository = AppGraph.repository) : ViewModel() {
    val currentProfile: StateFlow<Profile> = repo.currentProfile
    val profiles: StateFlow<List<Profile>> = repo.profiles

    fun switchTo(profileId: Long) = repo.switchProfile(profileId)

    fun completeOnboarding(businessName: String, ownerName: String, pin: String) {
        repo.setBusinessName(businessName)
        repo.createOwnerProfile(ownerName, pin)
    }
}

// The Figma uses a proportional grotesque here (looks like Space Grotesk); system sans is the stand-in.
private val IntroFont = FontFamily.SansSerif

/**
 * Minimal Intro / "who's using the phone" screen. Member 1 extends it:
 * first-run onboarding branch, password check on Continue / Switch Profiles.
 */
@Composable
fun IntroScreen(onContinue: () -> Unit, viewModel: IntroViewModel = viewModel()) {
    val profile by viewModel.currentProfile.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    var showSwitcher by rememberSaveable { mutableStateOf(false) }

    // PIN Authentication Dialog States
    var pendingProfile by rememberSaveable { mutableStateOf<Profile?>(null) }
    var enteredPin by rememberSaveable { mutableStateOf("") }
    var pinError by rememberSaveable { mutableStateOf(false) }

    // First-run onboarding check: If no profiles exist, show the setup screen.
    if (profiles.isEmpty()) {
        BusinessSetupScreen(
            onSetupComplete = { businessName, ownerName, pin ->
                viewModel.completeOnboarding(businessName, ownerName, pin)
                onContinue()
            }
        )
        return
    }

    LightSystemBarIcons() // green background needs white status/nav icons

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(28.dp)).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Inventory2, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(104.dp))
            }
        }

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("StokTara", fontFamily = IntroFont, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Color.White)
            Text(
                "Simple stock counting for small shops. Track what’s in, what’s out, and who touched it.",
                fontFamily = IntroFont, fontSize = 14.sp, lineHeight = 20.sp, color = Color.White.copy(alpha = 0.85f),
            )
        }
        Spacer(Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Placeholder)
                .clickable {
                    pendingProfile = profile
                    enteredPin = ""
                    pinError = false
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Continue as ${profile.nickname} (${profile.role.label})",
                fontFamily = IntroFont, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Primary,
            )
        }
        Text(
            "Switch Profiles",
            fontFamily = IntroFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally).clickable { showSwitcher = true }.padding(vertical = 16.dp, horizontal = 24.dp),
        )
        Spacer(Modifier.height(16.dp))
    }

    // Switch Profiles Modal
    if (showSwitcher) {
        SwitchProfilesDialog(
            profiles = profiles,
            currentId = profile.id,
            onPick = { selected ->
                showSwitcher = false
                pendingProfile = selected
                enteredPin = ""
                pinError = false
            },
            onDismiss = { showSwitcher = false },
        )
    }

    // PIN Authentication Dialog
    pendingProfile?.let { targetProfile ->
        AlertDialog(
            onDismissRequest = {
                pendingProfile = null
                enteredPin = ""
                pinError = false
            },
            title = { Text("Enter PIN for ${targetProfile.nickname}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 4) enteredPin = it
                            pinError = false
                        },
                        label = { Text("PIN / Password") },
                        isError = pinError,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true
                    )
                    if (pinError) {
                        Text(
                            text = "Incorrect PIN. Try again.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Validate entered PIN against target profile PIN
                        if (enteredPin == targetProfile.password) {
                            viewModel.switchTo(targetProfile.id)
                            pendingProfile = null
                            onContinue()
                        } else {
                            pinError = true
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingProfile = null
                        enteredPin = ""
                        pinError = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SwitchProfilesDialog(
    profiles: List<Profile>,
    currentId: Long,
    onPick: (Profile) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().softCard(fill = Paper).padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Switch Account", style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                IconSquareButton(Icons.Outlined.Close, "Close", onDismiss)
            }
            Spacer(Modifier.height(8.dp))
            profiles.forEach { p ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onPick(p) }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(Primary), contentAlignment = Alignment.Center) {
                        Text(p.nickname.first().uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${p.nickname} (${p.role.label})",
                        color = Ink,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (p.id == currentId) FontWeight.Bold else FontWeight.Medium,
                        ),
                    )
                }
            }
        }
    }
}

/** White status/navigation bar icons while this screen is showing; restored to dark on leave. */
@Composable
private fun LightSystemBarIcons() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = true
            controller?.isAppearanceLightNavigationBars = true
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}