package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.settings.CollapsibleSettingsTopBar
import com.example.ui.components.settings.SettingsSubsection
import com.example.ui.components.settings.rememberCollapsibleTopBarConnection

@Composable
fun PaletteStyleScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minHeightDp = 64f
    val maxHeightDp = 180f
    val topBarHeight = remember { Animatable(maxHeightDp) }
    val nestedScrollConnection = rememberCollapsibleTopBarConnection(topBarHeight, minHeightDp, maxHeightDp)

    var selectedStyle by remember { mutableStateOf("VIBRANT") }

    val styles = listOf(
        Triple("VIBRANT", "Vibrant & Dynamic", "Extracts the richest, most saturated colors from artwork"),
        Triple("MUTED", "Muted & Gentle", "Subtle pastel undertones with gentle contrast"),
        Triple("DOMINANT", "Dominant Hue", "Focuses on the primary color surface"),
        Triple("HIGH_CONTRAST", "High Contrast Accent", "Maximizes text and control visibility")
    )

    Scaffold(
        topBar = {
            CollapsibleSettingsTopBar(
                title = "Palette Style",
                onBackClick = onBack,
                showBackButton = true,
                topBarHeight = topBarHeight,
                minHeightDp = minHeightDp,
                maxHeightDp = maxHeightDp
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                SettingsSubsection(title = "ALBUM ART COLOR EXTRACTION") {
                    styles.forEach { (key, title, subtitle) ->
                        val isSelected = selectedStyle == key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStyle = key }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
