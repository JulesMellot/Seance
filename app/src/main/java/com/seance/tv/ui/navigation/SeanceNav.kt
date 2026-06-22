package com.seance.tv.ui.navigation

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.foundation.focusGroup
import androidx.compose.ui.focus.onFocusChanged
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.seance.tv.data.model.MediaItem
import com.seance.tv.ui.browse.BrowseScreen
import com.seance.tv.ui.detail.DetailScreen
import com.seance.tv.ui.detail.DetailViewModel
import com.seance.tv.ui.home.HomeScreen
import com.seance.tv.ui.player.PlayerActivity
import com.seance.tv.ui.search.SearchScreen
import com.seance.tv.ui.settings.SettingsScreen
import com.seance.tv.ui.theme.Accent
import com.seance.tv.ui.theme.BackgroundBase
import com.seance.tv.ui.theme.BackgroundDeep
import com.seance.tv.ui.theme.Dimens
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.Motion
import com.seance.tv.ui.theme.SoraFontFamily
import com.seance.tv.ui.theme.SurfaceFocused
import com.seance.tv.ui.theme.TextMuted
import com.seance.tv.ui.theme.TextPrimary
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

object Routes {
    const val HOME = "home"
    const val MOVIES = "browse/movie"
    const val SHOWS = "browse/show"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val DETAIL_PATTERN = "detail/{ratingKey}"
    fun detail(ratingKey: String) = "detail/$ratingKey"
}

private enum class NavDest(
    val route: String,
    val label: String,
    val icon: ImageVector,         // contour, au repos
    val selectedIcon: ImageVector  // plein, quand actif/focus
) {
    Search(Routes.SEARCH, "Recherche", Icons.Outlined.Search, Icons.Filled.Search),
    Home(Routes.HOME, "Accueil", Icons.Outlined.Home, Icons.Filled.Home),
    Movies(Routes.MOVIES, "Films", Icons.Outlined.Movie, Icons.Filled.Movie),
    Shows(Routes.SHOWS, "Séries", Icons.Outlined.Tv, Icons.Filled.Tv),
    Settings(Routes.SETTINGS, "Paramètres", Icons.Outlined.Settings, Icons.Filled.Settings)
}

@Composable
fun AppScaffold(profileThumb: String? = null) {
    val navController = rememberNavController()

    Row(modifier = Modifier.fillMaxSize().background(BackgroundBase)) {
        NavRail(navController, profileThumb)
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            SeanceNavHost(navController)
        }
    }
}

@Composable
private fun NavRail(navController: NavHostController, profileThumb: String?) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    var expanded by remember { mutableStateOf(false) }
    val width by animateDpAsState(
        targetValue = if (expanded) Dimens.railExpanded else Dimens.railCollapsed,
        animationSpec = tween(220),
        label = "railWidth"
    )

    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .onFocusChanged { expanded = it.hasFocus }
            .focusGroup()
            .background(
                Brush.horizontalGradient(
                    listOf(BackgroundDeep, BackgroundBase)
                )
            )
            .padding(vertical = 28.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Avatar du profil actif (remplace le wordmark)
        Row(
            modifier = Modifier.height(48.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SurfaceFocused),
                contentAlignment = Alignment.Center
            ) {
                if (!profileThumb.isNullOrBlank()) {
                    AsyncImage(
                        model = profileThumb,
                        contentDescription = "Profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        text = "S",
                        fontFamily = LoraFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 22.sp,
                        color = Accent
                    )
                }
            }
            if (expanded) {
                Spacer(Modifier.width(14.dp))
                Text(
                    text = "Séance",
                    fontFamily = LoraFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp,
                    color = Accent,
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.height(18.dp))

        NavDest.entries.forEach { dest ->
            val selected = when (dest) {
                NavDest.Home -> currentRoute == Routes.HOME
                NavDest.Movies -> currentRoute == Routes.MOVIES
                NavDest.Shows -> currentRoute == Routes.SHOWS
                NavDest.Search -> currentRoute == Routes.SEARCH
                NavDest.Settings -> currentRoute == Routes.SETTINGS
            }
            RailItem(
                dest = dest,
                selected = selected,
                expanded = expanded,
                onClick = {
                    if (currentRoute != dest.route) {
                        navController.navigate(dest.route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun RailItem(
    dest: NavDest,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val active = selected || focused
    val tint = if (active) Accent else TextMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (focused) Color.White.copy(alpha = 0.08f) else Color.Transparent)
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicateur de sélection (liseré amber)
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(if (selected) Accent else Color.Transparent)
        )
        Spacer(Modifier.width(10.dp))
        Icon(
            imageVector = if (active) dest.selectedIcon else dest.icon,
            contentDescription = dest.label,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
        if (expanded) {
            Spacer(Modifier.width(16.dp))
            Text(
                text = dest.label,
                fontFamily = SoraFontFamily,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 15.sp,
                color = if (active) TextPrimary else TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SeanceNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { fadeIn(tween(Motion.crossfade)) },
        exitTransition = { fadeOut(tween(Motion.crossfade)) },
        popEnterTransition = { fadeIn(tween(Motion.crossfade)) },
        popExitTransition = { fadeOut(tween(Motion.crossfade)) }
    ) {
        composable(Routes.HOME) {
            HomeScreen(onOpenDetail = { navController.navigate(Routes.detail(it)) })
        }
        composable(Routes.MOVIES) {
            BrowseScreen(
                sectionType = "movie",
                onOpenDetail = { navController.navigate(Routes.detail(it)) }
            )
        }
        composable(Routes.SHOWS) {
            BrowseScreen(
                sectionType = "show",
                onOpenDetail = { navController.navigate(Routes.detail(it)) }
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(onOpenDetail = { navController.navigate(Routes.detail(it)) })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
        composable(Routes.DETAIL_PATTERN) { entry ->
            val ratingKey = entry.arguments?.getString("ratingKey") ?: return@composable
            val context = LocalContext.current
            val viewModel: DetailViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            androidx.compose.runtime.LaunchedEffect(ratingKey) { viewModel.load(ratingKey) }

            DetailScreen(
                state = state,
                imageUrl = viewModel::imageUrl,
                onPlay = { item, audioLang, subLang, subsOff ->
                    val partKey = item.media.firstOrNull()?.parts?.firstOrNull()?.key
                    launchPlayer(
                        context, item, partKey?.let { viewModel.streamUrl(it) },
                        audioLanguage = audioLang,
                        subtitleLanguage = subLang,
                        subtitlesDisabled = subsOff
                    )
                },
                onSeasonSelected = viewModel::selectSeason,
                onItemClick = { navController.navigate(Routes.detail(it.ratingKey)) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/** Lance le lecteur (Activity plein écran séparée). */
fun launchPlayer(
    context: Context,
    item: MediaItem,
    streamUrl: String?,
    audioLanguage: String? = null,
    subtitleLanguage: String? = null,
    subtitlesDisabled: Boolean = false
) {
    val partKey = item.media.firstOrNull()?.parts?.firstOrNull()?.key
    context.startActivity(
        Intent(context, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_RATING_KEY, item.ratingKey)
            putExtra(PlayerActivity.EXTRA_TITLE, item.title)
            if (partKey != null && streamUrl != null) {
                putExtra(PlayerActivity.EXTRA_PART_KEY, partKey)
                putExtra(PlayerActivity.EXTRA_STREAM_URL, streamUrl)
            }
            item.duration?.let { putExtra(PlayerActivity.EXTRA_DURATION, it) }
            item.viewOffset?.let { putExtra(PlayerActivity.EXTRA_VIEW_OFFSET, it) }
            audioLanguage?.let { putExtra(PlayerActivity.EXTRA_AUDIO_LANG, it) }
            subtitleLanguage?.let { putExtra(PlayerActivity.EXTRA_SUBTITLE_LANG, it) }
            if (subtitlesDisabled) putExtra(PlayerActivity.EXTRA_SUBS_OFF, true)
        }
    )
}
