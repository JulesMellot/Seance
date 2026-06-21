package com.seance.tv.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.app.Activity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.seance.tv.ui.theme.Accent
import com.seance.tv.ui.theme.BackgroundBase
import com.seance.tv.ui.theme.BorderSubtle
import com.seance.tv.ui.theme.Dimens
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.Radii
import com.seance.tv.ui.theme.SoraFontFamily
import com.seance.tv.ui.theme.Surface
import com.seance.tv.ui.theme.SurfaceFocused
import com.seance.tv.ui.theme.TextMuted
import com.seance.tv.ui.theme.TextPrimary
import com.seance.tv.ui.theme.TextSecondary

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBase)
            .verticalScroll(rememberScrollState())
            .padding(start = Dimens.safeH, top = Dimens.safeTop, end = 64.dp, bottom = 48.dp)
    ) {
        Text(
            text = "Paramètres",
            fontFamily = LoraFontFamily,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            fontSize = 38.sp,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Bibliothèques affichées sur l'accueil et le parcours",
            fontFamily = SoraFontFamily,
            fontSize = 14.sp,
            color = TextMuted
        )
        Spacer(Modifier.height(24.dp))

        when {
            state.isLoading -> Text("Chargement…", fontFamily = SoraFontFamily, color = TextMuted)
            state.error != null -> Text(state.error!!, fontFamily = SoraFontFamily, color = TextMuted)
            else -> Column(modifier = Modifier.fillMaxWidth(0.55f)) {
                state.libraries.forEachIndexed { index, lib ->
                    LibraryRow(
                        title = lib.section.title,
                        isShow = lib.section.type == "show",
                        enabled = lib.enabled,
                        onToggle = { viewModel.toggle(lib.section.key) }
                    )
                    if (index < state.libraries.lastIndex) RowDivider()
                }
            }
        }

        // ── Serveur (liste des serveurs Plex du compte) ──────────────────
        if (state.servers.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))
            SectionLabel("Serveur")
            Spacer(Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth(0.55f)) {
                state.servers.forEachIndexed { index, server ->
                    ServerRow(
                        name = server.name,
                        selected = server.isCurrent,
                        onClick = {
                            viewModel.selectServer(server.url) { (context as? Activity)?.recreate() }
                        }
                    )
                    if (index < state.servers.lastIndex) RowDivider()
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        SectionLabel("Compte")
        Spacer(Modifier.height(12.dp))
        if (state.multipleProfiles) {
            AccountAction(
                icon = Icons.Filled.SwitchAccount,
                label = "Changer de profil",
                modifier = Modifier.fillMaxWidth(0.6f),
                onClick = { (context as? Activity)?.recreate() }
            )
            Spacer(Modifier.height(10.dp))
        }
        LogoutButton(
            modifier = Modifier.fillMaxWidth(0.6f),
            onClick = { viewModel.logout { (context as? Activity)?.recreate() } }
        )
    }
}

@Composable
private fun AccountAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(Radii.card))
            .background(if (focused) SurfaceFocused else Surface)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (focused) TextPrimary else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = TextPrimary
        )
    }
}

@Composable
private fun LogoutButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(Radii.card))
            .background(if (focused) Color(0xFFE8736A) else Surface)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            tint = if (focused) Color.Black else Color(0xFFE8736A),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = "Se déconnecter",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = if (focused) Color.Black else TextPrimary
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 1.5.sp,
        color = TextMuted
    )
}

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(BorderSubtle)
    )
}

@Composable
private fun ServerRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(Radii.chip))
            .background(if (focused) SurfaceFocused else Color.Transparent)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Dns,
            contentDescription = null,
            tint = if (selected) Accent else if (focused) TextPrimary else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = name,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = if (focused) TextPrimary else TextPrimary.copy(alpha = 0.92f),
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (selected) Accent else Color.White.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Serveur actuel",
                    tint = Color.Black,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LibraryRow(
    title: String,
    isShow: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(Radii.chip))
            .background(if (focused) SurfaceFocused else Color.Transparent)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onToggle)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isShow) Icons.Filled.Tv else Icons.Filled.Movie,
            contentDescription = null,
            tint = if (focused) TextPrimary else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = if (focused) TextPrimary else TextPrimary.copy(alpha = 0.92f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (isShow) "Séries" else "Films",
            fontFamily = SoraFontFamily,
            fontSize = 12.sp,
            color = TextMuted
        )
        Spacer(Modifier.width(16.dp))
        // Interrupteur (coche dans une pastille)
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (enabled) Accent else Color.White.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            if (enabled) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Activée",
                    tint = Color.Black,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
