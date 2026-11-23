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
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.OrchidMauve
import com.example.ui.theme.PaleLavender
import com.example.ui.theme.StudioSpacing
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
            .semantics {
                contentDescription = "Before and after comparison slider. Drag left or right to compare original selfie with studio headshot."
            }
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
        val handleSize = StudioSpacing.touchTarget
        val handleHalfPx = with(density) { (handleSize / 2).toPx() }
        val dividerX = (widthPx * splitFraction).coerceIn(0f, widthPx)

        // Base layer: Studio developed headshot (right side)
        Image(
            bitmap = afterImage,
            contentDescription = "Developed studio headshot",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Clipped top layer: Original casual selfie (left side)
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

        // High-contrast Midnight Indigo + Pale Lavender split line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * splitFraction
            drawLine(
                color = MidnightIndigo.copy(alpha = 0.7f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 6f
            )
            drawLine(
                color = PaleLavender,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2.5f
            )
        }

        // Tactile 48dp accessible circular handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset((dividerX - handleHalfPx).roundToInt(), 0) }
                .size(handleSize),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DeepPlum)
                    .border(2.dp, PaleLavender, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Code,
                    contentDescription = "Drag horizontally to compare",
                    tint = PaleLavender,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // High-contrast corner comparison badges (13.2:1 WCAG AAA on Midnight Indigo)
        if (splitFraction > 0.24f) {
            ComparisonPillBadge(
                label = stringResource(R.string.label_original),
                emphasized = false,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
        }
        if (splitFraction < 0.76f) {
            ComparisonPillBadge(
                label = stringResource(R.string.label_studio),
                emphasized = true,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun ComparisonPillBadge(
    label: String,
    emphasized: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (emphasized) DeepPlum.copy(alpha = 0.92f) else MidnightIndigo.copy(alpha = 0.88f))
            .border(
                width = 1.dp,
                color = if (emphasized) PaleLavender.copy(alpha = 0.75f) else OrchidMauve.copy(alpha = 0.65f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PaleLavender
        )
    }
}
