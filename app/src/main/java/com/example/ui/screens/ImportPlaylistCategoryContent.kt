package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.playlistimport.*
import com.example.presentation.viewmodel.ImportPlaylistViewModel
import com.example.presentation.viewmodel.ImportUiState
import com.example.ui.components.settings.ImportTrackPreviewItem
import com.example.ui.components.settings.SettingsSubsection
import com.example.ui.theme.MyApplicationTheme

@Composable
fun ImportPlaylistCategoryContent(
    onImportComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImportPlaylistViewModel? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as? android.app.Application
    val resolvedViewModel: ImportPlaylistViewModel = viewModel ?: if (app != null) {
        viewModel(factory = ImportPlaylistViewModel.provideFactory(app))
    } else {
        viewModel()
    }

    val uiState by resolvedViewModel.uiState.collectAsState()
    var urlText by remember { mutableStateOf("") }

    val isParsing = uiState is ImportUiState.Parsing
    val isImporting = uiState is ImportUiState.Importing

    val previewResult: PlaylistImportResult? = when (val state = uiState) {
        is ImportUiState.Preview -> state.result
        is ImportUiState.Importing -> state.preview
        is ImportUiState.Success -> state.result
        else -> null
    }

    val importProgress: ImportProgress? = when (val state = uiState) {
        is ImportUiState.Importing -> state.progress
        is ImportUiState.Success -> state.progress
        else -> null
    }

    val sampleLinks = listOf(
        "Spotify" to "https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M",
        "Apple Music" to "https://music.apple.com/us/playlist/todays-hits/pl.f4d106fed2bd41149aaacabb233eb5eb",
        "YouTube Music" to "https://music.youtube.com/playlist?list=PL4fGSI1pDJn6jXS_5NWD36m_R4Bq92330"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("import_playlist_category_content")
    ) {
        // Section 1: URL Input Card
        SettingsSubsection(title = "IMPORT FROM LINK") {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Paste a public playlist link",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Supports Spotify, Apple Music, and YouTube Music links",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = urlText,
                        onValueChange = {
                            urlText = it
                            resolvedViewModel.setUrlText(it)
                        },
                        placeholder = {
                            Text(
                                text = "https://open.spotify.com/playlist/...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_url_input"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (isParsing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                IconButton(
                                    onClick = { resolvedViewModel.parseUrl(urlText) },
                                    enabled = urlText.isNotBlank() && !isParsing,
                                    modifier = Modifier.testTag("import_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Playlist",
                                        tint = if (urlText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    // Quick Sample Link Chips
                    Text(
                        text = "Quick Sample Links:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(sampleLinks) { (label, link) ->
                            SuggestionChip(
                                onClick = {
                                    urlText = link
                                    resolvedViewModel.setUrlText(link)
                                    resolvedViewModel.parseUrl(link)
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                shape = CircleShape,
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = null
                            )
                        }
                    }
                }
            }
        }

        // Error message if any
        if (uiState is ImportUiState.Error) {
            val errorMsg = (uiState as ImportUiState.Error).message
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = errorMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Section 2: Show Preview Card when URL is parsed
        AnimatedVisibility(
            visible = previewResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (previewResult != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(16.dp))
                    SettingsSubsection(title = "PREVIEW") {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Cover + Name Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AsyncImage(
                                        model = previewResult.coverUrl,
                                        contentDescription = "Playlist Cover",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = previewResult.playlistName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "${previewResult.tracks.size} tracks • ${previewResult.source.name.replace("_", " ")}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                                Spacer(Modifier.height(12.dp))

                                // Track list preview (first 5 tracks)
                                val previewTracks = previewResult.tracks.take(5)
                                previewTracks.forEachIndexed { idx, track ->
                                    ImportTrackPreviewItem(
                                        track = track,
                                        index = idx + 1
                                    )
                                    if (idx < previewTracks.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                        )
                                    }
                                }

                                if (previewResult.tracks.size > 5) {
                                    Text(
                                        text = "+ ${previewResult.tracks.size - 5} more tracks in queue",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 10.dp, start = 32.dp)
                                    )
                                }

                                Spacer(Modifier.height(20.dp))

                                // Import Action Button
                                Button(
                                    onClick = { resolvedViewModel.confirmImport() },
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("confirm_import_button"),
                                    enabled = !isImporting && (importProgress == null || !importProgress.isComplete),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    if (isImporting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.5.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = if (importProgress != null) "Importing (${importProgress.current}/${importProgress.totalTracks})..." else "Importing...",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    } else if (importProgress != null && importProgress.isComplete) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Playlist Imported",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Import Playlist",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Show Result Card when import finishes
        AnimatedVisibility(
            visible = importProgress != null && importProgress.isComplete,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut()
        ) {
            if (importProgress != null && importProgress.isComplete) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(16.dp))
                    SettingsSubsection(title = "RESULT") {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Import Complete",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "${importProgress.matchedTracks} of ${importProgress.totalTracks} songs imported to your library",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (importProgress.createdPlaylistId != null) {
                                    Spacer(Modifier.height(14.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            onImportComplete(importProgress.createdPlaylistId)
                                        },
                                        shape = CircleShape,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlaylistPlay,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Open In Playlists", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ImportPlaylistCategoryContentPreview() {
    MyApplicationTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ImportPlaylistCategoryContent(onImportComplete = {})
        }
    }
}
