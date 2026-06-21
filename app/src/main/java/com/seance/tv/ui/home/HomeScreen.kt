package com.seance.tv.ui.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.seance.tv.data.model.HomeRow
import com.seance.tv.data.model.MediaItem
import com.seance.tv.ui.components.CardAspect
import com.seance.tv.ui.components.HeroSection
import com.seance.tv.ui.components.RowSection
import com.seance.tv.ui.detail.DetailActivity
import com.seance.tv.ui.player.PlayerActivity
import com.seance.tv.ui.theme.AccentDefault
import com.seance.tv.ui.theme.BackgroundDark
import com.seance.tv.ui.theme.SoraFontFamily
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Shuffle when user scrolls near the bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .distinctUntilChanged()
            .collect { info ->
                val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = info.totalItemsCount
                if (totalItems > 0 && lastVisible >= totalItems - 2) {
                    viewModel.shuffle()
                }
            }
    }

    val focusedItem = uiState.focusedItem
    val serverUrl = "" // Injected via ServerManager in production

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (uiState.isLoading && uiState.rows.isEmpty()) {
            Text(
                text = "Chargement…",
                fontFamily = SoraFontFamily,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (uiState.error != null && uiState.rows.isEmpty()) {
            Text(
                text = uiState.error ?: "Erreur",
                fontFamily = SoraFontFamily,
                color = Color.Red.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                // Hero (55% of screen height)
                item {
                    focusedItem?.let { hero ->
                        HeroSection(
                            item = hero,
                            artUrl = null, // populated via ServerManager
                            accentColor = AccentDefault,
                            dominantWashed = AccentDefault.copy(alpha = 0.18f),
                            onPlayClick = {
                                context.startActivity(
                                    Intent(context, PlayerActivity::class.java).apply {
                                        putExtra(PlayerActivity.EXTRA_RATING_KEY, hero.ratingKey)
                                        putExtra(PlayerActivity.EXTRA_TITLE, hero.title)
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                        )
                    } ?: Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .background(BackgroundDark)
                    )
                }

                items(uiState.rows, key = { row ->
                    when (row) {
                        is HomeRow.OnDeck -> "on_deck"
                        is HomeRow.Collection -> "col_${row.collectionKey}"
                        is HomeRow.RecentlyAdded -> "recently_added"
                        is HomeRow.LiveTV -> "live_tv"
                    }
                }) { row ->
                    when (row) {
                        is HomeRow.OnDeck -> RowSection(
                            title = "Continuer à regarder",
                            items = row.items,
                            aspect = CardAspect.LANDSCAPE,
                            buildImageUrl = { null },
                            onItemClick = { item ->
                                context.startActivity(
                                    Intent(context, DetailActivity::class.java).apply {
                                        putExtra(DetailActivity.EXTRA_RATING_KEY, item.ratingKey)
                                    }
                                )
                            },
                            onItemFocus = viewModel::onItemFocused
                        )
                        is HomeRow.Collection -> RowSection(
                            title = row.title,
                            items = row.items,
                            aspect = CardAspect.PORTRAIT,
                            buildImageUrl = { null },
                            onItemClick = { item ->
                                context.startActivity(
                                    Intent(context, DetailActivity::class.java).apply {
                                        putExtra(DetailActivity.EXTRA_RATING_KEY, item.ratingKey)
                                    }
                                )
                            },
                            onItemFocus = viewModel::onItemFocused
                        )
                        is HomeRow.RecentlyAdded -> RowSection(
                            title = "Ajouts récents",
                            items = row.items,
                            aspect = CardAspect.LANDSCAPE,
                            buildImageUrl = { null },
                            onItemClick = { item ->
                                context.startActivity(
                                    Intent(context, DetailActivity::class.java).apply {
                                        putExtra(DetailActivity.EXTRA_RATING_KEY, item.ratingKey)
                                    }
                                )
                            },
                            onItemFocus = viewModel::onItemFocused
                        )
                        is HomeRow.LiveTV -> RowSection(
                            title = "Live TV",
                            items = row.items,
                            aspect = CardAspect.LANDSCAPE,
                            buildImageUrl = { null },
                            onItemClick = {},
                            onItemFocus = viewModel::onItemFocused
                        )
                    }
                }
            }
        }
    }
}
