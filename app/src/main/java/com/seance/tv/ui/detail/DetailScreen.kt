package com.seance.tv.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seance.tv.data.model.MediaItem
import com.seance.tv.ui.components.CardAspect
import com.seance.tv.ui.components.MediaCard
import com.seance.tv.ui.theme.AccentDefault
import com.seance.tv.ui.theme.BackgroundDark
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.OnSurface
import com.seance.tv.ui.theme.SoraFontFamily

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailUiState,
    onPlay: (MediaItem) -> Unit,
    onSeasonSelected: (MediaItem) -> Unit,
    onBack: () -> Unit
) {
    val item = state.item ?: return

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {

        // Blurred background art
        AsyncImage(
            model = null, // populated via URL builder
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 48.dp, vertical = 32.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Poster
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(270.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1C1C1C))
                ) {
                    AsyncImage(
                        model = null,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Info column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontFamily = LoraFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 36.sp,
                        color = Color.White,
                        lineHeight = 42.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.year?.let {
                            Text(it.toString(), fontFamily = SoraFontFamily, fontSize = 14.sp, color = OnSurface.copy(alpha = 0.7f))
                        }
                        item.contentRating?.let {
                            Text(it, fontFamily = SoraFontFamily, fontSize = 12.sp, color = AccentDefault)
                        }
                        item.rating?.let {
                            Text("★ %.1f".format(it), fontFamily = SoraFontFamily, fontSize = 14.sp, color = AccentDefault)
                        }
                    }

                    if (item.genres.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.genres.joinToString(" · ") { it.tag },
                            fontFamily = SoraFontFamily,
                            fontSize = 13.sp,
                            color = OnSurface.copy(alpha = 0.6f)
                        )
                    }

                    if (item.summary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = item.summary,
                            fontFamily = SoraFontFamily,
                            fontSize = 14.sp,
                            color = OnSurface.copy(alpha = 0.8f),
                            lineHeight = 22.sp,
                            maxLines = 5
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { onPlay(item) },
                            colors = ButtonDefaults.colors(
                                containerColor = AccentDefault,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                if (item.viewOffset != null && item.viewOffset > 0) "Reprendre" else "Lire",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(onClick = onBack) {
                            Text("Retour", fontFamily = SoraFontFamily)
                        }
                    }

                    if (item.directors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Réalisateur : " + item.directors.joinToString(", ") { it.tag },
                            fontFamily = SoraFontFamily,
                            fontSize = 13.sp,
                            color = OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // TV Show — seasons & episodes
            if (item.isShow && state.seasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Saisons",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.seasons) { season ->
                        val isSelected = season.ratingKey == state.selectedSeason?.ratingKey
                        if (isSelected) {
                            Button(
                                onClick = { onSeasonSelected(season) },
                                colors = ButtonDefaults.colors(
                                    containerColor = AccentDefault,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(
                                    season.title,
                                    fontFamily = SoraFontFamily,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            OutlinedButton(onClick = { onSeasonSelected(season) }) {
                                Text(season.title, fontFamily = SoraFontFamily, fontSize = 13.sp)
                            }
                        }
                    }
                }

                if (state.episodes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 48.dp)
                    ) {
                        items(state.episodes, key = { it.ratingKey }) { episode ->
                            Column(modifier = Modifier.width(200.dp)) {
                                MediaCard(
                                    item = episode,
                                    imageUrl = null,
                                    aspect = CardAspect.LANDSCAPE,
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(113.dp),
                                    onClick = { onPlay(episode) }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${episode.episodeIndex}. ${episode.title}",
                                    fontFamily = SoraFontFamily,
                                    fontSize = 12.sp,
                                    color = OnSurface.copy(alpha = 0.8f),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            // Cast
            if (item.cast.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Casting",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.cast.take(8).joinToString(", ") { it.tag },
                    fontFamily = SoraFontFamily,
                    fontSize = 13.sp,
                    color = OnSurface.copy(alpha = 0.65f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
