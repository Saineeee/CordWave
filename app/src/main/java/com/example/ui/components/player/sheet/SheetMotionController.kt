package com.example.ui.components.player.sheet

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Logical resting states of the player sheet.
 */
internal enum class PlayerSheetState {
    /** Full screen player. */
    EXPANDED,

    /** Mini player docked above the bottom navigation bar. */
    COLLAPSED
}

/**
 * Single owner of the player sheet's motion. Drives the [translationY] and
 * [expansionFraction] animatables in parallel under a [MutatorMutex] so that
 * drags, programmatic animations and layout syncs never fight each other.
 *
 * [expansionFraction] is 0f when fully collapsed (mini player) and 1f when
 * fully expanded (full screen player). [translationY] is the sheet's top
 * offset in pixels: [expandedY] (0) when expanded and [collapsedY] (screen
 * height minus mini player height and bottom bar) when collapsed.
 *
 * @param translationY animatable holding the sheet's vertical translation.
 * @param expansionFraction animatable holding the 0..1 expansion progress.
 * @param expandedY the sheet's translationY target when expanded (0px).
 * @param defaultAnimationSpec spec used when [animateTo] gets no explicit one.
 */
internal class SheetMotionController(
    private val translationY: Animatable<Float, AnimationVector1D>,
    private val expansionFraction: Animatable<Float, AnimationVector1D>,
    private val expandedY: Float,
    private val defaultAnimationSpec: AnimationSpec<Float> = spring()
) {
    private val mutex = MutatorMutex()

    /** Current expansion progress in 0..1. */
    val currentFraction: Float get() = expansionFraction.value

    /** Current vertical translation of the sheet in pixels. */
    val currentTranslationY: Float get() = translationY.value

    /** Whether either channel still has an animation in flight. */
    val isRunning: Boolean get() = translationY.isRunning || expansionFraction.isRunning

    /**
     * Cancels any in-flight animation on both channels and releases them for
     * the next mutation (typically the drag gesture taking over).
     */
    suspend fun stop() = mutex.mutate(MutatePriority.PreventUserInput) {
        translationY.stop()
        expansionFraction.stop()
    }

    private fun targetsFor(
        targetExpanded: Boolean,
        canExpand: Boolean,
        collapsedY: Float
    ): Pair<Float, Float> {
        return if (targetExpanded && canExpand) {
            expandedY to 1f
        } else {
            collapsedY to 0f
        }
    }

    /**
     * Animates both channels in parallel towards the target sheet state.
     *
     * @param targetExpanded whether the sheet should end up expanded.
     * @param canExpand whether expanding is currently allowed; when false the
     * sheet always animates to the collapsed position.
     * @param collapsedY the collapsed translationY target in pixels.
     * @param animationSpec spec applied to both channels.
     * @param initialVelocity velocity carried over from a drag, px/s.
     */
    suspend fun animateTo(
        targetExpanded: Boolean,
        canExpand: Boolean,
        collapsedY: Float,
        animationSpec: AnimationSpec<Float> = defaultAnimationSpec,
        initialVelocity: Float = 0f
    ) = mutex.mutate {
        val (targetY, targetFraction) = targetsFor(targetExpanded, canExpand, collapsedY)
        coroutineScope {
            launch {
                translationY.animateTo(
                    targetValue = targetY,
                    animationSpec = animationSpec,
                    initialVelocity = initialVelocity
                )
            }
            launch {
                expansionFraction.animateTo(
                    targetValue = targetFraction,
                    animationSpec = animationSpec,
                    initialVelocity = initialVelocity
                )
            }
        }
    }

    /**
     * Jumps both channels to the target sheet state without animating.
     */
    suspend fun snapTo(
        targetExpanded: Boolean,
        canExpand: Boolean,
        collapsedY: Float
    ) = mutex.mutate {
        val (targetY, targetFraction) = targetsFor(targetExpanded, canExpand, collapsedY)
        coroutineScope {
            launch { translationY.snapTo(targetY) }
            launch { expansionFraction.snapTo(targetFraction) }
        }
    }

    /**
     * Re-derives [translationY] from the current expansion fraction, keeping
     * the visual progress stable when the screen geometry changes (rotation,
     * first bottom-bar measurement, split screen, ...).
     */
    suspend fun syncToExpansion(collapsedY: Float) = mutex.mutate {
        val fraction = expansionFraction.value
        val travel = (collapsedY - expandedY).coerceAtLeast(0f)
        translationY.snapTo(collapsedY - fraction * travel)
    }
}
