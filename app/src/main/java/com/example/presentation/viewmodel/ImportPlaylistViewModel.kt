package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.OuterTuneDatabase
import com.example.data.playlistimport.ImportProgress
import com.example.data.playlistimport.PlaylistImportRepository
import com.example.data.playlistimport.PlaylistImportResult
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ImportUiState {
    object Idle : ImportUiState()
    object Parsing : ImportUiState()
    data class Preview(val result: PlaylistImportResult) : ImportUiState()
    data class Importing(val progress: ImportProgress, val preview: PlaylistImportResult) : ImportUiState()
    data class Success(val playlistId: String, val result: PlaylistImportResult, val progress: ImportProgress) : ImportUiState()
    data class Error(val message: String) : ImportUiState()
}

class ImportPlaylistViewModel @JvmOverloads constructor(
    application: Application,
    private val importRepository: PlaylistImportRepository = PlaylistImportRepository(
        context = application,
        musicRepository = MusicRepository(
            context = application,
            database = OuterTuneDatabase.getInstance(application),
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
        )
    )
) : AndroidViewModel(application) {

    companion object {
        fun provideFactory(application: Application): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val repo = MusicRepository(
                        context = application,
                        database = OuterTuneDatabase.getInstance(application),
                        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
                    )
                    return ImportPlaylistViewModel(
                        application = application,
                        importRepository = PlaylistImportRepository(
                            context = application,
                            musicRepository = repo
                        )
                    ) as T
                }
            }
    }

    private val _urlText = MutableStateFlow("")
    val urlText: StateFlow<String> = _urlText.asStateFlow()

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private var currentPreviewResult: PlaylistImportResult? = null

    fun setUrlText(text: String) {
        _urlText.value = text
    }

    fun parseUrl(url: String = _urlText.value) {
        val target = url.trim()
        if (target.isBlank()) {
            _uiState.value = ImportUiState.Error("Please enter a playlist URL")
            return
        }

        _urlText.value = target
        _uiState.value = ImportUiState.Parsing

        viewModelScope.launch {
            try {
                val result = importRepository.parsePlaylist(target)
                currentPreviewResult = result
                _uiState.value = ImportUiState.Preview(result)
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.localizedMessage ?: "Failed to parse playlist URL")
            }
        }
    }

    fun confirmImport() {
        val preview = currentPreviewResult ?: return
        viewModelScope.launch {
            try {
                importRepository.importPlaylist(preview).collect { progress ->
                    if (progress.isComplete) {
                        _uiState.value = ImportUiState.Success(
                            playlistId = progress.createdPlaylistId ?: "imported_playlist",
                            result = preview,
                            progress = progress
                        )
                    } else {
                        _uiState.value = ImportUiState.Importing(progress, preview)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.localizedMessage ?: "Failed to import playlist")
            }
        }
    }

    fun reset() {
        _urlText.value = ""
        currentPreviewResult = null
        _uiState.value = ImportUiState.Idle
    }
}
