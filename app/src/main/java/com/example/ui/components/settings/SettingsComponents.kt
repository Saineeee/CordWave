package com.example.ui.components.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.model.SettingsCategory
import kotlinx.coroutines.launch

/**
 * Returns (Background Color, Icon Tint Color) for category items matching PixelTune specs.
 */
fun getCategoryColors(category: SettingsCategory, isDark: Boolean): Pair<Color, Color> {
    return if (isDark) {
        when (category) {
            SettingsCategory.LIBRARY -> Pair(Color(0xFF004A77), Color(0xFFC2E7FF))
            SettingsCategory.APPEARANCE -> Pair(Color(0xFF7D5260), Color(0xFFFFD8E4))
            SettingsCategory.PLAYBACK -> Pair(Color(0xFF633B48), Color(0xFFFFD8EC))
            SettingsCategory.BEHAVIOR -> Pair(Color(0xFF3E4C63), Color(0xFFD7E3FF))
            SettingsCategory.BACKUP_RESTORE -> Pair(Color(0xFF3B4869), Color(0xFFD9E2FF))
            SettingsCategory.IMPORT_PLAYLIST -> Pair(Color(0xFF4A148C), Color(0xFFE1BEE7))
            SettingsCategory.ABOUT -> Pair(Color(0xFF3F474D), Color(0xFFDEE3EB))
            SettingsCategory.EQUALIZER -> Pair(Color(0xFF6E4E13), Color(0xFFFFDEAC))
            SettingsCategory.DEVICE_CAPABILITIES -> Pair(Color(0xFF004D61), Color(0xFFACEFEE))
            SettingsCategory.ACCOUNTS -> Pair(Color(0xFF324F34), Color(0xFFCBEFD0))
        }
    } else {
        when (category) {
            SettingsCategory.LIBRARY -> Pair(Color(0xFFD1E4FF), Color(0xFF001D36))
            SettingsCategory.APPEARANCE -> Pair(Color(0xFFFFD8E4), Color(0xFF31111D))
            SettingsCategory.PLAYBACK -> Pair(Color(0xFFFFD8EC), Color(0xFF311022))
            SettingsCategory.BEHAVIOR -> Pair(Color(0xFFD7E3FF), Color(0xFF001B3F))
            SettingsCategory.BACKUP_RESTORE -> Pair(Color(0xFFD9E2FF), Color(0xFF0E1C38))
            SettingsCategory.IMPORT_PLAYLIST -> Pair(Color(0xFFF3E5F5), Color(0xFF4A148C))
            SettingsCategory.ABOUT -> Pair(Color(0xFFDEE3EB), Color(0xFF171C22))
            SettingsCategory.EQUALIZER -> Pair(Color(0xFFFFDEAC), Color(0xFF281900))
            SettingsCategory.DEVICE_CAPABILITIES -> Pair(Color(0xFFACEFEE), Color(0xFF002022))
            SettingsCategory.ACCOUNTS -> Pair(Color(0xFFCBEFD0), Color(0xFF00210E))
        }
    }
}

/**
 * Large expressive category item card (88dp height) for Level 1 Hub.
 */
@Composable
fun ExpressiveCategoryItem(
    category: SettingsCategory,
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(24.dp),
    customColors: Pair<Color, Color>? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.let {
        // Simple dark luminance check
        androidx.core.graphics.ColorUtils.calculateLuminance(it.hashCode()) < 0.5
    }
    val colors = customColors ?: getCategoryColors(category, isDark)

    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .testTag("settings_category_${category.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 56dp circular icon container with category-specific background color
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.first),
                contentAlignment = Alignment.Center
            ) {
                category.icon?.let { iconVec ->
                    Icon(
                        imageVector = iconVec,
                        contentDescription = null,
                        tint = colors.second,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Large expressive navigation item card (88dp height) for standalone navigation.
 */
@Composable
fun ExpressiveNavigationItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: Pair<Color, Color>,
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(24.dp),
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.first),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.second,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Settings Group with 24dp rounded outer corners and 2dp gap between grouped items.
 */
@Composable
fun ExpressiveSettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        content = content
    )
}

/**
 * Standard settings row with leading icon, title, subtitle, and optional trailing click/chevron.
 */
@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = Icons.Default.ChevronRight,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val clickModifier = if (onClick != null && enabled) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailingContent()
        } else if (trailingIcon != null && onClick != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Settings item with a Switch on the right side.
 */
@Composable
fun SwitchSettingItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )
    }
}

/**
 * Settings item with a continuous or stepped slider and live value display.
 */
@Composable
fun SliderSettingsItem(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueText: String,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Settings item with selectable option pills / chips.
 */
@Composable
fun ThemeSelectorItem(
    label: String,
    description: String? = null,
    options: Map<String, String>,
    selectedKey: String,
    onSelectionChanged: (String) -> Unit,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (key, name) ->
                val isSelected = (key == selectedKey)
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectionChanged(key) },
                    label = {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null
                )
            }
        }
    }
}

/**
 * Special Library refresh component with sync button, progress bar, and rebuild option.
 */
@Composable
fun RefreshLibraryItem(
    isSyncing: Boolean,
    syncProgress: Float,
    statusMessage: String = "",
    onSync: () -> Unit,
    onRebuild: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Library Sync & Maintenance",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isSyncing) statusMessage else "Scan local files, update tags, or rebuild cache",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            FilledTonalButton(
                onClick = onSync,
                enabled = !isSyncing,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Syncing")
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sync")
                }
            }
        }

        if (isSyncing) {
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { syncProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onRebuild,
            enabled = !isSyncing,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Rebuild Complete Media Catalog", style = MaterialTheme.typography.labelMedium)
        }
    }
}

/**
 * Settings Subsection divider and container with title in labelLarge, SemiBold.
 */
@Composable
fun SettingsSubsection(
    title: String,
    addBottomSpace: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                content = content
            )
        }

        if (addBottomSpace) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * PixelTune Collapsible Top Bar with Animated Height, NestedScrollConnection, and Smooth Spring Dynamics.
 */
@Composable
fun CollapsibleSettingsTopBar(
    title: String,
    onBackClick: () -> Unit,
    showBackButton: Boolean = true,
    topBarHeight: Animatable<Float, *>,
    minHeightDp: Float,
    maxHeightDp: Float,
    modifier: Modifier = Modifier
) {
    val collapseFraction = ((maxHeightDp - topBarHeight.value) / (maxHeightDp - minHeightDp))
        .coerceIn(0f, 1f)

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxWidth()
            .height(topBarHeight.value.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Back Button
            if (showBackButton) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopStart)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(
                                alpha = 0.5f + (0.5f * collapseFraction)
                            ),
                            CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .testTag("settings_top_bar_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Collapsed small title (shown when collapsed)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(if (showBackButton) Alignment.CenterStart else Alignment.CenterStart)
                    .padding(start = if (showBackButton) 56.dp else 0.dp)
                    .graphicsLayer {
                        alpha = collapseFraction
                    }
            )

            // Expanded large title (shown at bottom when expanded)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 8.dp, start = 4.dp)
                    .graphicsLayer {
                        alpha = (1f - collapseFraction * 1.5f).coerceIn(0f, 1f)
                        translationY = -(collapseFraction * 20f)
                    }
            ) {
                Text(
                    text = title,
                    style = if (title.length > 13) {
                        MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                    } else {
                        MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Creates and remembers a NestedScrollConnection that shrinks/expands the top bar with a spring release animation.
 */
@Composable
fun rememberCollapsibleTopBarConnection(
    topBarHeight: Animatable<Float, *>,
    minHeightDp: Float,
    maxHeightDp: Float
): NestedScrollConnection {
    val coroutineScope = rememberCoroutineScope()

    return remember(minHeightDp, maxHeightDp) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y / 2.5f // Convert pixels to dp approximately
                val current = topBarHeight.value
                val target = (current + delta).coerceIn(minHeightDp, maxHeightDp)
                val consumedY = (target - current) * 2.5f

                coroutineScope.launch {
                    topBarHeight.snapTo(target)
                }

                return if (delta < 0) {
                    Offset(0f, consumedY)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0) {
                    val delta = available.y / 2.5f
                    val current = topBarHeight.value
                    val target = (current + delta).coerceIn(minHeightDp, maxHeightDp)
                    val consumedY = (target - current) * 2.5f

                    coroutineScope.launch {
                        topBarHeight.snapTo(target)
                    }
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val fraction = (maxHeightDp - topBarHeight.value) / (maxHeightDp - minHeightDp)
                val target = if (fraction < 0.5f) maxHeightDp else minHeightDp
                topBarHeight.animateTo(
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
}
