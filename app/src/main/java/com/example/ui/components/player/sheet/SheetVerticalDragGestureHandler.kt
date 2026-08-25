package com.example.ui.components.player.sheet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Gesture handler that drives the player sheet's vertical drag: dragging the
 * mini player upwards expands it into the full screen player and dragging the
 * full player downwards collapses it back into the mini player.
 *
 * All sheet motion is funnelled through [sheetMotionController]; during an
 * active drag the animatables are snapped directly (with UNDISPATCHED
 * coroutines so each frame applies immediately), and on release the handler
 * resolves a target state using a 5dp drag threshold and a 150f velocity
 * threshold, then hands control back to the controller:
 *
 *  - expanding runs [onAnimateSheet] towards the expanded position and then
 *    reports [onExpandSheetState];
 *  - collapsing first squashes the sheet vertically via
 *    [visualOvershootScaleY] (initial squash from
 *    [collapseInitialSquashForFraction], spring MediumBouncy/StiffnessVeryLow)
 *    while the sheet animates down with a fraction-dependent damping spring
 *    (see [collapseSpringDampingForFraction], StiffnessLow), then reports
 *    [onCollapseSheetState].
 */
internal class SheetVerticalDragGestureHandler(
    private val scope: CoroutineScope,
    private val sheetMotionController: SheetMotionController,
    private val translationY: Animatable<Float, AnimationVector1D>,
    private val expansionFraction: Animatable<Float, AnimationVector1D>,
    private val visualOvershootScaleY: Animatable<Float, AnimationVector1D>,
    private val expandedY: () -> Float,
    private val collapsedY: () -> Float,
    private val miniHeightPx: () -> Float,
    private val dragThresholdPx: Float,
    private val velocityThreshold: Float,
    private val onAnimateSheet: suspend (targetExpanded: Boolean, animationSpec: AnimationSpec<Float>, velocity: Float) -> Unit,
    private val onExpandSheetState: () -> Unit,
    private val onCollapseSheetState: () -> Unit
) {
    private val velocityTracker = VelocityTracker()
    private var dragJob: Job? = null
    private var settleJob: Job? = null
    private var dragStartedState = PlayerSheetState.COLLAPSED

    /** Called when the vertical drag begins. */
    fun onDragStart() {
        settleJob?.cancel()
        dragJob?.cancel()
        // Take ownership of the animatables before the first drag frame.
        dragJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            sheetMotionController.stop()
        }
        velocityTracker.resetTracking()
        dragStartedState =
            if (expansionFraction.value > 0.5f) PlayerSheetState.EXPANDED else PlayerSheetState.COLLAPSED
    }

    /**
     * Called for every vertical drag increment. [position] is the pointer
     * position in the gesture handler's coordinates and [timeMillis] the
     * event time, both used for velocity tracking.
     */
    fun onVerticalDrag(dragAmount: Float, position: Offset, timeMillis: Long) {
        val frame = computeSheetVerticalDragFrame(
            rawTranslationY = translationY.value + dragAmount,
            expandedY = expandedY(),
            collapsedY = collapsedY(),
            miniHeightPx = miniHeightPx()
        )
        dragJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            translationY.snapTo(frame.translationY)
            expansionFraction.snapTo(frame.expansionFraction)
        }
        velocityTracker.addPosition(timeMillis, position)
    }

    /** Called when the vertical drag ends (finger lifted). */
    fun onDragEnd() {
        val velocity = velocityTracker.calculateVelocity().y
        val target = resolveVerticalSheetTargetState(
            currentSheetState = dragStartedState,
            currentVelocity = velocity,
            currentTranslationY = translationY.value,
            expandedY = expandedY(),
            collapsedY = collapsedY(),
            dragThresholdPx = dragThresholdPx,
            velocityThreshold = velocityThreshold
        )
        velocityTracker.resetTracking()

        settleJob = scope.launch {
            if (target == PlayerSheetState.EXPANDED) {
                onAnimateSheet(true, spring(stiffness = Spring.StiffnessMediumLow), velocity)
                onExpandSheetState()
            } else {
                val fractionAtRelease = expansionFraction.value
                // Squash: start slightly compressed and spring back to 1f
                // while the sheet travels down.
                launch {
                    visualOvershootScaleY.snapTo(collapseInitialSquashForFraction(fractionAtRelease))
                    visualOvershootScaleY.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessVeryLow
                        )
                    )
                }
                val damping = collapseSpringDampingForFraction(fractionAtRelease)
                onAnimateSheet(
                    false,
                    spring(dampingRatio = damping, stiffness = Spring.StiffnessLow),
                    velocity
                )
                onCollapseSheetState()
            }
        }
    }

    /** Called when the vertical drag is cancelled by the system. */
    fun onDragCancel() {
        velocityTracker.resetTracking()
        settleJob = scope.launch {
            onAnimateSheet(
                dragStartedState == PlayerSheetState.EXPANDED,
                spring(stiffness = Spring.StiffnessMediumLow),
                0f
            )
        }
    }
}

/**
 * Attaches the sheet vertical drag gesture to [this] modifier. All pointer
 * changes consumed by the drag are marked as consumed.
 */
internal fun Modifier.playerSheetVerticalDragGesture(
    enabled: Boolean,
    handler: SheetVerticalDragGestureHandler
): Modifier = this.pointerInput(handler, enabled) {
    if (!enabled) return@pointerInput
    detectVerticalDragGestures(
        onDragStart = { handler.onDragStart() },
        onVerticalDrag = { change: PointerInputChange, dragAmount: Float ->
            change.consume()
            handler.onVerticalDrag(
                dragAmount = dragAmount,
                position = change.position,
                timeMillis = change.uptimeMillis
            )
        },
        onDragEnd = { handler.onDragEnd() },
        onDragCancel = { handler.onDragCancel() }
    )
}
