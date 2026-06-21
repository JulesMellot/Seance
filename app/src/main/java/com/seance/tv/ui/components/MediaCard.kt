package com.seance.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seance.tv.data.model.MediaItem
import com.seance.tv.ui.theme.SoraFontFamily

enum class CardAspect { PORTRAIT, LANDSCAPE }

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaCard(
    item: MediaItem,
    imageUrl: String?,
    aspect: CardAspect = CardAspect.PORTRAIT,
    accentColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onFocus: () -> Unit = {}
) {
    val aspectRatio = if (aspect == CardAspect.PORTRAIT) 2f / 3f else 16f / 9f

    Card(
        onClick = onClick,
        onLongClick = {},
        modifier = modifier.aspectRatio(aspectRatio),
        colors = CardDefaults.colors(
            containerColor = Color(0xFF1C1C1C),
            focusedContainerColor = Color(0xFF2A2A2A)
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    accentColor.takeIf { it != Color.Transparent } ?: Color(0xFFE5A00D)
                )
            )
        ),
        scale = CardDefaults.scale(focusedScale = 1.05f),
        shape = CardDefaults.shape(shape = RoundedCornerShape(8.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2A2A2A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title.take(2).uppercase(),
                        fontFamily = SoraFontFamily,
                        fontSize = 24.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            if (item.progressFraction > 0.01f) {
                ProgressBar(
                    fraction = item.progressFraction,
                    accentColor = accentColor,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(3.dp)
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(
    fraction: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(Color.White.copy(alpha = 0.3f))) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(3.dp)
                .background(accentColor.takeIf { it != Color.Transparent } ?: Color(0xFFE5A00D))
        )
    }
}
