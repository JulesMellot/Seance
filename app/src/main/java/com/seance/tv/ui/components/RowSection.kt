package com.seance.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.seance.tv.data.model.MediaItem
import com.seance.tv.ui.theme.Accent
import com.seance.tv.ui.theme.Dimens
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.Motion
import com.seance.tv.ui.theme.Radii
import com.seance.tv.ui.theme.SoraFontFamily
import com.seance.tv.ui.theme.TextPrimary

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun RowSection(
    title: String,
    items: List<MediaItem>,
    aspect: CardAspect = CardAspect.PORTRAIT,
    accentColor: Color = Accent,
    numbered: Boolean = false,
    active: Boolean = false,
    buildImageUrl: (MediaItem) -> String?,
    onItemClick: (MediaItem) -> Unit,
    onItemFocus: (MediaItem) -> Unit = {}
) {
    val cardWidth = if (aspect == CardAspect.PORTRAIT) Dimens.cardPortraitW else Dimens.cardLandscapeW

    // La rangée active grossit légèrement et passe au premier plan ; les autres se ternissent.
    val p by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(Motion.medium, easing = Motion.expoOut),
        label = "rowActive"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val s = lerp(1f, 1.05f, p)
                scaleX = s
                scaleY = s
                alpha = lerp(0.5f, 1f, p)
                transformOrigin = TransformOrigin(0f, 0.5f)
            }
    ) {
        // En-tête d'étagère : fin liseré accent + titre Sora
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = Dimens.safeH, bottom = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(Radii.pill))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimary.copy(alpha = 0.94f)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = Dimens.safeH, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(if (numbered) 4.dp else Dimens.cardGap)
        ) {
            itemsIndexed(items, key = { _, item -> item.ratingKey }) { index, item ->
                if (numbered) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        RankNumeral(index + 1)
                        MediaCard(
                            item = item,
                            imageUrl = buildImageUrl(item),
                            aspect = aspect,
                            accentColor = accentColor,
                            modifier = Modifier.width(cardWidth),
                            onClick = { onItemClick(item) },
                            onFocus = { onItemFocus(item) }
                        )
                    }
                } else {
                    MediaCard(
                        item = item,
                        imageUrl = buildImageUrl(item),
                        aspect = aspect,
                        accentColor = accentColor,
                        modifier = Modifier.width(cardWidth),
                        onClick = { onItemClick(item) },
                        onFocus = { onItemFocus(item) }
                    )
                }
            }
        }
    }
}

/** Grand chiffre de classement (façon Top 10), aligné bas, semi-transparent. */
@Composable
private fun RankNumeral(rank: Int) {
    Box(
        modifier = Modifier
            .width(if (rank >= 10) 76.dp else 52.dp)
            .padding(end = 4.dp, bottom = 20.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Text(
            text = rank.toString(),
            fontFamily = LoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 92.sp,
            color = Color.White.copy(alpha = 0.16f)
        )
    }
}
