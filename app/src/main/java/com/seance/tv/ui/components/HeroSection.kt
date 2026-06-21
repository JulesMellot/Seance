package com.seance.tv.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
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
import com.seance.tv.ui.theme.Accent
import com.seance.tv.ui.theme.BackgroundBase
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.Motion
import com.seance.tv.ui.theme.Radii
import com.seance.tv.ui.theme.SoraFontFamily
import com.seance.tv.ui.theme.TextPrimary

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroSection(
    item: MediaItem,
    artUrl: String?,
    accent: Color = Accent,
    eyebrow: String = "À LA UNE",
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    playFocusRequester: FocusRequester? = null,
    buildImageUrl: (String?) -> String? = { it },
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {

        // Fond cinématique — crossfade quand l'élément focalisé change
        Crossfade(
            targetState = artUrl,
            animationSpec = tween(Motion.crossfade, easing = Motion.expoOut),
            label = "heroArt"
        ) { url ->
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(BackgroundBase))
            }
        }

        // Scrim horizontal (lisibilité du texte à gauche)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.78f)
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(
                        0f to BackgroundBase.copy(alpha = 0.97f),
                        0.55f to BackgroundBase.copy(alpha = 0.55f),
                        1f to Color.Transparent
                    )
                )
        )
        // Scrim vertical (fond vers le bas, fond app)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.5f to Color.Transparent,
                        1f to BackgroundBase
                    )
                )
        )

        // Contenu
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.56f)
                .padding(start = 48.dp, bottom = 44.dp)
        ) {
            // Texte (crossfade sur changement d'élément) — CTA en dehors pour stabilité du focus
            Crossfade(
                targetState = item,
                animationSpec = tween(Motion.crossfade, easing = Motion.expoOut),
                label = "heroText"
            ) { hero ->
                Column {
                    Text(
                        text = eyebrow,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 2.5.sp,
                        color = accent,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Logo-titre transparent si dispo, sinon titre en serif Lora.
                    val logo = hero.clearLogo?.let { buildImageUrl(it) }
                    if (logo != null) {
                        AsyncImage(
                            model = logo,
                            contentDescription = hero.title,
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart,
                            modifier = Modifier
                                .heightIn(max = 116.dp)
                                .widthIn(max = 440.dp)
                                .padding(bottom = 6.dp)
                        )
                    } else {
                        Text(
                            text = hero.title,
                            fontFamily = LoraFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            fontSize = 44.sp,
                            lineHeight = 50.sp,        // clearance jambages italiques
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    MetaLine(hero, accent)

                    if (hero.summary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = hero.summary,
                            fontFamily = SoraFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color.White.copy(alpha = 0.74f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val resume = item.viewOffset != null && item.viewOffset > 0
                Button(
                    onClick = onPlay,
                    modifier = if (playFocusRequester != null) Modifier.focusRequester(playFocusRequester) else Modifier,
                    colors = ButtonDefaults.colors(
                        containerColor = accent,
                        contentColor = Color.Black,
                        focusedContainerColor = Color.White,
                        focusedContentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (resume) "▶  Reprendre" else "▶  Lire",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Button(
                    onClick = onDetails,
                    colors = ButtonDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.16f),
                        contentColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedContentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Détails",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaLine(item: MediaItem, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        item.rating?.let {
            Text("★ %.1f".format(it), fontFamily = SoraFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = accent)
            Dot()
        }
        item.year?.let {
            Text(it.toString(), fontFamily = SoraFontFamily, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
        }
        // Durée (films) ou nombre de saisons (séries)
        (item.runtimeLabel?.takeIf { item.isMovie } ?: item.seasonsLabel)?.let {
            Dot()
            Text(it, fontFamily = SoraFontFamily, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
        }
        item.contentRating?.let {
            Dot()
            Pill(it)
        }
        item.genres.takeIf { it.isNotEmpty() }?.let { genres ->
            Dot()
            Text(
                text = genres.take(3).joinToString(" · ") { it.tag },
                fontFamily = SoraFontFamily,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Pill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radii.chip))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, fontFamily = SoraFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
private fun Dot() {
    Box(
        modifier = Modifier
            .padding(horizontal = 11.dp)
            .height(3.dp)
            .clip(RoundedCornerShape(Radii.pill))
            .background(Color.White.copy(alpha = 0.4f))
            .width(3.dp)
    )
}
