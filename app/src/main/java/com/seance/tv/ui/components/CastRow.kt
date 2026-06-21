package com.seance.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seance.tv.data.model.PlexRole
import com.seance.tv.ui.theme.AccentBright
import com.seance.tv.ui.theme.Dimens
import com.seance.tv.ui.theme.Motion
import com.seance.tv.ui.theme.SoraFontFamily
import com.seance.tv.ui.theme.SurfaceFocused
import com.seance.tv.ui.theme.TextMuted
import com.seance.tv.ui.theme.TextPrimary
import com.seance.tv.ui.theme.TextSecondary

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CastRow(
    cast: List<PlexRole>,
    buildImageUrl: (String?) -> String?,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = Dimens.safeH),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(cast.take(20), key = { it.tag + it.role }) { role ->
            CastMember(role, buildImageUrl(role.thumb))
        }
    }
}

@Composable
private fun CastMember(role: PlexRole, imageUrl: String?) {
    var focused by remember { mutableStateOf(false) }
    val p by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = tween(Motion.fast, easing = Motion.expoOut),
        label = "castFocus"
    )

    Column(
        modifier = Modifier.width(96.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer { val s = lerp(1f, 1.08f, p); scaleX = s; scaleY = s }
                .clip(CircleShape)
                .background(SurfaceFocused)
                .border(BorderStroke(lerp(0f, 2.5f, p).dp, AccentBright), CircleShape)
                .onFocusChanged { focused = it.isFocused }
                .focusable()
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = role.tag,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = role.tag.take(1).uppercase(),
                        fontFamily = SoraFontFamily,
                        fontSize = 30.sp,
                        color = TextMuted
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = role.tag,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (role.role.isNotBlank()) {
            Text(
                text = role.role,
                fontFamily = SoraFontFamily,
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
