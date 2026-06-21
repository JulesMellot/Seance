package com.seance.tv.ui.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.seance.tv.ui.theme.Accent
import com.seance.tv.ui.theme.AccentSoft
import com.seance.tv.ui.theme.BackgroundBase
import com.seance.tv.ui.theme.BackgroundDeep
import com.seance.tv.ui.theme.BorderSubtle
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.Radii
import com.seance.tv.ui.theme.SoraFontFamily
import com.seance.tv.ui.theme.Surface
import com.seance.tv.ui.theme.TextMuted
import com.seance.tv.ui.theme.TextPrimary
import com.seance.tv.ui.theme.TextSecondary

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.authState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Surface, BackgroundBase, BackgroundDeep),
                    radius = 1400f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Séance",
                fontFamily = LoraFontFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                fontSize = 68.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "VOTRE CINÉMA, CHEZ VOUS",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 4.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(56.dp))

            when {
                state.isLoading && state.pinCode.isEmpty() -> PollingDots()

                state.pinCode.isNotEmpty() -> {
                    PinCode(state.pinCode)
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rendez-vous sur ",
                            fontFamily = SoraFontFamily,
                            fontSize = 17.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "plex.tv/link",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = Accent
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    PollingDots()
                }

                state.error != null -> {
                    Text(
                        text = state.error ?: "",
                        fontFamily = SoraFontFamily,
                        fontSize = 16.sp,
                        color = Color(0xFFE8736A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PinCode(code: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        code.forEach { ch ->
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 84.dp)
                    .clip(RoundedCornerShape(Radii.chip))
                    .background(AccentSoft)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(Radii.chip)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ch.toString(),
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun PollingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .alpha(alpha)
                    .background(color = Accent, shape = CircleShape)
            )
        }
    }
}
