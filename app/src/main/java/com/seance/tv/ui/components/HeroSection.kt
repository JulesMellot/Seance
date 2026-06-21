package com.seance.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seance.tv.data.model.MediaItem
import com.seance.tv.ui.theme.LoraFontFamily
import com.seance.tv.ui.theme.SoraFontFamily

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroSection(
    item: MediaItem,
    artUrl: String?,
    accentColor: Color,
    dominantWashed: Color,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // Background art
        if (artUrl != null) {
            AsyncImage(
                model = artUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(dominantWashed))
        }

        // Gradient overlay — bottom fade to transparent
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Accent tint overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dominantWashed)
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 48.dp, bottom = 32.dp, end = 200.dp)
        ) {
            Text(
                text = item.title,
                fontFamily = LoraFontFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 42.sp,
                color = Color.White,
                lineHeight = 48.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                item.year?.let {
                    MetaChip(text = it.toString(), accentColor = accentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                item.contentRating?.let {
                    MetaChip(text = it, accentColor = accentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                item.duration?.let {
                    val hours = it / 3_600_000
                    val mins = (it % 3_600_000) / 60_000
                    val formatted = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                    Text(
                        text = formatted,
                        fontFamily = SoraFontFamily,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            if (item.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = item.summary,
                    fontFamily = SoraFontFamily,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 3,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.colors(
                    containerColor = accentColor,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (item.viewOffset != null && item.viewOffset > 0) "Reprendre" else "Lire",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MetaChip(text: String, accentColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(accentColor.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontFamily = SoraFontFamily,
            fontSize = 12.sp,
            color = accentColor
        )
    }
}
