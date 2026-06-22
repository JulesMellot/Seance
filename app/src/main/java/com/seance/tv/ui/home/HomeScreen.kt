package com.seance.tv.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.Icon
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.seance.tv.data.model.HomeRow
import com.seance.tv.data.model.MediaItem
import com.seance.tv.ui.components.CardAspect
import com.seance.tv.ui.components.HeroSection
import com.seance.tv.ui.components.HomeSkeleton
import com.seance.tv.ui.components.RowSection
import com.seance.tv.ui.navigation.launchPlayer
import com.seance.tv.ui.theme.Accent
import com.seance.tv.ui.theme.BackgroundBase
import com.seance.tv.ui.theme.Dimens
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.Radii
import com.seance.tv.ui.theme.SoraFontFamily
import com.seance.tv.ui.theme.Surface
import com.seance.tv.ui.theme.TextMuted
import com.seance.tv.ui.theme.TextPrimary
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import com.seance.tv.ui.theme.loadAccentColor
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val firstCardFocus = remember { FocusRequester() }
    var focusRequested by remember { mutableStateOf(false) }
    var focusedRowIndex by remember { mutableStateOf(-1) }  // index LazyColumn de la rangée active

    // La rangée focalisée s'ancre en haut de la zone (son titre reste visible), les
    // précédentes glissent au-dessus. On attend qu'elle soit mesurée avant d'ancrer.
    LaunchedEffect(focusedRowIndex) {
        if (focusedRowIndex < 0) return@LaunchedEffect
        // Marge haute : le titre de la rangée se pose nettement sous le fondu du haut.
        val topMargin = with(density) { 12.dp.roundToPx() }
        repeat(12) {
            val info = listState.layoutInfo
            val target = info.visibleItemsInfo.firstOrNull { it.index == focusedRowIndex }
            if (target != null && target.size > 0) {
                val targetTop = info.viewportStartOffset + topMargin
                val delta = (target.offset - targetTop).toFloat()
                if (kotlin.math.abs(delta) > 4f) listState.animateScrollBy(delta)
                return@LaunchedEffect
            }
            delay(16)
        }
    }

    // Accent dynamique : couleur extraite du poster focalisé (ou du hero au repos).
    var dynamicAccent by remember { mutableStateOf(Accent) }
    val activeItem = uiState.focusedItem ?: uiState.heroItem
    LaunchedEffect(activeItem?.ratingKey) {
        delay(160)
        val url = viewModel.imageUrl(activeItem?.thumb ?: activeItem?.art)
        dynamicAccent = loadAccentColor(context, url) ?: Accent
    }
    val accent by animateColorAsState(dynamicAccent, tween(450), label = "accent")

    // Focus initial sur la 1ère carte (le hero n'a plus de bouton : on interagit via les cartes).
    LaunchedEffect(uiState.rows.isNotEmpty() && !uiState.isLoading) {
        if (uiState.rows.isNotEmpty() && !uiState.isLoading && !focusRequested) {
            delay(200)
            runCatching { firstCardFocus.requestFocus() }.onSuccess { focusRequested = true }
        }
    }

    // « Découvrir d'autres choses » : remonter en haut et redonner le focus à la 1ère carte.
    LaunchedEffect(uiState.refreshTick) {
        if (uiState.refreshTick > 0) {
            listState.animateScrollToItem(0)
            runCatching { firstCardFocus.requestFocus() }
        }
    }

    fun openDetail(item: MediaItem) = onOpenDetail(item.ratingKey)

    fun play(item: MediaItem) {
        val partKey = item.media.firstOrNull()?.parts?.firstOrNull()?.key
        launchPlayer(context, item, partKey?.let { viewModel.streamUrl(it) })
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBase)) {
        when {
            uiState.isLoading && uiState.rows.isEmpty() -> {
                HomeSkeleton(modifier = Modifier.fillMaxSize())
            }
            uiState.error != null && uiState.rows.isEmpty() -> ErrorState(uiState.error!!)
            else -> {
                // Hero ÉPINGLÉ (toujours visible) qui reflète l'item focalisé,
                // les lignes scrollent dans la zone du bas (fond sombre).
                val displayedHero = uiState.focusedItem ?: uiState.heroItem
                Column(modifier = Modifier.fillMaxSize()) {
                    if (displayedHero != null) {
                        HeroSection(
                            item = displayedHero,
                            artUrl = viewModel.imageUrl(displayedHero.art ?: displayedHero.thumb),
                            accent = accent,
                            showActions = false,   // hero purement informatif : on interagit via les cartes
                            buildImageUrl = { viewModel.imageUrl(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.60f)
                        )
                    }

                    // Zone des lignes (scroll) + fondu en haut
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(uiState.rows, key = { _, row ->
                                when (row) {
                                    is HomeRow.OnDeck -> "on_deck"
                                    is HomeRow.Collection -> "col_${row.collectionKey}"
                                    is HomeRow.RecentlyAdded -> "recently_added"
                                    is HomeRow.Genre -> "genre_${row.title}"
                                }
                            }) { index, row ->
                                val active = focusedRowIndex == index
                                val onFocus: (MediaItem) -> Unit = { item ->
                                    viewModel.onItemFocused(item)
                                    focusedRowIndex = index
                                }
                                Spacer(Modifier.height(Dimens.rowGap))
                                when (row) {
                                    is HomeRow.OnDeck -> RowSection(
                                        title = "Continuer à regarder",
                                        items = row.items,
                                        aspect = CardAspect.LANDSCAPE,
                                        accentColor = accent,
                                        active = active,
                                        firstItemFocusRequester = if (index == 0) firstCardFocus else null,
                                        buildImageUrl = { viewModel.imageUrl(it.thumb) },
                                        onItemClick = ::openDetail,
                                        onItemFocus = onFocus
                                    )
                                    is HomeRow.Collection -> RowSection(
                                        title = row.title,
                                        items = row.items,
                                        aspect = CardAspect.PORTRAIT,
                                        accentColor = accent,
                                        active = active,
                                        firstItemFocusRequester = if (index == 0) firstCardFocus else null,
                                        buildImageUrl = { viewModel.imageUrl(it.thumb) },
                                        onItemClick = ::openDetail,
                                        onItemFocus = onFocus
                                    )
                                    is HomeRow.RecentlyAdded -> RowSection(
                                        title = "Nouveautés",
                                        items = row.items.take(10),
                                        aspect = CardAspect.PORTRAIT,
                                        numbered = true,
                                        accentColor = accent,
                                        active = active,
                                        firstItemFocusRequester = if (index == 0) firstCardFocus else null,
                                        buildImageUrl = { viewModel.imageUrl(it.thumb) },
                                        onItemClick = ::openDetail,
                                        onItemFocus = onFocus
                                    )
                                    is HomeRow.Genre -> RowSection(
                                        title = row.title,
                                        items = row.items,
                                        aspect = CardAspect.PORTRAIT,
                                        accentColor = accent,
                                        active = active,
                                        firstItemFocusRequester = if (index == 0) firstCardFocus else null,
                                        buildImageUrl = { viewModel.imageUrl(it.thumb) },
                                        onItemClick = ::openDetail,
                                        onItemFocus = onFocus
                                    )
                                }
                            }

                            item {
                                Spacer(Modifier.height(Dimens.rowGap + 8.dp))
                                DiscoverMoreButton(
                                    accent = accent,
                                    onClick = { viewModel.discoverMore() }
                                )
                                Spacer(Modifier.height(56.dp))
                            }
                        }

                        // Fondu en haut : les lignes fondent en remontant sous le hero.
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .fillMaxWidth()
                                .height(28.dp)
                                .background(
                                    Brush.verticalGradient(
                                        0f to BackgroundBase,
                                        1f to Color.Transparent
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverMoreButton(accent: Color, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .padding(start = Dimens.safeH)
            .height(52.dp)
            .clip(RoundedCornerShape(Radii.pill))
            .background(if (focused) accent else Surface)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = null,
            tint = if (focused) Color.Black else accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Découvrir d'autres choses",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = if (focused) Color.Black else TextPrimary
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Quelque chose s'est mal passé",
                fontFamily = LoraFontFamily,
                fontStyle = FontStyle.Italic,
                fontSize = 28.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = message,
                fontFamily = SoraFontFamily,
                fontSize = 14.sp,
                color = TextMuted
            )
        }
    }
}
