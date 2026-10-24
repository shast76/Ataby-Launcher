package com.example.tvlauncher

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/** Liquid Glass benzeri yüzey: yarı saydam dolgu + parlak kenar. */
fun Modifier.glass(
    enabled: Boolean,
    focused: Boolean = false,
    shape: Shape = RoundedCornerShape(20.dp)
): Modifier {
    val top = if (enabled) 0.24f else 0.10f
    val bottom = if (enabled) 0.06f else 0.10f
    val edgeA = if (focused) 0.95f else 0.50f
    val edgeB = if (focused) 0.60f else 0.25f
    return this
        .clip(shape)
        .background(Brush.verticalGradient(listOf(Color.White.copy(top), Color.White.copy(bottom))))
        .border(
            if (focused) 2.dp else 1.dp,
            Brush.linearGradient(listOf(Color.White.copy(edgeA), Color.White.copy(0.08f), Color.White.copy(edgeB))),
            shape
        )
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.tvClick(onClick: () -> Unit, onLong: (() -> Unit)? = null): Modifier = composed {
    combinedClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
        onLongClick = onLong
    )
}

@Composable
fun Wallpaper() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            Brush.linearGradient(
                listOf(Color(0xFF0B1026), Color(0xFF1B1140), Color(0xFF0A2A3F)),
                start = Offset.Zero, end = Offset(size.width, size.height)
            )
        )
        fun blob(c: Color, x: Float, y: Float, r: Float) = drawRect(
            Brush.radialGradient(listOf(c, Color.Transparent), center = Offset(size.width * x, size.height * y), radius = r)
        )
        val m = size.minDimension
        blob(Color(0x884F7CFF), 0.18f, 0.22f, m * 0.8f)
        blob(Color(0x77B04FFF), 0.85f, 0.30f, m * 0.7f)
        blob(Color(0x6600D4C8), 0.60f, 0.95f, m * 0.8f)
    }
}
