package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.ObsidianDarkroom
import com.example.ui.theme.TungstenAmber
import kotlin.math.roundToInt

@Composable
fun BeforeAfterSlider(
    beforeBitmap: Bitmap,
    afterBitmap: Bitmap,
    splitFraction: Float,
    onSplitFractionChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val beforeImage = remember(beforeBitmap) { beforeBitmap.asImageBitmap() }
    val afterImage = remember(afterBitmap) { afterBitmap.asImageBitmap() }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .testTag("before_after_slider")
            .semantics { contentDescription = "Before and after headshot comparison slider" }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (size.width > 0) {
                        onSplitFractionChange((offset.x / size.width).coerceIn(0.05f, 0.95f))
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    if (size.width > 0) {
                        onSplitFractionChange((change.position.x / size.width).coerceIn(0.05f, 0.95f))
                    }
                }
            }
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val handleSize = 48.dp
        val handleHalfPx = with(density) { (handleSize / 2).toPx() }
        val dividerX = (widthPx * splitFraction).coerceIn(0f, widthPx)

        // Base layer: Studio developed headshot (right side visible beyond split)
        Image(
            bitmap = afterImage,
            contentDescription = "Developed studio headshot",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Clipped top layer: Original casual selfie (left side up to splitFraction)
        Image(
            bitmap = beforeImage,
            contentDescription = "Original casual selfie",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(right = size.width * splitFraction) {
                        this@drawWithContent.drawContent()
                    }
                }
        )

        // Luminous tungsten split hairline
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * splitFraction
            drawLine(
                color = Color.Black.copy(alpha = 0.45f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 5f
            )
            drawLine(
                color = TungstenAmber,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2.5f
            )
        }

        // Tactile 48dp minimum touch target circular handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset((dividerX - handleHalfPx).roundToInt(), 0) }
                .size(handleSize),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ObsidianDarkroom.copy(alpha = 0.88f))
                    .border(1.5.dp, TungstenAmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Code,
                    contentDescription = "Drag to compare original and studio headshot",
                    tint = TungstenAmber,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Corner darkroom HUD labels
        if (splitFraction > 0.22f) {
            HudComparisonBadge(
                label = stringResource(R.string.label_original),
                accent = false,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
        }
        if (splitFraction < 0.78f) {
            HudComparisonBadge(
                label = stringResource(R.string.label_studio),
                accent = true,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun HudComparisonBadge(
    label: String,
    accent: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ObsidianDarkroom.copy(alpha = 0.76f))
            .border(
                width = 1.dp,
                color = if (accent) TungstenAmber.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (accent) TungstenAmber else MaterialTheme.colorScheme.secondary
        )
    }
}
