package com.seance.tv.ui.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.seance.tv.data.model.GenreEntry
import com.seance.tv.ui.components.CardAspect
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
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BrowseScreen(
    sectionType: String,
    onOpenDetail: (String) -> Unit,
    viewModel: BrowseViewModel = hiltViewModel()
) {
    LaunchedEffect(sectionType) { viewModel.start(sectionType) }
    val state by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()

    // Pagination : charger plus quand on approche de la fin
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { last ->
                if (last >= state.items.size - Dimens.gridColumns * 2) viewModel.loadMore()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBase)
            .padding(start = Dimens.safeH, top = Dimens.safeTop, end = 32.dp)
    ) {
        // Titre
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = state.title,
                fontFamily = LoraFontFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                fontSize = 38.sp,
                color = TextPrimary
            )
            state.selectedGenre?.let {
                Spacer(Modifier.width(14.dp))
                Text(
                    text = "· ${it.title}",
                    fontFamily = SoraFontFamily,
                    fontSize = 18.sp,
                    color = Accent,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Chips de genre
        if (state.genres.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = Dimens.safeH)
            ) {
                item {
                    GenreChip(
                        label = "Tous",
                        selected = state.selectedGenre == null,
                        onClick = { viewModel.selectGenre(null) }
                    )
                }
                items(state.genres, key = { it.key }) { genre ->
                    GenreChip(
                        label = genre.title,
                        selected = state.selectedGenre?.key == genre.key,
                        onClick = { viewModel.selectGenre(genre) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Tri + filtre « non vus »
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = Dimens.safeH),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Text(
                    text = "Trier",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            items(SortOption.entries.toList(), key = { it.name }) { option ->
                GenreChip(
                    label = option.label,
                    selected = state.sort == option,
                    onClick = { viewModel.selectSort(option) }
                )
            }
            item {
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .width(1.dp)
                        .height(22.dp)
                        .background(Color.White.copy(alpha = 0.12f))
                )
            }
            item {
                GenreChip(
                    label = "Non vus",
                    selected = state.unwatchedOnly,
                    onClick = { viewModel.toggleUnwatched() }
                )
            }
        }
        Spacer(Modifier.height(18.dp))

        when {
            state.isLoading && state.items.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Chargement…", fontFamily = SoraFontFamily, color = TextMuted) }

            state.error != null && state.items.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text(state.error!!, fontFamily = SoraFontFamily, color = TextMuted) }

            else -> LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(Dimens.gridColumns),
                horizontalArrangement = Arrangement.spacedBy(Dimens.gridGap),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp)
            ) {
                gridItems(state.items, key = { it.ratingKey }) { item ->
                    MediaCard(
                        item = item,
                        imageUrl = viewModel.imageUrl(item.thumb),
                        aspect = CardAspect.PORTRAIT,
                        showCaption = true,
                        onClick = { onOpenDetail(item.ratingKey) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GenreChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val bg = when {
        selected -> Accent
        focused -> SurfaceFocused
        else -> Surface
    }
    val fg = if (selected) Color.Black else TextPrimary.copy(alpha = if (focused) 1f else 0.8f)

    Box(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(Radii.pill))
            .background(bg)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = SoraFontFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 13.sp,
            color = fg
        )
    }
}
