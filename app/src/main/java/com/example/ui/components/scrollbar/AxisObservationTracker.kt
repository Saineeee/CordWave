package com.example.ui.components.scrollbar

/**
 * Tracks observed item sizes and strides along the main axis of a lazy layout
 * so that the expressive scrollbar can estimate the total content extent and
 * the per-step scroll distance without needing to fully compose every item.
 *
 * Observation keys are stable per logical row: for [androidx.compose.foundation.lazy.LazyListState]
 * the key is the item index, for
 * [androidx.compose.foundation.lazy.grid.LazyGridState] the key is the row index.
 * This lets the median-based representative calculations stay meaningful for
 * both list and grid layouts.
 */
internal class AxisObservationTracker {
    private var observedItemSizes = mutableMapOf<Int, Float>()
    private var observedStrides = mutableMapOf<Int, Float>()
    private var representativeStridePx: Float? = null
    private var representativeItemSizePx: Float? = null
    private var lastTotalItemsCount: Int = -1
    private var lastSpacingPx: Int = -1

    /**
     * Clears all accumulated observations when the layout identity has changed
     * (different total item count or different item spacing).
     */
    fun resetIfNeeded(totalItemsCount: Int, spacingPx: Int) {
        if (totalItemsCount != lastTotalItemsCount || spacingPx != lastSpacingPx) {
            observedItemSizes.clear()
            observedStrides.clear()
            representativeStridePx = null
            representativeItemSizePx = null
            lastTotalItemsCount = totalItemsCount
            lastSpacingPx = spacingPx
        }
    }

    /**
     * Records the main-axis size of the item at [index] the first time it is seen.
     */
    fun observeItemSize(index: Int, sizePx: Float) {
        if (!observedItemSizes.containsKey(index)) {
            observedItemSizes[index] = sizePx
        }
    }

    /**
     * Records the main-axis stride (item size + spacing) of the item at [index]
     * the first time it is seen.
     */
    fun observeStride(index: Int, stridePx: Float) {
        if (!observedStrides.containsKey(index)) {
            observedStrides[index] = stridePx
        }
    }

    /**
     * Records a representative sample taken from the first visible element,
     * used as a bootstrap estimate before enough items have been observed.
     */
    fun observeRepresentativeSample(strideSamplePx: Float, itemSizeSamplePx: Float) {
        if (representativeStridePx == null) representativeStridePx = strideSamplePx
        if (representativeItemSizePx == null) representativeItemSizePx = itemSizeSamplePx
    }

    /**
     * The best estimate of the stride between consecutive steps, preferring the
     * median of all observed strides and falling back to the bootstrap sample
     * or the caller-supplied [fallbackStridePx].
     */
    fun representativeStridePx(fallbackStridePx: Float): Float =
        if (observedStrides.isNotEmpty()) observedStrides.values.median() else representativeStridePx ?: fallbackStridePx

    /**
     * The best estimate of a single item's main-axis size, preferring the median
     * of all observed sizes and falling back to the bootstrap sample or the
     * caller-supplied [fallbackItemSizePx].
     */
    fun representativeItemSizePx(fallbackItemSizePx: Float): Float =
        if (observedItemSizes.isNotEmpty()) observedItemSizes.values.median() else representativeItemSizePx ?: fallbackItemSizePx

    /**
     * Estimated main-axis distance from the start of the layout to the start of
     * the step at [index], assuming a uniform [representativeStridePx].
     */
    fun distanceBeforeIndex(index: Int, representativeStridePx: Float): Float =
        index * representativeStridePx

    /**
     * The observed size of the item at [index] if known, otherwise the
     * representative estimate.
     */
    fun itemSizePx(index: Int, representativeItemSizePx: Float): Float =
        observedItemSizes[index] ?: representativeItemSizePx
}

/**
 * Median of a collection of floats. Returns 0f for an empty collection.
 */
internal fun Collection<Float>.median(): Float {
    if (isEmpty()) return 0f
    val sorted = sorted()
    val mid = size / 2
    return if (size % 2 == 1) sorted[mid] else (sorted[mid - 1] + sorted[mid]) / 2f
}
