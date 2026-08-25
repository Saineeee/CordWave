package com.example.ui.components.player.sheet

import androidx.compose.animation.core.Spring
import androidx.compose.ui.util.lerp

/**
 * One frame of the sheet's vertical drag: the coerced translation and the
 * matching expansion fraction derived from it.
 */
internal data class SheetVerticalDragFrame(
    /** Coerced sheet translation in pixels. */
    val translationY: Float,
    /** Expansion progress in 0..1 matching [translationY]. */
    val expansionFraction: Float
)

/**
 * Computes the sheet frame for a raw dragged translation.
 *
 * The raw translation is coerced into the over-drag range
 * `expandedY - 0.2 * miniHeightPx .. collapsedY + 0.2 * miniHeightPx`,
 * which lets the sheet rubber-band slightly past either resting position
 * before the fraction is clamped to 0..1.
 *
 * @param rawTranslationY the unconstrained translation accumulated by the drag.
 * @param expandedY translationY of the fully expanded sheet (0px).
 * @param collapsedY translationY of the fully collapsed (mini) sheet.
 * @param miniHeightPx height of the mini player in pixels.
 */
internal fun computeSheetVerticalDragFrame(
    rawTranslationY: Float,
    expandedY: Float,
    collapsedY: Float,
    miniHeightPx: Float
): SheetVerticalDragFrame {
    val minY = expandedY - 0.2f * miniHeightPx
    val maxY = collapsedY + 0.2f * miniHeightPx
    val y = rawTranslationY.coerceIn(minY, maxY)
    val travel = (collapsedY - expandedY).coerceAtLeast(1f)
    val fraction = ((collapsedY - y) / travel).coerceIn(0f, 1f)
    return SheetVerticalDragFrame(translationY = y, expansionFraction = fraction)
}

/**
 * Resolves which resting state the sheet should animate to when a vertical
 * drag ends.
 *
 * A velocity beyond [velocityThreshold] wins first: flinging upwards expands
 * and flinging downwards collapses. Otherwise the sheet commits to the state
 * it has travelled more than [dragThresholdPx] away from, and when neither
 * threshold is met it returns to [currentSheetState].
 *
 * @param currentSheetState the state the sheet was resting in when the drag began.
 * @param currentVelocity vertical velocity in px/s (positive downwards).
 * @param currentTranslationY the sheet's current translation.
 * @param expandedY translationY of the fully expanded sheet (0px).
 * @param collapsedY translationY of the fully collapsed (mini) sheet.
 * @param dragThresholdPx minimum travel from the resting position to commit,
 * typically 5dp in pixels.
 * @param velocityThreshold fling velocity threshold in px/s (150f).
 */
internal fun resolveVerticalSheetTargetState(
    currentSheetState: PlayerSheetState,
    currentVelocity: Float,
    currentTranslationY: Float,
    expandedY: Float,
    collapsedY: Float,
    dragThresholdPx: Float,
    velocityThreshold: Float
): PlayerSheetState {
    if (currentVelocity < -velocityThreshold) return PlayerSheetState.EXPANDED
    if (currentVelocity > velocityThreshold) return PlayerSheetState.COLLAPSED

    // How far the sheet currently sits above its collapsed resting position.
    val displacementFromCollapsed = collapsedY - currentTranslationY
    val displacementFromExpanded = currentTranslationY - expandedY

    return when (currentSheetState) {
        PlayerSheetState.COLLAPSED ->
            if (displacementFromCollapsed > dragThresholdPx) {
                PlayerSheetState.EXPANDED
            } else {
                PlayerSheetState.COLLAPSED
            }
        PlayerSheetState.EXPANDED ->
            if (displacementFromExpanded > dragThresholdPx) {
                PlayerSheetState.COLLAPSED
            } else {
                PlayerSheetState.EXPANDED
            }
    }
}

/**
 * Damping used by the collapse spring. The more expanded the sheet was when
 * the collapse started, the bouncier the settle: fully collapsed drags settle
 * with no bounce, drags starting near the top settle with a low bounce.
 */
internal fun collapseSpringDampingForFraction(currentFraction: Float): Float =
    lerp(
        start = Spring.DampingRatioNoBouncy,
        stop = Spring.DampingRatioLowBouncy,
        fraction = currentFraction.coerceIn(0f, 1f)
    )

/**
 * Initial vertical squash applied to [visualOvershootScaleY] when a collapse
 * begins: 1f (no squash) from the mini position, 0.97f (3% squash) when
 * collapsing from fully expanded.
 */
internal fun collapseInitialSquashForFraction(currentFraction: Float): Float =
    lerp(
        start = 1.0f,
        stop = 0.97f,
        fraction = currentFraction.coerceIn(0f, 1f)
    )
