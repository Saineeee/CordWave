package com.example.ui.components.scrollbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Internal snapshot of everything the scrollbar needs to know about the
 * attached lazy layout. Values are estimates built from the visible window
 * plus the [AxisObservationTracker] representative-size bookkeeping.
 *
 * @param totalItems total number of items in the layout (item, not row, units).
 * @param totalContentPx estimated full content extent along the main axis.
 * @param viewportPx main-axis size of the viewport.
 * @param scrolledPx current scroll position estimate.
 * @param fraction current scroll position in [0, 1].
 * @param stridePx estimated distance between consecutive scroll steps
 * (item stride for lists, row stride for grids).
 * @param stepItems number of items advanced by a single stride
 * (1 for lists, column count for grids).
 * @param firstVisibleIndex index of the top-most visible item.
 */
internal data class ScrollGeometry(
    val totalItems: Int,
    val totalContentPx: Float,
    val viewportPx: Float,
    val scrolledPx: Float,
    val fraction: Float,
    val stridePx: Float,
    val stepItems: Int,
    val firstVisibleIndex: Int
)

private fun computeListGeometry(
    state: LazyListState,
    tracker: AxisObservationTracker
): ScrollGeometry? {
    val layout = state.layoutInfo
    val totalItems = layout.totalItemsCount
    if (totalItems <= 0) return null
    val visible = layout.visibleItemsInfo
    if (visible.isEmpty()) return null
    val viewport = (layout.viewportEndOffset - layout.viewportStartOffset)
        .toFloat()
        .coerceAtLeast(1f)

    // Derive the item spacing from the first adjacent pair of visible items.
    var spacing = 0
    if (visible.size >= 2) {
        val a = visible[0]
        val b = visible[1]
        if (b.index == a.index + 1) {
            spacing = (b.offset - a.offset - a.size).coerceAtLeast(0)
        }
    }
    tracker.resetIfNeeded(totalItems, spacing)
    visible.forEach { item ->
        tracker.observeItemSize(item.index, item.size.toFloat())
        tracker.observeStride(item.index, item.size + spacing.toFloat())
    }
    tracker.observeRepresentativeSample(
        strideSamplePx = visible[0].size + spacing.toFloat(),
        itemSizeSamplePx = visible[0].size.toFloat()
    )

    val stride = tracker.representativeStridePx(visible[0].size + spacing.toFloat())
        .coerceAtLeast(1f)
    val itemSize = tracker.representativeItemSizePx(visible[0].size.toFloat())
    val totalContent = tracker.distanceBeforeIndex(totalItems, stride) + itemSize
    val first = visible.minByOrNull { it.offset } ?: return null
    val maxScroll = (totalContent - viewport).coerceAtLeast(1f)
    val scrolled = (tracker.distanceBeforeIndex(first.index, stride) - first.offset)
        .coerceIn(0f, maxScroll)

    return ScrollGeometry(
        totalItems = totalItems,
        totalContentPx = totalContent,
        viewportPx = viewport,
        scrolledPx = scrolled,
        fraction = (scrolled / maxScroll).coerceIn(0f, 1f),
        stridePx = stride,
        stepItems = 1,
        firstVisibleIndex = first.index
    )
}

private fun computeGridGeometry(
    state: LazyGridState,
    tracker: AxisObservationTracker
): ScrollGeometry? {
    val layout = state.layoutInfo
    val totalItems = layout.totalItemsCount
    if (totalItems <= 0) return null
    val visible = layout.visibleItemsInfo
    if (visible.isEmpty()) return null
    val viewport = (layout.viewportEndOffset - layout.viewportStartOffset)
        .toFloat()
        .coerceAtLeast(1f)

    // Group visible items into rows by their main-axis offset.
    data class Row(val y: Int, val height: Int, val firstIndex: Int)
    val rows = visible
        .groupBy { it.offset.y }
        .entries
        .sortedBy { it.key }
        .map { (y, items) -> Row(y, items.maxOf { it.size.height }, items.minOf { it.index }) }
    val columns = visible.groupBy { it.offset.y }.values.maxOf { it.size }
        .coerceAtLeast(1)

    val firstRowHeight = rows.first().height.toFloat()
    val rowStride = if (rows.size >= 2) {
        (rows[1].y - rows[0].y).toFloat()
    } else {
        firstRowHeight
    }.coerceAtLeast(1f)
    val rowSpacing = (rowStride - firstRowHeight).toInt().coerceAtLeast(0)

    tracker.resetIfNeeded(totalItems, rowSpacing)
    rows.forEach { row ->
        val rowIndex = row.firstIndex / columns
        tracker.observeItemSize(rowIndex, row.height.toFloat())
        tracker.observeStride(rowIndex, rowStride)
    }
    tracker.observeRepresentativeSample(
        strideSamplePx = rowStride,
        itemSizeSamplePx = firstRowHeight
    )

    val stride = tracker.representativeStridePx(rowStride).coerceAtLeast(1f)
    val rowHeight = tracker.representativeItemSizePx(firstRowHeight)
    val totalRows = ceil(totalItems / columns.toFloat()).toInt().coerceAtLeast(1)
    val totalContent = tracker.distanceBeforeIndex(totalRows, stride) + rowHeight
    val first = visible.minByOrNull { it.offset.y } ?: return null
    val firstRowIndex = first.index / columns
    val maxScroll = (totalContent - viewport).coerceAtLeast(1f)
    val scrolled = (tracker.distanceBeforeIndex(firstRowIndex, stride) - first.offset.y)
        .coerceIn(0f, maxScroll)

    return ScrollGeometry(
        totalItems = totalItems,
        totalContentPx = totalContent,
        viewportPx = viewport,
        scrolledPx = scrolled,
        fraction = (scrolled / maxScroll).coerceIn(0f, 1f),
        stridePx = stride,
        stepItems = columns,
        firstVisibleIndex = first.index
    )
}

/**
 * A Canvas-drawn expressive scrollbar for lazy lists and grids.
 *
 * The track is a thin rounded line drawn with [androidx.compose.ui.graphics.drawscope.DrawLine]
 * in `secondaryContainer`; the thumb is a path in `primary` whose left edge is a
 * half circle and whose right edge uses [indicatorRightCornerRadius] corners.
 * While interacting, the thumb animates from [thickness] to
 * [indicatorExpandedWidth] + [indicatorExpandedWidthBoost] wide (tween 200ms,
 * FastOutSlowInEasing) and an `UnfoldMore` icon fades and scales in.
 *
 * Dragging the thumb scrolls the attached layout. Large jumps are animated over
 * 70ms (FastOutSlowInEasing) when the stride is at least 16dp and the delta is
 * at least 10dp, otherwise the position snaps.
 *
 * When [dragLabelProvider] is supplied and the user is interacting, a circular
 * label is shown to the left of the thumb (gap [dragLabelGap]) with the
 * provider-supplied text for the current first visible item, animating in with
 * alpha 0 -> 1, scale 0.82 -> 1.0 and a slide of 8dp -> 0 (tween 180ms,
 * FastOutSlowInEasing).
 *
 * The scrollbar is only composed while the attached layout can actually scroll
 * (`canScrollForward || canScrollBackward`).
 *
 * Typical usage:
 * ```
 * Box(modifier = Modifier.fillMaxSize()) {
 *     LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) { ... }
 *     ExpressiveScrollBar(
 *         listState = listState,
 *         modifier = Modifier.align(Alignment.CenterEnd),
 *         dragLabelProvider = { index ->
 *             songs.getOrNull(index)?.title?.firstOrNull()?.uppercase()
 *         }
 *     )
 * }
 * ```
 */
@Composable
fun ExpressiveScrollBar(
    modifier: Modifier = Modifier,
    listState: LazyListState? = null,
    gridState: LazyGridState? = null,
    minHeight: Dp = 48.dp,
    thickness: Dp = 8.dp,
    indicatorExpandedWidth: Dp = 24.dp,
    indicatorExpandedWidthBoost: Dp = 4.dp,
    indicatorRightCornerRadius: Dp = 6.dp,
    paddingEnd: Dp = 4.dp,
    trackGap: Dp = 8.dp,
    dragLabelProvider: ((Int) -> String?)? = null,
    dragLabelSize: Dp = 40.dp,
    dragLabelGap: Dp = 10.dp
) {
    require((listState != null) != (gridState != null)) {
        "ExpressiveScrollBar requires exactly one of listState or gridState"
    }

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // --- Geometry converted to pixels ------------------------------------
    val thicknessPx = with(density) { thickness.toPx() }
    val expandedWidthPx = with(density) { (indicatorExpandedWidth + indicatorExpandedWidthBoost).toPx() }
    val minHeightPx = with(density) { minHeight.toPx() }
    val trackGapPx = with(density) { trackGap.toPx() }
    val paddingEndPx = with(density) { paddingEnd.toPx() }
    val rightCornerRadiusPx = with(density) { indicatorRightCornerRadius.toPx() }
    val dragLabelSizePx = with(density) { dragLabelSize.toPx() }
    val dragLabelGapPx = with(density) { dragLabelGap.toPx() }
    val labelSlideRangePx = with(density) { 8.dp.toPx() }
    val smoothJumpStepThresholdPx = with(density) { 16.dp.toPx() }
    val smoothJumpDeltaThresholdPx = with(density) { 10.dp.toPx() }
    val iconSizePx = with(density) { 16.dp.toPx() }

    val hitAreaWidth = paddingEnd + indicatorExpandedWidth + indicatorExpandedWidthBoost

    val tracker = remember { AxisObservationTracker() }

    // --- Derived geometry, recomputed only when the layout info changes ---
    val geometry: State<ScrollGeometry?> = remember(listState, gridState) {
        derivedStateOf {
            if (listState != null) computeListGeometry(listState, tracker)
            else computeGridGeometry(gridState!!, tracker)
        }
    }

    val canScroll = geometry.value?.let { it.totalContentPx > it.viewportPx + 0.5f } == true

    // Nothing to draw when the layout cannot scroll in either direction.
    if (!canScroll) return

    val trackColor = MaterialTheme.colorScheme.secondaryContainer
    val indicatorColor = MaterialTheme.colorScheme.primary
    val onIndicatorColor = MaterialTheme.colorScheme.onPrimary

    // --- Interaction state ------------------------------------------------
    var isInteracting by remember { mutableStateOf(false) }
    var dragTravel by remember { mutableStateOf(0f) } // thumb top within travel range [0, maxTravel]
    var grabOffset by remember { mutableStateOf(0f) }
    var scrollJob by remember { mutableStateOf<Job?>(null) }

    val thumbWidth = remember { Animatable(thicknessPx) }
    val jumpFraction = remember { Animatable(0f) }
    val labelAlpha = remember { Animatable(0f) }
    val labelScale = remember { Animatable(0.82f) }
    val labelSlide = remember { Animatable(labelSlideRangePx) }

    LaunchedEffect(isInteracting) {
        thumbWidth.animateTo(
            targetValue = if (isInteracting) expandedWidthPx else thicknessPx,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        )
    }

    if (dragLabelProvider != null) {
        LaunchedEffect(isInteracting) {
            val spec = tween<Float>(durationMillis = 180, easing = FastOutSlowInEasing)
            if (isInteracting) {
                launch { labelAlpha.animateTo(1f, spec) }
                launch { labelScale.animateTo(1f, spec) }
                labelSlide.animateTo(0f, spec)
            } else {
                launch { labelAlpha.animateTo(0f, spec) }
                launch { labelScale.animateTo(0.82f, spec) }
                labelSlide.animateTo(labelSlideRangePx, spec)
            }
        }
    }

    // --- Helpers ------------------------------------------------------------

    /**
     * Converts a scroll fraction into the target item index and remainder for
     * the attached lazy layout and scrolls there (snap semantics).
     */
    suspend fun scrollListToFraction(fraction: Float) {
        val geo = geometry.value ?: return
        val maxScroll = (geo.totalContentPx - geo.viewportPx).coerceAtLeast(1f)
        val targetScrolled = fraction.coerceIn(0f, 1f) * maxScroll
        val stepIndex = floor(targetScrolled / geo.stridePx).toInt()
        val remainder = targetScrolled - stepIndex * geo.stridePx
        val itemIndex = (stepIndex * geo.stepItems).coerceIn(0, (geo.totalItems - 1).coerceAtLeast(0))
        if (listState != null) {
            listState.scrollToItem(itemIndex, remainder.roundToInt())
        } else {
            gridState?.scrollToItem(itemIndex, remainder.roundToInt())
        }
    }

    fun snapListToFraction(fraction: Float) {
        scrollJob?.cancel()
        scrollJob = scope.launch { scrollListToFraction(fraction) }
    }

    /**
     * Smooth jump: animates the layout scroll over 70ms so that large drag
     * deltas glide instead of teleporting.
     */
    fun animateListToFraction(target: Float, from: Float) {
        scrollJob?.cancel()
        scrollJob = scope.launch {
            jumpFraction.snapTo(from)
            // Animatable.animateTo's per-frame block is non-suspend, so we
            // launch a parallel observer that collects the value via snapshotFlow
            // and forwards each distinct sample to the suspend scroll routine.
            val observer = launch {
                androidx.compose.runtime.snapshotFlow { jumpFraction.value }.collect { value ->
                    scrollListToFraction(value)
                }
            }
            jumpFraction.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing)
            )
            observer.cancel()
            // Final settle: ensure the exact target is reached even if the
            // snapshot observer missed the very last update.
            scrollListToFraction(target)
        }
    }

    fun trackMetrics(heightPx: Float): Triple<Float, Float, Float> {
        val trackLen = (heightPx - 2 * trackGapPx).coerceAtLeast(1f)
        val geo = geometry.value
        val indicatorLen = if (geo != null) {
            (trackLen * geo.viewportPx / geo.totalContentPx).coerceIn(minHeightPx, trackLen)
        } else {
            trackLen
        }
        val maxTravel = (trackLen - indicatorLen).coerceAtLeast(1f)
        return Triple(trackLen, indicatorLen, maxTravel)
    }

    // --- Root layout ---------------------------------------------------------
    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(hitAreaWidth)
    ) {
        val boxHeightPx = constraints.maxHeight.toFloat()

        // Canvas-drawn track + thumb. All state reads happen inside the draw
        // lambda so scrolling only invalidates the draw phase.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(listState, gridState) {
                    detectDragGestures(
                        onDragStart = { position ->
                            val (_, indicatorLen, maxTravel) = trackMetrics(size.height.toFloat())
                            val geo = geometry.value ?: return@detectDragGestures
                            isInteracting = true
                            val currentTravel = geo.fraction * maxTravel
                            grabOffset = position.y - trackGapPx - currentTravel
                            if (grabOffset < 0f || grabOffset > indicatorLen) {
                                // Touch landed outside the thumb: centre the thumb
                                // under the finger and jump there immediately.
                                grabOffset = indicatorLen / 2f
                                val target =
                                    (position.y - trackGapPx - grabOffset).coerceIn(0f, maxTravel)
                                dragTravel = target
                                snapListToFraction(target / maxTravel)
                            } else {
                                dragTravel = currentTravel
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val (_, _, maxTravel) = trackMetrics(size.height.toFloat())
                            val geo = geometry.value ?: return@detectDragGestures
                            val newTravel =
                                (dragTravel + dragAmount.y).coerceIn(0f, maxTravel)
                            val deltaPx = abs(newTravel - dragTravel)
                            dragTravel = newTravel
                            val targetFraction = newTravel / maxTravel
                            val scrolling = scrollJob?.isActive == true ||
                                    listState?.isScrollInProgress == true ||
                                    gridState?.isScrollInProgress == true
                            if (!scrolling &&
                                geo.stridePx >= smoothJumpStepThresholdPx &&
                                deltaPx >= smoothJumpDeltaThresholdPx
                            ) {
                                animateListToFraction(targetFraction, geo.fraction)
                            } else {
                                snapListToFraction(targetFraction)
                            }
                        },
                        onDragEnd = {
                            isInteracting = false
                        },
                        onDragCancel = {
                            isInteracting = false
                        }
                    )
                }
        ) {
            val geo = geometry.value ?: return@Canvas
            val (_, indicatorLen, maxTravel) = trackMetrics(size.height)
            val top = if (isInteracting) {
                trackGapPx + dragTravel
            } else {
                trackGapPx + geo.fraction * maxTravel
            }

            // Track: a single rounded line along the end edge.
            val trackCenterX = size.width - paddingEndPx - thicknessPx / 2f
            drawLine(
                color = trackColor,
                start = Offset(trackCenterX, trackGapPx),
                end = Offset(trackCenterX, size.height - trackGapPx),
                strokeWidth = thicknessPx,
                cap = StrokeCap.Round
            )

            // Thumb: half circle on the left, rounded corners on the right.
            val thumbW = thumbWidth.value.coerceAtLeast(thicknessPx)
            val right = size.width - paddingEndPx
            val left = right - thumbW
            val bottom = top + indicatorLen
            val halfCircle = CornerRadius(thumbW / 2f, thumbW / 2f)
            val rightCorner = CornerRadius(rightCornerRadiusPx, rightCornerRadiusPx)
            val thumbPath = Path()
            thumbPath.addRoundRect(
                RoundRect(
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                    topLeftCornerRadius = halfCircle,
                    bottomLeftCornerRadius = halfCircle,
                    topRightCornerRadius = rightCorner,
                    bottomRightCornerRadius = rightCorner
                )
            )
            drawPath(thumbPath, indicatorColor)
        }

        // UnfoldMore icon, fading and scaling in as the thumb expands.
        Icon(
            imageVector = Icons.Filled.UnfoldMore,
            contentDescription = null,
            tint = onIndicatorColor,
            modifier = Modifier
                .size(with(density) { iconSizePx.toDp() })
                .align(Alignment.TopEnd)
                .offset {
                    val geo = geometry.value ?: return@offset IntOffset.Zero
                    val (_, indicatorLen, maxTravel) = trackMetrics(boxHeightPx)
                    val top = if (isInteracting) {
                        trackGapPx + dragTravel
                    } else {
                        trackGapPx + geo.fraction * maxTravel
                    }
                    val expansion = ((thumbWidth.value - thicknessPx) /
                            (expandedWidthPx - thicknessPx).coerceAtLeast(1f))
                        .coerceIn(0f, 1f)
                    val x = -(paddingEndPx + thumbWidth.value / 2f - iconSizePx / 2f)
                    val y = top + indicatorLen / 2f - iconSizePx / 2f
                    IntOffset(x.roundToInt(), y.roundToInt())
                }
                .graphicsLayer {
                    val expansion = ((thumbWidth.value - thicknessPx) /
                            (expandedWidthPx - thicknessPx).coerceAtLeast(1f))
                        .coerceIn(0f, 1f)
                    alpha = expansion
                    scaleX = expansion
                    scaleY = expansion
                }
        )

        // Drag label: a circular Surface to the left of the thumb.
        if (dragLabelProvider != null && (isInteracting || labelAlpha.value > 0.01f)) {
            val label = dragLabelProvider(geometry.value?.firstVisibleIndex ?: 0)
            if (label != null) {
                Surface(
                    shape = CircleShape,
                    color = indicatorColor,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(dragLabelSize)
                        .align(Alignment.CenterEnd)
                        .offset {
                            val geo = geometry.value ?: return@offset IntOffset.Zero
                            val (_, indicatorLen, maxTravel) = trackMetrics(boxHeightPx)
                            val top = if (isInteracting) {
                                trackGapPx + dragTravel
                            } else {
                                trackGapPx + geo.fraction * maxTravel
                            }
                            val x = -(paddingEndPx + expandedWidthPx + dragLabelGapPx)
                            val y = top + indicatorLen / 2f - dragLabelSizePx / 2f
                            IntOffset(x.roundToInt(), y.roundToInt())
                        }
                        .graphicsLayer {
                            alpha = labelAlpha.value
                            scaleX = labelScale.value
                            scaleY = labelScale.value
                            translationX = -labelSlide.value
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            color = onIndicatorColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
