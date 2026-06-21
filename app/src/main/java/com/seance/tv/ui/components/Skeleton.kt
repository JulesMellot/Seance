package com.seance.tv.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.seance.tv.ui.theme.Dimens
import com.seance.tv.ui.theme.Motion
import com.seance.tv.ui.theme.Surface
import com.seance.tv.ui.theme.SurfaceFocused

/** Brush animé de gauche à droite — base de tous les skeletons. */
@Composable
private fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = Motion.expoOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )
    return Brush.linearGradient(
        colors = listOf(Surface, SurfaceFocused, Surface),
        start = Offset(x, 0f),
        end = Offset(x + 600f, 0f)
    )
}

@Composable
fun SkeletonBox(modifier: Modifier = Modifier, radius: Int = 12) {
    Box(modifier = modifier.clip(RoundedCornerShape(radius.dp)).background(shimmerBrush()))
}

/** Écran d'accueil en chargement — hero + deux étagères fantômes. */
@Composable
fun HomeSkeleton(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Column(modifier = modifier) {
        // Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.heroHeight)
                .background(brush)
        ) {
            Column(
                modifier = Modifier
                    .padding(start = Dimens.safeH, bottom = 40.dp)
                    .align(androidx.compose.ui.Alignment.BottomStart)
            ) {
                SkeletonBox(Modifier.width(360.dp).height(40.dp))
                Spacer(Modifier.height(14.dp))
                SkeletonBox(Modifier.width(520.dp).height(16.dp))
                Spacer(Modifier.height(8.dp))
                SkeletonBox(Modifier.width(420.dp).height(16.dp))
                Spacer(Modifier.height(22.dp))
                SkeletonBox(Modifier.width(150.dp).height(44.dp), radius = 999)
            }
        }
        Spacer(Modifier.height(Dimens.rowGap))
        repeat(2) {
            SkeletonRow()
            Spacer(Modifier.height(Dimens.rowGap))
        }
    }
}

@Composable
private fun SkeletonRow() {
    Column {
        SkeletonBox(
            Modifier.padding(start = Dimens.safeH).width(180.dp).height(18.dp),
            radius = 6
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.padding(start = Dimens.safeH),
            horizontalArrangement = Arrangement.spacedBy(Dimens.cardGap)
        ) {
            repeat(6) {
                SkeletonBox(Modifier.width(133.dp).height(200.dp))
            }
        }
    }
}
