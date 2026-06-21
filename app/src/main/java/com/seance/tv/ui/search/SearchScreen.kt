package com.seance.tv.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
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
import com.seance.tv.ui.components.CardAspect
import com.seance.tv.ui.components.MediaCard
import com.seance.tv.ui.theme.Accent
import com.seance.tv.ui.theme.BackgroundBase
import com.seance.tv.ui.theme.BackgroundDeep
import com.seance.tv.ui.theme.Dimens
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.Radii
import com.seance.tv.ui.theme.SoraFontFamily
import com.seance.tv.ui.theme.Surface
import com.seance.tv.ui.theme.SurfaceFocused
import com.seance.tv.ui.theme.TextMuted
import com.seance.tv.ui.theme.TextPrimary

private val KEYS: List<Char> = (('A'..'Z') + ('0'..'9')).toList()

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    onOpenDetail: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Row(modifier = Modifier.fillMaxSize().background(BackgroundBase)) {

        // ── Panneau clavier ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(440.dp)
                .fillMaxHeight()
                .background(BackgroundDeep)
                .padding(start = 36.dp, top = Dimens.safeTop, end = 24.dp, bottom = 24.dp)
        ) {
            // Champ de saisie
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(Radii.chip))
                    .background(Surface)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = state.query.ifBlank { "Rechercher…" },
                    fontFamily = SoraFontFamily,
                    fontSize = 16.sp,
                    color = if (state.query.isBlank()) TextMuted else TextPrimary
                )
            }

            Spacer(Modifier.height(20.dp))

            // Grille de touches (6 colonnes)
            KEYS.chunked(6).forEach { rowKeys ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowKeys.forEach { key ->
                        KeyButton(
                            label = key.toString(),
                            modifier = Modifier.size(48.dp),
                            onClick = { viewModel.append(key) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KeyButton(
                    label = "Espace",
                    modifier = Modifier.height(48.dp).width(152.dp),
                    onClick = { viewModel.append(' ') }
                )
                KeyButton(
                    label = "⌫",
                    modifier = Modifier.size(48.dp),
                    onClick = { viewModel.backspace() }
                )
                KeyButton(
                    label = "Effacer",
                    modifier = Modifier.height(48.dp).width(96.dp),
                    onClick = { viewModel.clear() }
                )
            }
        }

        // ── Résultats ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(start = 36.dp, top = Dimens.safeTop, end = 32.dp)
        ) {
            when {
                state.query.isBlank() -> CenterHint("Tapez pour rechercher un film ou une série")
                state.isSearching && state.results.isEmpty() -> CenterHint("Recherche…")
                state.hasSearched && state.results.isEmpty() -> CenterHint("Aucun résultat pour « ${state.query} »")
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.gridGap),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 48.dp)
                ) {
                    items(state.results, key = { it.ratingKey }) { item ->
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
}

@Composable
private fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radii.chip))
            .background(if (focused) Accent else SurfaceFocused)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (label.length > 1) 13.sp else 17.sp,
            color = if (focused) Color.Black else TextPrimary
        )
    }
}

@Composable
private fun CenterHint(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            fontFamily = LoraFontFamily,
            fontStyle = FontStyle.Italic,
            fontSize = 22.sp,
            color = TextMuted
        )
    }
}
