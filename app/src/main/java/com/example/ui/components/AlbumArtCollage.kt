package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.Song

// Organic bean/squircle shape using cubic Bezier curves
val BlobShape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.04f)
            cubicTo(w * 0.55f, -0.04f, w * 0.92f, h * 0.12f, w * 0.98f, h * 0.42f)
            cubicTo(w * 1.04f, h * 0.72f, w * 0.88f, h * 0.96f, w * 0.58f, h * 0.98f)
            cubicTo(w * 0.28f, h * 1.00f, w * 0.02f, h * 0.86f, 0f, h * 0.58f)
            cubicTo(-0.02f, h * 0.30f, w * 0.02f, h * 0.10f, w * 0.22f, h * 0.04f)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun AlbumArtCollage(
    songs: List<Song>,
    onPlayAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mainSong = songs.getOrNull(0)
    val topStartSong = songs.getOrNull(1)
    val bottomEndSong = songs.getOrNull(2)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .testTag("album_art_collage"),
        contentAlignment = Alignment.Center
    ) {
        // Inner relative container
        Box(
            modifier = Modifier
                .width(310.dp)
                .height(260.dp)
        ) {
            // Main Central Bean/Squircle Blob Album Cover (~220dp)
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.Center)
                    .shadow(16.dp, shape = BlobShape)
                    .clip(BlobShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.25f),
                        shape = BlobShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!mainSong?.albumArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = mainSong?.albumArtUri,
                        contentDescription = mainSong?.title ?: "Main Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            // Top-Start Circular Cover (56dp), slightly overlapping main art
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 16.dp, y = 14.dp)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!topStartSong?.albumArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = topStartSong?.albumArtUri,
                        contentDescription = topStartSong?.title ?: "Top-Start Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom-End Circular Cover (56dp), slightly overlapping main art
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-36).dp, y = (-12).dp)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!bottomEndSong?.albumArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = bottomEndSong?.albumArtUri,
                        contentDescription = bottomEndSong?.title ?: "Bottom-End Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Circular Play Button (72dp) with Color(0xFFA8C7FA) fill and dark play triangle
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 6.dp)
                    .shadow(14.dp, CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onPlayAll)
                    .testTag("album_collage_play_all"),
                shape = CircleShape,
                color = Color(0xFFA8C7FA)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Mix",
                        tint = Color(0xFF001D35),
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }
    }
}
