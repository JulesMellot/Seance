package com.seance.tv.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seance.tv.data.model.HomeUser
import com.seance.tv.ui.theme.Accent
import com.seance.tv.ui.theme.AccentSoft
import com.seance.tv.ui.theme.BackgroundBase
import com.seance.tv.ui.theme.BackgroundDeep
import com.seance.tv.ui.theme.BorderSubtle
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.Radii
import com.seance.tv.ui.theme.SoraFontFamily
import com.seance.tv.ui.theme.Surface
import com.seance.tv.ui.theme.SurfaceFocused
import com.seance.tv.ui.theme.TextMuted
import com.seance.tv.ui.theme.TextPrimary
import com.seance.tv.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    profiles: List<HomeUser>,
    isLoading: Boolean,
    error: String?,
    onSelect: (HomeUser, String?) -> Unit
) {
    var pinFor by remember { mutableStateOf<HomeUser?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Surface, BackgroundBase, BackgroundDeep),
                    radius = 1600f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val target = pinFor
        if (target == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Qui regarde ?",
                    fontFamily = LoraFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    fontSize = 46.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.height(48.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    profiles.forEachIndexed { index, user ->
                        ProfileTile(
                            user = user,
                            autoFocus = index == 0,
                            onClick = {
                                if (user.requiresPin) pinFor = user else onSelect(user, null)
                            }
                        )
                    }
                }
                if (error != null) {
                    Spacer(Modifier.height(36.dp))
                    Text(
                        text = error,
                        fontFamily = SoraFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFE8736A),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            PinEntry(
                user = target,
                error = error,
                onSubmit = { pin -> onSelect(target, pin) },
                onCancel = { pinFor = null }
            )
        }
    }
}

@Composable
private fun ProfileTile(
    user: HomeUser,
    autoFocus: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (autoFocus) runCatching { focusRequester.requestFocus() }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (focused) 132.dp else 120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceFocused)
                .border(
                    width = if (focused) 3.dp else 1.dp,
                    color = if (focused) Accent else BorderSubtle,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!user.thumb.isNullOrBlank()) {
                AsyncImage(
                    model = user.thumb,
                    contentDescription = user.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
                )
            } else {
                Text(
                    text = user.title.take(1).uppercase(),
                    fontFamily = LoraFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 48.sp,
                    color = Accent
                )
            }
            if (user.requiresPin) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(BackgroundDeep.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Protégé",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = user.title,
            fontFamily = SoraFontFamily,
            fontWeight = if (focused) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 16.sp,
            color = if (focused) TextPrimary else TextSecondary
        )
    }
}

@Composable
private fun PinEntry(
    user: HomeUser,
    error: String?,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }

    fun append(d: Char) {
        if (pin.length < 4) {
            pin += d
            if (pin.length == 4) onSubmit(pin)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Code de ${user.title}",
            fontFamily = LoraFontFamily,
            fontStyle = FontStyle.Italic,
            fontSize = 30.sp,
            color = TextPrimary
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 60.dp)
                        .clip(RoundedCornerShape(Radii.chip))
                        .background(AccentSoft)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(Radii.chip)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (i < pin.length) "•" else "",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = TextPrimary
                    )
                }
            }
        }
        if (error != null) {
            Spacer(Modifier.height(16.dp))
            Text(error, fontFamily = SoraFontFamily, fontSize = 14.sp, color = Color(0xFFE8736A))
        }
        Spacer(Modifier.height(28.dp))
        // Pavé numérique 3x4
        val rows = listOf(listOf('1', '2', '3'), listOf('4', '5', '6'), listOf('7', '8', '9'))
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEachIndexed { r, keys ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    keys.forEach { k ->
                        PinKey(label = k.toString(), autoFocus = r == 0 && k == '1', onClick = { append(k) })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PinKey(label = "annuler", wide = true, onClick = onCancel)
                PinKey(label = "0", onClick = { append('0') })
                PinKey(
                    label = "del",
                    icon = true,
                    onClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) }
                )
            }
        }
    }
}

@Composable
private fun PinKey(
    label: String,
    onClick: () -> Unit,
    wide: Boolean = false,
    icon: Boolean = false,
    autoFocus: Boolean = false
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (autoFocus) runCatching { focusRequester.requestFocus() } }

    Box(
        modifier = Modifier
            .focusRequester(focusRequester)
            .size(width = if (wide) 124.dp else 56.dp, height = 56.dp)
            .clip(RoundedCornerShape(Radii.chip))
            .background(if (focused) Accent else SurfaceFocused)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (icon) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Effacer",
                tint = if (focused) Color.Black else TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = label,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (label.length > 1) 14.sp else 22.sp,
                color = if (focused) Color.Black else TextPrimary
            )
        }
    }
}
