package com.seance.tv.ui.search

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardCapslock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.seance.tv.ui.components.CardAspect
import com.seance.tv.ui.components.MediaCard
import com.seance.tv.ui.theme.Accent
import com.seance.tv.ui.theme.BackgroundBase
import com.seance.tv.ui.theme.BackgroundDeep
import com.seance.tv.ui.theme.BorderSubtle
import com.seance.tv.ui.theme.Dimens
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.Radii
import com.seance.tv.ui.theme.SoraFontFamily
import com.seance.tv.ui.theme.Surface
import com.seance.tv.ui.theme.SurfaceFocused
import com.seance.tv.ui.theme.TextMuted
import com.seance.tv.ui.theme.TextPrimary

private val LETTERS: List<Char> = ('a'..'z').toList()
private val DIGITS: List<Char> = ('0'..'9').toList()

/** Variantes accentuées proposées en appui long (façon clavier mobile). */
private val ACCENT_MAP: Map<Char, List<Char>> = mapOf(
    'a' to listOf('à', 'â', 'ä'),
    'c' to listOf('ç'),
    'e' to listOf('é', 'è', 'ê', 'ë'),
    'i' to listOf('î', 'ï'),
    'o' to listOf('ô', 'ö'),
    'u' to listOf('û', 'ù', 'ü'),
    'y' to listOf('ÿ'),
    'n' to listOf('ñ')
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    onOpenDetail: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val recents by viewModel.recentSearches.collectAsState()
    var shifted by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val voiceAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!text.isNullOrBlank()) viewModel.voiceResult(text)
        }
    }
    fun launchVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites un titre…")
        }
        runCatching { voiceLauncher.launch(intent) }
    }

    Row(modifier = Modifier.fillMaxSize().background(BackgroundBase)) {

        // ── Panneau clavier ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(440.dp)
                .fillMaxHeight()
                .background(BackgroundDeep)
                .padding(start = 36.dp, top = Dimens.safeTop, end = 24.dp, bottom = 24.dp)
        ) {
            // Champ de saisie (+ micro si reconnaissance vocale dispo)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
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
                if (voiceAvailable) {
                    Spacer(Modifier.width(8.dp))
                    IconKey(
                        icon = Icons.Filled.Mic,
                        contentDescription = "Recherche vocale",
                        modifier = Modifier.size(52.dp),
                        onClick = { launchVoice() }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Lettres (6 colonnes). Appui long → variantes accentuées.
            LETTERS.chunked(6).forEach { rowKeys ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowKeys.forEach { key ->
                        LetterKey(
                            base = key,
                            shifted = shifted,
                            accents = ACCENT_MAP[key].orEmpty(),
                            onChar = { viewModel.append(it) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(4.dp))

            // Chiffres
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DIGITS.forEach { key ->
                    KeyButton(
                        label = key.toString(),
                        modifier = Modifier.size(width = 32.dp, height = 44.dp),
                        onClick = { viewModel.append(key) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Actions (icônes)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconKey(
                    icon = Icons.Filled.KeyboardCapslock,
                    contentDescription = "Majuscule",
                    modifier = Modifier.size(width = 60.dp, height = 48.dp),
                    active = shifted,
                    onClick = { shifted = !shifted }
                )
                IconKey(
                    icon = Icons.Filled.SpaceBar,
                    contentDescription = "Espace",
                    modifier = Modifier.height(48.dp).weight(1f),
                    onClick = { viewModel.append(' ') }
                )
                IconKey(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Retour arrière",
                    modifier = Modifier.size(48.dp),
                    onClick = { viewModel.backspace() }
                )
                IconKey(
                    icon = Icons.Filled.Close,
                    contentDescription = "Tout effacer",
                    modifier = Modifier.size(48.dp),
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
                state.query.isBlank() ->
                    if (recents.isEmpty()) {
                        CenterHint("Tapez pour rechercher un film ou une série")
                    } else {
                        RecentSearches(
                            recents = recents,
                            onSelect = { viewModel.useRecent(it) },
                            onClear = { viewModel.clearRecentSearches() }
                        )
                    }
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
                            onClick = {
                                viewModel.onResultOpened()
                                onOpenDetail(item.ratingKey)
                            }
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
    active: Boolean = false,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val bg = when {
        focused -> Accent
        active -> Accent.copy(alpha = 0.28f)
        else -> SurfaceFocused
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radii.chip))
            .background(bg)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LetterKey(
    base: Char,
    shifted: Boolean,
    accents: List<Char>,
    onChar: (Char) -> Unit
) {
    val ch = if (shifted) base.uppercaseChar() else base
    var focused by remember { mutableStateOf(false) }
    var showAccents by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val keyFocus = remember { FocusRequester() }

    // Rendre le focus à la touche quand le popup d'accents se ferme.
    LaunchedEffect(showAccents) {
        if (!showAccents && focused) runCatching { keyFocus.requestFocus() }
    }

    Box {
        Box(
            modifier = Modifier
                .size(48.dp)
                .focusRequester(keyFocus)
                .clip(RoundedCornerShape(Radii.chip))
                .background(if (focused) Accent else SurfaceFocused)
                .onFocusChanged { focused = it.isFocused }
                .combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = { onChar(ch) },
                    onLongClick = { if (accents.isNotEmpty()) showAccents = true }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ch.toString(),
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = if (focused) Color.Black else TextPrimary
            )
            // Pastille discrète : accents disponibles en appui long.
            if (accents.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (focused) Color.Black.copy(alpha = 0.45f) else Accent)
                )
            }
        }

        if (showAccents) {
            AccentPopup(
                accents = accents.map { if (shifted) it.uppercaseChar() else it },
                onPick = { showAccents = false; onChar(it) },
                onDismiss = { showAccents = false }
            )
        }
    }
}

@Composable
private fun AccentPopup(
    accents: List<Char>,
    onPick: (Char) -> Unit,
    onDismiss: () -> Unit
) {
    val yOffset = with(LocalDensity.current) { -(56.dp).roundToPx() }
    val first = remember { FocusRequester() }
    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(0, yOffset),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(Radii.chip))
                .background(BackgroundDeep)
                .border(1.dp, BorderSubtle, RoundedCornerShape(Radii.chip))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            accents.forEachIndexed { i, a ->
                AccentKey(
                    label = a.toString(),
                    focusRequester = if (i == 0) first else null,
                    onClick = { onPick(a) }
                )
            }
        }
    }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
}

@Composable
private fun AccentKey(
    label: String,
    focusRequester: FocusRequester?,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = (if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .size(44.dp)
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
            fontSize = 18.sp,
            color = if (focused) Color.Black else TextPrimary
        )
    }
}

@Composable
private fun IconKey(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val bg = when {
        focused -> Accent
        active -> Accent.copy(alpha = 0.28f)
        else -> SurfaceFocused
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radii.chip))
            .background(bg)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (focused) Color.Black else TextPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun RecentSearches(
    recents: List<String>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recherches récentes",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            RecentChip(label = "Effacer", onClick = onClear)
        }
        Spacer(Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            recents.forEach { q ->
                RecentChip(label = q, onClick = { onSelect(q) }, fill = true)
            }
        }
    }
}

@Composable
private fun RecentChip(
    label: String,
    onClick: () -> Unit,
    fill: Boolean = false
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = (if (fill) Modifier.fillMaxWidth(0.7f) else Modifier)
            .height(44.dp)
            .clip(RoundedCornerShape(Radii.pill))
            .background(if (focused) Accent else Surface)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
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
