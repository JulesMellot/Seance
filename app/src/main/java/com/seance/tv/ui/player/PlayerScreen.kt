package com.seance.tv.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.Text
import com.seance.tv.ui.theme.AccentDefault
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    state: PlayerUiState,
    videoPlayer: VideoPlayer,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onProgress: (Long, Long) -> Unit,
    onBack: () -> Unit
) {
    var controlsVisible by remember { mutableStateOf(true) }

    // Position en direct depuis le lecteur
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            onProgress(videoPlayer.getPositionMs(), videoPlayer.getDurationMs())
        }
    }

    LaunchedEffect(state.showControls, state.isPlaying) {
        if (state.isPlaying) {
            delay(4_000)
            controlsVisible = false
        }
    }

    LaunchedEffect(state.streamUrl) {
        if (state.streamUrl.isNotBlank()) {
            videoPlayer.play(state.streamUrl, state.positionMs)
        }
    }

    // Synchronise pause/lecture déclenché par la télécommande
    LaunchedEffect(state.isPlaying) {
        videoPlayer.setPlaying(state.isPlaying)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { videoPlayer.playerView },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            ) {
                // Title at top
                Text(
                    text = state.title,
                    fontFamily = LoraFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 22.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                )

                // Controls at bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 24.dp)
                ) {
                    // Progress bar
                    if (state.durationMs > 0) {
                        val fraction = (state.positionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(4.dp)
                                    .background(AccentDefault)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(state.positionMs),
                                fontFamily = SoraFontFamily,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = formatTime(state.durationMs),
                                fontFamily = SoraFontFamily,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // -10s
                        ControlButton(label = "−10s", onClick = {
                            videoPlayer.seekRelative(-10_000)
                            onSeek(-10_000)
                        })
                        Spacer(modifier = Modifier.width(16.dp))
                        // Play/Pause
                        ControlButton(
                            label = if (state.isPlaying) "⏸" else "▶",
                            onClick = onPlayPause,
                            large = true
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        // +10s
                        ControlButton(label = "+10s", onClick = {
                            videoPlayer.seekRelative(10_000)
                            onSeek(10_000)
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ControlButton(label: String, onClick: () -> Unit, large: Boolean = false) {
    val size = if (large) 56.dp else 44.dp
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
        colors = IconButtonDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.15f),
            focusedContainerColor = AccentDefault
        )
    ) {
        Text(
            text = label,
            fontFamily = SoraFontFamily,
            fontSize = if (large) 18.sp else 12.sp,
            color = Color.White
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
