package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp

@Composable
fun ExpressiveTopBarContent(
    title: String,
    collapseFraction: Float,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    collapsedTitleStartPadding: Dp = 56.dp,
    expandedTitleStartPadding: Dp = 16.dp,
    containerHeightRange: Pair<Dp, Dp> = 88.dp to 56.dp,
    titleStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    titleScaleRange: Pair<Float, Float> = 1.2f to 0.8f,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fadeSubtitleOnCollapse: Boolean = true,
    maxLines: Int = 2
) {
    val isLongTitle = remember(title) {
        title.length > 13
    }

    val titleScale = lerp(titleScaleRange.first, titleScaleRange.second, collapseFraction)
    val startPadding = lerp(expandedTitleStartPadding, collapsedTitleStartPadding, collapseFraction)
    val verticalBias = lerp(-0.8f, 0f, collapseFraction)
    val subtitleAlpha = if (fadeSubtitleOnCollapse && subtitle != null) lerp(1f, 0f, collapseFraction) else 1f

    val effectiveMaxLines = if (isLongTitle) 1 else maxLines
    val effectiveStyle = if (isLongTitle) {
        titleStyle.copy(fontSize = (titleStyle.fontSize.value * 0.9f).sp, fontWeight = FontWeight.Bold)
    } else {
        titleStyle.copy(fontWeight = FontWeight.Bold)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(start = startPadding, end = 16.dp, top = 8.dp, bottom = 8.dp),
        contentAlignment = BiasAlignment(horizontalBias = -1f, verticalBias = verticalBias)
    ) {
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = effectiveStyle,
                color = contentColor,
                maxLines = effectiveMaxLines,
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
}
