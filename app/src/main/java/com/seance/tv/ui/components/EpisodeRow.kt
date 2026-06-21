package com.seance.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seance.tv.data.model.MediaItem
import com.seance.tv.ui.theme.Accent
import com.seance.tv.ui.theme.AccentBright
import com.seance.tv.ui.theme.Dimens
import com.seance.tv.ui.theme.Motion
import com.seance.tv.ui.theme.Radii
import com.seance.tv.ui.theme.SoraFontFamily
import com.seance.tv.ui.theme.Surface
import com.seance.tv.ui.theme.SurfaceFocused
import com.seance.tv.ui.theme.TextMuted
import com.seance.tv.ui.theme.TextPrimary
import com.seance.tv.ui.theme.TextSecondary

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EpisodeRow(
    episodes: List<MediaItem>,
    buildImageUrl: (String?) -> String?,
    onPlay: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = Dimens.safeH),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        items(episodes, key = { it.ratingKey }) { episode ->
            EpisodeCard(episode, buildImageUrl(episode.thumb), onClick = { onPlay(episode) })
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun EpisodeCard(
    episode: MediaItem,
    imageUrl: String?,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val p by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = tween(Motion.fast, easing = Motion.expoOut),
        label = "epFocus"
    )

    Column(modifier = Modifier.width(300.dp)) {
        Card(
            onClick = onClick,
            onLongClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .onFocusChanged { focused = it.isFocused }
                .graphicsLayer { val s = lerp(1f, 1.05f, p); scaleX = s; scaleY = s }
                .shadow(
                    elevation = lerp(0f, 20f, p).dp,
                    shape = RoundedCornerShape(Radii.card),
                    ambientColor = Accent,
                    spotColor = Accent
                ),
            colors = CardDefaults.colors(containerColor = Surface, focusedContainerColor = SurfaceFocused),
            border = CardDefaults.border(
                border = Border.None,
                focusedBorder = Border(BorderStroke(2.5.dp, AccentBright))
            ),
            scale = CardDefaults.scale(focusedScale = 1f),
            shape = CardDefaults.shape(shape = RoundedCornerShape(Radii.card))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = episode.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(SurfaceFocused))
                }
                if (episode.progressFraction > 0.01f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(episode.progressFraction)
                                .height(3.dp)
                                .clip(RoundedCornerShape(Radii.pill))
                                .background(Accent)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = buildString {
                episode.episodeIndex?.let { append("E$it · ") }
                append(episode.title)
            },
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        episode.runtimeLabel?.let {
            Spacer(Modifier.height(2.dp))
            Text(it, fontFamily = SoraFontFamily, fontSize = 11.sp, color = TextMuted)
        }
        if (episode.summary.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = episode.summary,
                fontFamily = SoraFontFamily,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
