package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.DeepPlumDestructive
import com.example.ui.theme.DustyRose
import com.example.ui.theme.DustyRoseBorder
import com.example.ui.theme.DustyRoseMuted
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.OrchidMauve
import com.example.ui.theme.PaleLavender
import com.example.ui.theme.PaleLavenderElevated
import com.example.ui.theme.StudioSpacing

/**
 * Adds a crisp, high-contrast keyboard/assistive focus-visible ring using Orchid Mauve / Midnight Indigo.
 */
fun Modifier.brandFocusRing(
    shape: Shape = RoundedCornerShape(14.dp)
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val ringColor by animateColorAsState(
        targetValue = if (isFocused) MidnightIndigo else Color.Transparent,
        animationSpec = tween(150),
        label = "focus_ring"
    )
    this
        .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
        .border(
            width = if (isFocused) 2.dp else 0.dp,
            color = ringColor,
            shape = shape
        )
}

/**
 * Tier 1: Primary Call-to-Action — High-emphasis Deep Plum (#69306D) fill with Pale Lavender (#F2D7EE) text.
 * Strictly reserved for the single most important action per view, sheet, or dialog.
 */
@Composable
fun PrimaryCtaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepPlum,
            contentColor = PaleLavender,
            disabledContainerColor = DustyRose,
            disabledContentColor = MidnightIndigo.copy(alpha = 0.55f)
        ),
        contentPadding = contentPadding,
        modifier = modifier
            .defaultMinSize(minHeight = StudioSpacing.primaryButtonHeight)
            .brandFocusRing(shape),
        content = content
    )
}

/**
 * Tier 2: Secondary Action — Soft Dusty Rose tint with Orchid Mauve outline and high-contrast Midnight Indigo text.
 */
@Composable
fun SecondaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val bgColor by animateColorAsState(
        targetValue = if (selected) DeepPlum else PaleLavenderElevated,
        animationSpec = tween(160),
        label = "secondary_bg"
    )
    val fgColor by animateColorAsState(
        targetValue = if (selected) PaleLavender else MidnightIndigo,
        animationSpec = tween(160),
        label = "secondary_fg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) DeepPlum else OrchidMauve,
        animationSpec = tween(160),
        label = "secondary_border"
    )

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        border = BorderStroke(if (selected) 1.5.dp else 1.25.dp, if (enabled) borderColor else DustyRoseBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = bgColor,
            contentColor = fgColor,
            disabledContainerColor = DustyRoseMuted,
            disabledContentColor = MidnightIndigo.copy(alpha = 0.45f)
        ),
        contentPadding = contentPadding,
        modifier = modifier
            .defaultMinSize(minHeight = StudioSpacing.touchTarget)
            .brandFocusRing(shape),
        content = content
    )
}

/**
 * Tier 3: Tertiary / Ghost Action — Clean low-emphasis button with high-contrast Midnight Indigo or Deep Plum ink.
 */
@Composable
fun TertiaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = DeepPlum,
            disabledContentColor = MidnightIndigo.copy(alpha = 0.45f)
        ),
        contentPadding = contentPadding,
        modifier = modifier
            .defaultMinSize(minHeight = StudioSpacing.touchTarget)
            .brandFocusRing(shape),
        content = content
    )
}

/**
 * Tier 4: Destructive Action — Clearly differentiated visually with confirmation friction & explicit iconography.
 */
@Composable
fun DestructiveActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    if (filled) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepPlumDestructive,
                contentColor = PaleLavender,
                disabledContainerColor = DustyRose,
                disabledContentColor = MidnightIndigo.copy(alpha = 0.5f)
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            modifier = modifier
                .defaultMinSize(minHeight = StudioSpacing.touchTarget)
                .brandFocusRing(shape),
            content = content
        )
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            border = BorderStroke(1.5.dp, DeepPlumDestructive),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = DustyRoseMuted,
                contentColor = DeepPlumDestructive
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = modifier
                .defaultMinSize(minHeight = StudioSpacing.touchTarget)
                .brandFocusRing(shape),
            content = content
        )
    }
}

/**
 * Accessible form input with a persistent visible label above the field, crisp focus outline,
 * optional helper text, and clear inline validation feedback.
 */
@Composable
fun LabeledStudioTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    helperText: String? = null,
    errorText: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 5
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = StudioSpacing.maxReadingWidth),
        verticalArrangement = Arrangement.spacedBy(StudioSpacing.micro)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MidnightIndigo
        )

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MidnightIndigo.copy(alpha = 0.55f)
                )
            },
            isError = errorText != null,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MidnightIndigo),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeepPlum,
                unfocusedBorderColor = DustyRoseBorder,
                errorBorderColor = DeepPlumDestructive,
                focusedContainerColor = PaleLavenderElevated,
                unfocusedContainerColor = PaleLavenderElevated,
                errorContainerColor = DustyRoseMuted,
                cursorColor = DeepPlum
            ),
            modifier = fieldModifier.fillMaxWidth()
        )

        if (errorText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = DeepPlumDestructive,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.labelMedium,
                    color = DeepPlumDestructive
                )
            }
        } else if (helperText != null) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = DeepPlum,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Consistent editorial section header with clear visual hierarchy and optional right-aligned tabular badge.
 */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    badgeText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MidnightIndigo
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = DeepPlum
                )
            }
        }

        if (badgeText != null) {
            Spacer(modifier = Modifier.width(StudioSpacing.tight))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DustyRoseMuted,
                border = BorderStroke(1.dp, DustyRoseBorder)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MidnightIndigo,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
