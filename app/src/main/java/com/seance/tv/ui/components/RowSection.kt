package com.seance.tv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.seance.tv.data.model.MediaItem
import com.seance.tv.ui.theme.OnSurface
import com.seance.tv.ui.theme.SoraFontFamily

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun RowSection(
    title: String,
    items: List<MediaItem>,
    aspect: CardAspect = CardAspect.PORTRAIT,
    accentColor: Color = Color.Transparent,
    buildImageUrl: (MediaItem) -> String?,
    onItemClick: (MediaItem) -> Unit,
    onItemFocus: (MediaItem) -> Unit = {}
) {
    val cardHeight = if (aspect == CardAspect.PORTRAIT) 200.dp else 140.dp
    val cardWidth = if (aspect == CardAspect.PORTRAIT) (200 * 2f / 3f).dp else (140 * 16f / 9f).dp

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontFamily = SoraFontFamily,
            fontSize = 16.sp,
            color = OnSurface.copy(alpha = 0.85f),
            modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.ratingKey }) { item ->
                MediaCard(
                    item = item,
                    imageUrl = buildImageUrl(item),
                    aspect = aspect,
                    accentColor = accentColor,
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardHeight),
                    onClick = { onItemClick(item) },
                    onFocus = { onItemFocus(item) }
                )
            }
        }
    }
}
