package com.seance.tv.ui.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.seance.tv.ui.theme.BackgroundDark
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.OnSurface
import com.seance.tv.ui.theme.SoraFontFamily

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.authState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App name
            Text(
                text = "Séance",
                fontFamily = LoraFontFamily,
                fontStyle = FontStyle.Italic,
                fontSize = 64.sp,
                color = OnSurface,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            when {
                state.isLoading && state.pinCode.isEmpty() -> {
                    PollingDots()
                }
                state.pinCode.isNotEmpty() -> {
                    Text(
                        text = state.pinCode,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 72.sp,
                        color = Color.White,
                        letterSpacing = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Rendez-vous sur plex.tv/link",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        color = OnSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    PollingDots()
                }
                state.error != null -> {
                    Text(
                        text = state.error ?: "",
                        fontFamily = SoraFontFamily,
                        fontSize = 18.sp,
                        color = Color.Red.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PollingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = index * 200,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .alpha(alpha)
                    .background(color = Color.White, shape = CircleShape)
            )
        }
    }
}
