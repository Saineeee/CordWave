package com.example.ui.components.player.sheet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Gesture handler that dismisses the mini player with a horizontal swipe.
 *
 * The gesture has two distinct phases before the finger is lifted:
 *
 *  - [DragPhase.TENSION]: while the accumulated horizontal drag stays below
 *    [snapThresholdPx] (100dp) the visual offset moves with heavy resistance
 *    and is capped at [maxTensionOffsetPx] (30dp). This communicates that the
 *    swipe is not yet "armed" for dismissal.
 *  - [DragPhase.SNAPPING]: crossing the threshold triggers a LongPress haptic
 *    and springs the offset (damping 0.8, StiffnessLow) out to the accumulated
 *    drag, releasing the tension.
 *  - [DragPhase.FREE_DRAG]: afterwards the offset follows the finger directly
 *    through a stiff, non-bouncy spring.
 *
 * On release the mini player is dismissed when the accumulated drag exceeds
 * 40% of the screen width: the offset animates off screen over 200ms
 * (FastOutSlowInEasing), [onDismissPlaylistAndShowUndo] is invoked and the
 * offset snaps back to zero for the next composition. Anything short of that
 * springs back to the resting position (MediumBouncy, StiffnessMedium).
 */
internal class MiniPlayerDismissGestureHandler(
    private val scope: CoroutineScope,
    private val offsetAnimatable: Animatable<Float, AnimationVector1D>,
    density: Density,
    private val screenWidthPx: () -> Float,
    private val onHapticFeedback: () -> Unit,
    private val onDismissPlaylistAndShowUndo: () -> Unit
) {
    private enum class DragPhase {
        IDLE,
        TENSION,
        SNAPPING,
        FREE_DRAG
    }

    private var phase = DragPhase.IDLE
    private var accumulatedDragX = 0f
    private var dragJob: Job? = null
    private val maxTensionOffsetPx = 30f * density.density
    private val snapThresholdPx = 100f * density.density

    /** Called when the horizontal drag begins. */
    fun onDragStart() {
        phase = DragPhase.TENSION
        accumulatedDragX = 0f
        dragJob?.cancel()
    }

    /**
     * Called for every horizontal drag increment.
     */
    fun onHorizontalDrag(dragAmount: Float) {
        accumulatedDragX += dragAmount
        when (phase) {
            DragPhase.IDLE -> Unit
            DragPhase.TENSION -> {
                if (abs(accumulatedDragX) >= snapThresholdPx) {
                    // Armed: fire the haptic and spring out to the real drag
                    // position, then let the offset follow the finger freely.
                    phase = DragPhase.SNAPPING
                    onHapticFeedback()
                    dragJob?.cancel()
                    dragJob = scope.launch {
                        offsetAnimatable.animateTo(
                            targetValue = accumulatedDragX,
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        phase = DragPhase.FREE_DRAG
                    }
                } else {
                    // Resisted movement: at most maxTensionOffsetPx of travel.
                    val resistance = maxTensionOffsetPx / snapThresholdPx
                    val target = accumulatedDragX * resistance
                    dragJob?.cancel()
                    dragJob = scope.launch {
                        offsetAnimatable.snapTo(target)
                    }
                }
            }
            DragPhase.SNAPPING -> Unit // let the release spring finish
            DragPhase.FREE_DRAG -> {
                dragJob?.cancel()
                dragJob = scope.launch {
                    offsetAnimatable.animateTo(
                        targetValue = accumulatedDragX,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )
                }
            }
        }
    }

    /** Called when the horizontal drag ends (finger lifted). */
    fun onDragEnd() {
        val willDismiss = abs(accumulatedDragX) > screenWidthPx() * 0.4f
        dragJob?.cancel()
        if (willDismiss) {
            dragJob = scope.launch {
                val direction = if (accumulatedDragX >= 0f) 1f else -1f
                offsetAnimatable.animateTo(
                    targetValue = direction * screenWidthPx(),
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
                onDismissPlaylistAndShowUndo()
                offsetAnimatable.snapTo(0f)
            }
        } else {
            dragJob = scope.launch {
                offsetAnimatable.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
        phase = DragPhase.IDLE
        accumulatedDragX = 0f
    }

    /** Called when the horizontal drag is cancelled by the system. */
    fun onDragCancel() {
        dragJob?.cancel()
        dragJob = scope.launch {
            offsetAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        phase = DragPhase.IDLE
        accumulatedDragX = 0f
    }
}

/**
 * Attaches the mini player dismiss gesture to [this] modifier. All pointer
 * changes consumed by the drag are marked as consumed.
 */
internal fun Modifier.miniPlayerDismissHorizontalGesture(
    enabled: Boolean,
    handler: MiniPlayerDismissGestureHandler
): Modifier = this.pointerInput(handler, enabled) {
    if (!enabled) return@pointerInput
    detectHorizontalDragGestures(
        onDragStart = { handler.onDragStart() },
        onHorizontalDrag = { change, dragAmount ->
            change.consume()
            handler.onHorizontalDrag(dragAmount)
        },
        onDragEnd = { handler.onDragEnd() },
        onDragCancel = { handler.onDragCancel() }
    )
}
