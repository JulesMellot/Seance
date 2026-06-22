package com.seance.tv.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seance.tv.data.model.MediaItem
import com.seance.tv.data.model.PlexStream
import com.seance.tv.ui.components.CardAspect
import com.seance.tv.ui.components.CastRow
import com.seance.tv.ui.components.EpisodeRow
import com.seance.tv.ui.components.MediaCard
import com.seance.tv.ui.theme.Accent
import com.seance.tv.ui.theme.BackgroundBase
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
fun DetailScreen(
    state: DetailUiState,
    onPlay: (item: MediaItem, audioLanguage: String?, subtitleLanguage: String?, subtitlesDisabled: Boolean) -> Unit,
    onSeasonSelected: (MediaItem) -> Unit,
    onBack: () -> Unit,
    onItemClick: (MediaItem) -> Unit = {},
    imageUrl: (String?) -> String? = { null }
) {
    val item = state.item ?: run {
        Box(Modifier.fillMaxSize().background(BackgroundBase), contentAlignment = Alignment.Center) {
            Text(
                text = state.error ?: "Chargement…",
                fontFamily = SoraFontFamily,
                color = TextMuted
            )
        }
        return
    }

    // Cible de lecture : l'élément lui-même (film) ou le 1er épisode à reprendre/regarder (série)
    val playTarget: MediaItem? = if (item.isShow) {
        state.episodes.firstOrNull { (it.viewOffset ?: 0) > 0 } ?: state.episodes.firstOrNull()
    } else item

    val playFocus = remember { FocusRequester() }
    LaunchedEffect(item.ratingKey) {
        runCatching { playFocus.requestFocus() }
    }

    // ── Sélection des pistes (audio / sous-titres) ───────────────────────
    val audioTracks = playTarget?.audioStreams ?: emptyList()
    val subtitleTracks = playTarget?.subtitleStreams ?: emptyList()
    // Initialisées depuis les flags "selected" renvoyés par Plex.
    var selectedAudioId by remember(playTarget?.ratingKey) {
        mutableStateOf(audioTracks.firstOrNull { it.selected }?.id ?: audioTracks.firstOrNull()?.id)
    }
    // null = sous-titres désactivés.
    var selectedSubId by remember(playTarget?.ratingKey) {
        mutableStateOf(subtitleTracks.firstOrNull { it.selected }?.id)
    }
    var showTracks by remember { mutableStateOf(false) }
    BackHandler(enabled = showTracks) { showTracks = false }

    fun startPlayback() {
        val target = playTarget ?: return
        val audioLang = audioTracks.firstOrNull { it.id == selectedAudioId }?.playerLanguage
        val sub = subtitleTracks.firstOrNull { it.id == selectedSubId }
        onPlay(target, audioLang, sub?.playerLanguage, sub == null)
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBase)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // ── Panneau héro : backdrop + infos principales ──────────────
            Box(modifier = Modifier.fillMaxWidth().height(580.dp)) {
                AsyncImage(
                    model = imageUrl(item.art ?: item.thumb),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Scrim gauche
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.8f)
                        .align(Alignment.CenterStart)
                        .background(
                            Brush.horizontalGradient(
                                0f to BackgroundBase.copy(alpha = 0.97f),
                                0.6f to BackgroundBase.copy(alpha = 0.5f),
                                1f to Color.Transparent
                            )
                        )
                )
                // Scrim bas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.6f to Color.Transparent,
                                1f to BackgroundBase
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.66f)
                        .padding(start = Dimens.safeH, bottom = 40.dp)
                ) {
                    item.showTitle?.takeIf { item.isEpisode }?.let {
                        Text(it.uppercase(), fontFamily = SoraFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 2.sp, color = Accent)
                        Spacer(Modifier.height(8.dp))
                    }
                    val logo = item.clearLogo?.let { imageUrl(it) }
                    if (logo != null) {
                        AsyncImage(
                            model = logo,
                            contentDescription = item.title,
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart,
                            modifier = Modifier
                                .heightIn(max = 132.dp)
                                .widthIn(max = 520.dp)
                        )
                    } else {
                        Text(
                            text = item.title,
                            fontFamily = LoraFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            fontSize = 46.sp,
                            lineHeight = 52.sp,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    item.tagline?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            fontFamily = LoraFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 18.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    MetaRow(item)

                    if (item.summary.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = item.summary,
                            fontFamily = SoraFontFamily,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            color = Color.White.copy(alpha = 0.78f),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        val resume = (playTarget?.viewOffset ?: 0) > 0
                        Button(
                            onClick = { startPlayback() },
                            modifier = Modifier.focusRequester(playFocus),
                            colors = ButtonDefaults.colors(
                                containerColor = Accent,
                                contentColor = Color.Black,
                                focusedContainerColor = Color.White,
                                focusedContentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = if (resume) "▶  Reprendre" else "▶  Lire",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                        if (playTarget?.hasTrackChoices == true) {
                            Button(
                                onClick = { showTracks = true },
                                colors = ButtonDefaults.colors(
                                    containerColor = Color.White.copy(alpha = 0.14f),
                                    contentColor = Color.White,
                                    focusedContainerColor = Color.White,
                                    focusedContentColor = Color.Black
                                )
                            ) {
                                Text(
                                    text = "Pistes & sous-titres",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    // Réalisateur / studio
                    val credits = buildList {
                        item.directors.firstOrNull()?.let { add("Réalisé par ${item.directors.joinToString(", ") { d -> d.tag }}") }
                        item.studio?.takeIf { it.isNotBlank() }?.let { add(it) }
                    }
                    if (credits.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = credits.joinToString("   ·   "),
                            fontFamily = SoraFontFamily,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // ── Specs techniques ─────────────────────────────────────────
            val specs = listOfNotNull(item.resolutionLabel, item.videoCodecLabel, item.audioCodecLabel)
            if (specs.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(start = Dimens.safeH, top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    specs.forEach { SpecPill(it) }
                }
            }

            // ── Séries : saisons + épisodes ──────────────────────────────
            if (item.isShow && state.seasons.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                SectionTitle("Épisodes")
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Dimens.safeH),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.seasons, key = { it.ratingKey }) { season ->
                        val selected = season.ratingKey == state.selectedSeason?.ratingKey
                        SeasonChip(season.title, selected) { onSeasonSelected(season) }
                    }
                }
                if (state.episodes.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    EpisodeRow(
                        episodes = state.episodes,
                        buildImageUrl = imageUrl,
                        onPlay = { ep -> onPlay(ep, null, null, false) }
                    )
                }
            }

            // ── Casting ──────────────────────────────────────────────────
            if (item.cast.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                SectionTitle("Casting")
                Spacer(Modifier.height(16.dp))
                CastRow(cast = item.cast, buildImageUrl = imageUrl)
            }

            // ── Plus comme ça ────────────────────────────────────────────
            if (state.similar.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                SectionTitle("Plus comme ça")
                Spacer(Modifier.height(14.dp))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Dimens.safeH),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.cardGap)
                ) {
                    items(state.similar, key = { it.ratingKey }) { sim ->
                        MediaCard(
                            item = sim,
                            imageUrl = imageUrl(sim.thumb),
                            aspect = CardAspect.PORTRAIT,
                            showCaption = true,
                            modifier = Modifier.width(Dimens.cardPortraitW),
                            onClick = { onItemClick(sim) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(56.dp))
        }

        if (showTracks) {
            TracksPanel(
                audio = audioTracks,
                subtitles = subtitleTracks,
                selectedAudioId = selectedAudioId,
                selectedSubId = selectedSubId,
                onSelectAudio = { selectedAudioId = it },
                onSelectSub = { selectedSubId = it }
            )
        }
    }
}

@Composable
private fun MetaRow(item: MediaItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        item.rating?.let {
            Text("★ %.1f".format(it), fontFamily = SoraFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Accent)
            MetaDot()
        }
        item.year?.let {
            Text(it.toString(), fontFamily = SoraFontFamily, fontSize = 15.sp, color = Color.White.copy(alpha = 0.85f))
        }
        (item.runtimeLabel?.takeIf { item.isMovie } ?: item.seasonsLabel)?.let {
            MetaDot(); Text(it, fontFamily = SoraFontFamily, fontSize = 15.sp, color = Color.White.copy(alpha = 0.85f))
        }
        item.contentRating?.let {
            MetaDot()
            Box(
                modifier = Modifier.clip(RoundedCornerShape(Radii.chip)).background(Color.White.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 2.dp)
            ) { Text(it, fontFamily = SoraFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f)) }
        }
        item.genres.takeIf { it.isNotEmpty() }?.let { genres ->
            MetaDot()
            Text(
                text = genres.take(3).joinToString(" · ") { it.tag },
                fontFamily = SoraFontFamily,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MetaDot() {
    Box(
        modifier = Modifier
            .padding(horizontal = 11.dp)
            .width(3.dp)
            .height(3.dp)
            .clip(RoundedCornerShape(Radii.pill))
            .background(Color.White.copy(alpha = 0.4f))
    )
}

@Composable
private fun SpecPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radii.chip))
            .background(Surface)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontFamily = SoraFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.5.sp, color = TextSecondary)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = Dimens.safeH)
    ) {
        Box(modifier = Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(Radii.pill)).background(Accent))
        Spacer(Modifier.width(12.dp))
        Text(title, fontFamily = SoraFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextPrimary.copy(alpha = 0.94f))
    }
}

@Composable
private fun TracksPanel(
    audio: List<PlexStream>,
    subtitles: List<PlexStream>,
    selectedAudioId: Long?,
    selectedSubId: Long?,
    onSelectAudio: (Long?) -> Unit,
    onSelectSub: (Long?) -> Unit
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstFocus.requestFocus() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.66f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 26.dp)
        ) {
            Text(
                text = "Pistes & sous-titres",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = TextPrimary
            )

            if (audio.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                PanelSectionLabel("Audio")
                Spacer(Modifier.height(6.dp))
                audio.forEachIndexed { i, s ->
                    TrackOption(
                        label = s.label,
                        selected = s.id == selectedAudioId,
                        focusRequester = firstFocus.takeIf { i == 0 },
                        onClick = { onSelectAudio(s.id) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            PanelSectionLabel("Sous-titres")
            Spacer(Modifier.height(6.dp))
            TrackOption(
                label = "Désactivés",
                selected = selectedSubId == null,
                focusRequester = firstFocus.takeIf { audio.isEmpty() },
                onClick = { onSelectSub(null) }
            )
            subtitles.forEach { s ->
                TrackOption(
                    label = s.label,
                    selected = s.id == selectedSubId,
                    onClick = { onSelectSub(s.id) }
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "Retour pour fermer",
                fontFamily = SoraFontFamily,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun PanelSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        color = Accent
    )
}

@Composable
private fun TrackOption(
    label: String,
    selected: Boolean,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.chip))
            .background(if (focused) Color.White.copy(alpha = 0.12f) else Color.Transparent)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(7.dp)
                .height(7.dp)
                .clip(RoundedCornerShape(Radii.pill))
                .background(if (selected) Accent else Color.White.copy(alpha = 0.18f))
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            fontFamily = SoraFontFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 15.sp,
            color = if (selected) TextPrimary else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeasonChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.colors(
            containerColor = if (selected) Accent else Surface,
            contentColor = if (selected) Color.Black else TextPrimary,
            focusedContainerColor = Color.White,
            focusedContentColor = Color.Black
        )
    ) {
        Text(label, fontFamily = SoraFontFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}
