package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavyMusicSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    activeTrackColor: Color = MaterialTheme.colorScheme.primary,
    inactiveTrackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    trackHeight: Dp = 6.dp,
    thumbRadius: Dp = 8.dp,
    waveAmplitudeWhenPlaying: Dp = 3.dp,
    waveLength: Dp = 80.dp,
    waveAnimationDuration: Int = 2000,
    hideInactiveTrackPortion: Boolean = true,
    thumbLineHeightWhenInteracting: Dp = 24.dp
) {
    var isInteracting by remember { mutableStateOf(false) }
    var touchXFraction by remember { mutableFloatStateOf(value.coerceIn(0f, 1f)) }

    LaunchedEffect(value) {
        if (!isInteracting) {
            touchXFraction = value.coerceIn(0f, 1f)
        }
    }

    val density = LocalDensity.current

    // Infinite animation for wave phase
    val infiniteTransition = rememberInfiniteTransition(label = "WavePhaseTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(waveAnimationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    // Animated amplitude (flattens when paused or dragged)
    val targetAmplitude = if (isPlaying && !isInteracting) waveAmplitudeWhenPlaying else 0.dp
    val animatedAmplitude by animateDpAsState(
        targetValue = targetAmplitude,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "WaveAmplitude"
    )

    // Animated thumb height when interacting
    val targetThumbHeight = if (isInteracting) thumbLineHeightWhenInteracting else (thumbRadius * 2)
    val animatedThumbHeight by animateDpAsState(
        targetValue = targetThumbHeight,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "ThumbHeight"
    )

    val targetThumbWidth = if (isInteracting) 4.dp else (thumbRadius * 2)
    val animatedThumbWidth by animateDpAsState(
        targetValue = targetThumbWidth,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "ThumbWidth"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .testTag("wavy_music_slider")
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isInteracting = true
                        val newFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        touchXFraction = newFraction
                        onValueChange(newFraction)
                        tryAwaitRelease()
                        isInteracting = false
                        onValueChangeFinished?.invoke()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isInteracting = true
                        val newFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        touchXFraction = newFraction
                        onValueChange(newFraction)
                    },
                    onDragEnd = {
                        isInteracting = false
                        onValueChangeFinished?.invoke()
                    },
                    onDragCancel = {
                        isInteracting = false
                        onValueChangeFinished?.invoke()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val newFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        touchXFraction = newFraction
                        onValueChange(newFraction)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val thumbX = width * touchXFraction.coerceIn(0f, 1f)

            val trackHeightPx = with(density) { trackHeight.toPx() }
            val waveLengthPx = with(density) { waveLength.toPx() }
            val amplitudePx = with(density) { animatedAmplitude.toPx() }
            val thumbWidthPx = with(density) { animatedThumbWidth.toPx() }
            val thumbHeightPx = with(density) { animatedThumbHeight.toPx() }

            // 1. Draw Inactive Track (Right of Thumb)
            if (!hideInactiveTrackPortion) {
                if (thumbX < width) {
                    drawLine(
                        color = inactiveTrackColor,
                        start = Offset(thumbX, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = trackHeightPx,
                        cap = StrokeCap.Round
                    )
                }
            } else {
                // Subtle flat track line for inactive portion
                if (thumbX < width) {
                    drawLine(
                        color = inactiveTrackColor.copy(alpha = 0.25f),
                        start = Offset(thumbX, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = trackHeightPx,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 2. Draw Active Track (Left of Thumb)
            if (thumbX > 0f) {
                if (amplitudePx > 0.1f) {
                    val activePath = Path().apply {
                        moveTo(0f, centerY)
                        val step = 3f
                        var x = 0f
                        while (x <= thumbX) {
                            val radians = ((x / waveLengthPx) * 2 * PI + phase).toFloat()
                            val y = centerY + sin(radians) * amplitudePx
                            lineTo(x, y)
                            x += step
                        }
                        lineTo(thumbX, centerY)
                    }
                    drawPath(
                        path = activePath,
                        color = activeTrackColor,
                        style = Stroke(
                            width = trackHeightPx,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else {
                    drawLine(
                        color = activeTrackColor,
                        start = Offset(0f, centerY),
                        end = Offset(thumbX, centerY),
                        strokeWidth = trackHeightPx,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Draw Thumb
            val cornerRadius = CornerRadius(thumbWidthPx / 2f, thumbWidthPx / 2f)
            val thumbLeft = thumbX - (thumbWidthPx / 2f)
            val thumbTop = centerY - (thumbHeightPx / 2f)

            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(thumbLeft, thumbTop),
                size = Size(thumbWidthPx, thumbHeightPx),
                cornerRadius = cornerRadius
            )
        }
    }
}
