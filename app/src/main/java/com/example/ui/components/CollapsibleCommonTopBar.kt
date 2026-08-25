package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class CollapsibleHeaderState(
    val headerHeightRange: Pair<Dp, Dp>,
    val animatable: Animatable<Float, AnimationVector1D>,
    private val coroutineScope: CoroutineScope,
    private val density: Density
) {
    val collapseFraction: Float get() = animatable.value
    val currentHeaderHeight: Dp get() = lerp(headerHeightRange.first, headerHeightRange.second, collapseFraction)

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.y
            val headerHeightPx = with(density) { (headerHeightRange.first - headerHeightRange.second).toPx() }
            if (headerHeightPx <= 0f) return Offset.Zero

            val current = animatable.value
            val target = (current - delta / headerHeightPx).coerceIn(0f, 1f)
            if (target != current) {
                coroutineScope.launch {
                    animatable.snapTo(target)
                }
            }
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val target = if (animatable.value > 0.5f) 1f else 0f
            animatable.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            return super.onPostFling(consumed, available)
        }
    }
}

@Composable
fun rememberCollapsibleHeaderState(
    headerHeightRange: Pair<Dp, Dp> = 180.dp to 56.dp,
    initialFraction: Float = 0f
): CollapsibleHeaderState {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val animatable = remember { Animatable(initialFraction) }

    return remember(headerHeightRange, density, coroutineScope) {
        CollapsibleHeaderState(
            headerHeightRange = headerHeightRange,
            animatable = animatable,
            coroutineScope = coroutineScope,
            density = density
        )
    }
}

@Composable
fun CollapsibleCommonTopBar(
    title: String,
    collapseFraction: Float,
    headerHeight: Dp,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showBackButton: Boolean = true,
    containerColor: Color? = null,
    containerHeightRange: Pair<Dp, Dp> = 88.dp to 56.dp,
    titleStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    titleScaleRange: Pair<Float, Float> = 1.2f to 0.8f,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fadeSubtitleOnCollapse: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val bg = containerColor ?: MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
    val titleScale = lerp(titleScaleRange.first, titleScaleRange.second, collapseFraction)
    val titleBias = lerp(-0.8f, 0f, collapseFraction)
    val titleHorizontalPadding = if (showBackButton) {
        lerp(20.dp, 68.dp, collapseFraction)
    } else {
        20.dp
    }
    val subtitleAlpha = if (fadeSubtitleOnCollapse && subtitle != null) lerp(1f, 0f, collapseFraction) else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
            .statusBarsPadding()
            .background(bg)
    ) {
        // Back Button
        if (showBackButton) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp)
                    .size(48.dp)
                    .align(Alignment.TopStart)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .testTag("collapsible_top_bar_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Title and Subtitle Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = titleHorizontalPadding,
                    end = if (showBackButton) 68.dp else 20.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            contentAlignment = BiasAlignment(horizontalBias = -1f, verticalBias = titleBias)
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = titleStyle.copy(fontWeight = FontWeight.Bold),
                    color = contentColor,
                    maxLines = if (title.length > 13) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer {
                        scaleX = titleScale
                        scaleY = titleScale
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    }
                )
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer {
                            alpha = subtitleAlpha
                        }
                    )
                }
            }
        }

        // Actions Row
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}
