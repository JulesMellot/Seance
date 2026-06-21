package com.seance.tv.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Système de design Séance — un seul accent verrouillé (amber Plex), échelle d'off-black
 * en couches (jamais de noir pur), une seule échelle de rayons, motion "expo-out" retenue.
 */

// ── Couleurs : base neutre sombre en couches ──────────────────────────────
val BackgroundDeep = Color(0xFF09090B)   // fond le plus profond (derrière le hero)
val BackgroundBase = Color(0xFF0E0E12)   // fond applicatif
val Surface = Color(0xFF17171C)          // cards au repos
val SurfaceFocused = Color(0xFF24242D)   // surface surélevée / focus
val BorderSubtle = Color(0x14FFFFFF)     // séparateurs discrets (8% blanc)

val TextPrimary = Color(0xFFF4F4F6)      // off-white (pas de blanc pur)
val TextSecondary = Color(0xFF9C9CA6)    // texte secondaire
val TextMuted = Color(0x73FFFFFF)        // ~45% blanc

// Accent unique — amber Plex. Verrouillé sur toute l'app.
val Accent = Color(0xFFE5A00D)
val AccentBright = Color(0xFFF3B73A)     // highlight de focus (jamais néon)
val AccentSoft = Color(0x1FE5A00D)       // tint amber très léger (lavé)

// ── Rayons : échelle verrouillée (cards 12, chips 8, boutons pill) ────────
object Radii {
    val card = 12.dp
    val chip = 8.dp
    val pill = 999.dp
}

// ── Espacement : marges safe TV + rythme des étagères ─────────────────────
// Échelle volontairement compacte (~80 %) : plus de contenu à l'écran, plus raffiné.
object Dimens {
    val safeH = 44.dp        // marge horizontale safe (overscan TV)
    val safeTop = 24.dp
    val rowGap = 22.dp       // espace vertical entre étagères
    val cardGap = 14.dp      // espace entre cards d'une étagère
    val heroHeight = 520.dp  // hero cinématique (plein écran via fillParentMaxHeight)

    // Rail de navigation latéral (pattern Netflix-TV : étroit au repos, large au focus)
    val railCollapsed = 80.dp
    val railExpanded = 224.dp

    // Browse en grille
    val gridColumns = 7           // posters par ligne
    val gridGap = 16.dp           // gouttière grille
    val cardPortraitW = 122.dp    // largeur card portrait (étagères home)
    val cardLandscapeW = 212.dp   // largeur card paysage (on-deck, épisodes)
}

// ── Motion : easing expo-out unique + durées ──────────────────────────────
object Motion {
    val expoOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    const val fast = 180
    const val medium = 300
    const val slow = 520
    const val crossfade = 420
}
