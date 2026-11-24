package com.example.offlinesubplayer

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class PlayerUiState(
    val videoUri: Uri? = null,
    val subtitleName: String? = null,
    val cues: List<SubtitleCue> = emptyList(),
    val activeCue: SubtitleCue? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val showControls: Boolean = true,
    val translateEnabled: Boolean = true,
    val isModelReady: Boolean = false,
    val sourceLang: String = TranslateLanguage.ENGLISH,
    val targetLang: String = TranslateLanguage.INDONESIAN,
    val translationProgress: Float? = null,
    val statusMessage: String = ""
)

class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val translatorManager = OfflineTranslatorManager()
    private var translationJob: Job? = null
    private var modelDownloadJob: Job? = null

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            statusMessage = application.getString(R.string.status_initial)
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        downloadDefaultModel()
    }

    private fun getString(resId: Int, vararg formatArgs: Any): String =
        getApplication<Application>().getString(resId, *formatArgs)

    fun downloadDefaultModel(
        sourceLang: String = _uiState.value.sourceLang,
        targetLang: String = _uiState.value.targetLang
    ) {
        modelDownloadJob?.cancel()
        modelDownloadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(statusMessage = getString(R.string.status_preparing_model))
            }
            runCatching {
                translatorManager.prepareModel(sourceLang, targetLang)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isModelReady = true,
                        statusMessage = getString(
                            R.string.status_model_ready,
                            sourceLang.uppercase(Locale.US),
                            targetLang.uppercase(Locale.US)
                        )
                    )
                }
                if (_uiState.value.translateEnabled && _uiState.value.cues.isNotEmpty()) {
                    translateAllCues()
                }
            }.onFailure { err ->
                val detail = err.localizedMessage ?: getString(R.string.status_model_error_default)
                _uiState.update {
                    it.copy(
                        isModelReady = false,
                        statusMessage = getString(R.string.status_model_error, detail)
                    )
                }
            }
        }
    }

    fun changeLanguagePair(newSourceLang: String, newTargetLang: String) {
        if (newSourceLang == _uiState.value.sourceLang && newTargetLang == _uiState.value.targetLang) return
        translationJob?.cancel()

        val clearedCues = _uiState.value.cues.map { it.copy(translatedText = null) }
        _uiState.update {
            it.copy(
                sourceLang = newSourceLang,
                targetLang = newTargetLang,
                cues = clearedCues,
                activeCue = null,
                isModelReady = false,
                translationProgress = null
            )
        }
        downloadDefaultModel(newSourceLang, newTargetLang)
    }

    fun swapLanguages() {
        val currentSource = _uiState.value.sourceLang
        val currentTarget = _uiState.value.targetLang
        if (currentSource == currentTarget) return
        changeLanguagePair(newSourceLang = currentTarget, newTargetLang = currentSource)
    }

    fun setVideoUri(uri: Uri) {
        _uiState.update { it.copy(videoUri = uri) }
    }

    fun loadSubtitles(fileName: String?, srtContent: String) {
        val parsed = SrtParser.parse(srtContent)
        _uiState.update {
            it.copy(
                subtitleName = fileName ?: getString(R.string.default_subtitle_name),
                cues = parsed,
                activeCue = null,
                statusMessage = getString(R.string.status_loaded_cues, parsed.size)
            )
        }
        if (_uiState.value.translateEnabled && _uiState.value.isModelReady) {
            translateAllCues()
        }
    }

    fun toggleAutoTranslate(enabled: Boolean) {
        _uiState.update { it.copy(translateEnabled = enabled) }
        if (enabled && _uiState.value.isModelReady) {
            translateAllCues()
        }
    }

    fun exportTranslatedSrt(context: Context, uri: Uri, fileName: String? = null) {
        viewModelScope.launch {
            val cues = _uiState.value.cues
            if (cues.isEmpty()) {
                _uiState.update {
                    it.copy(statusMessage = getString(R.string.status_no_subtitles_to_save))
                }
                return@launch
            }
            val srtText = SrtExporter.buildSrtContent(cues, preferTranslated = true)
            SrtExporter.writeToUri(context, uri, srtText)
                .onSuccess {
                    val savedContent = context.contentResolver
                        .openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: srtText
                    loadSubtitles(
                        fileName = fileName
                            ?: uri.lastPathSegment
                            ?: getString(R.string.default_translated_filename),
                        srtContent = savedContent
                    )
                    _uiState.update {
                        it.copy(statusMessage = getString(R.string.status_srt_saved_reloaded))
                    }
                }
                .onFailure { err ->
                    val detail = err.localizedMessage ?: "error"
                    _uiState.update {
                        it.copy(statusMessage = getString(R.string.status_srt_save_error, detail))
                    }
                }
        }
    }

    private fun translateAllCues() {
        translationJob?.cancel()
        translationJob = viewModelScope.launch {
            val currentList = _uiState.value.cues.toMutableList()
            if (currentList.isEmpty()) {
                _uiState.update { it.copy(translationProgress = null) }
                return@launch
            }

            _uiState.update { it.copy(translationProgress = 0f) }

            for (i in currentList.indices) {
                val cue = currentList[i]
                val updatedCue = if (cue.translatedText == null) {
                    cue.copy(translatedText = translatorManager.translateText(cue.originalText))
                } else {
                    cue
                }
                currentList[i] = updatedCue

                val progress = (i + 1).toFloat() / currentList.size.toFloat()
                if (i % 3 == 0 || i == currentList.lastIndex) {
                    _uiState.update {
                        it.copy(
                            cues = currentList.toList(),
                            translationProgress = progress,
                            statusMessage = getString(
                                R.string.status_translating_progress,
                                (progress * 100).toInt()
                            )
                        )
                    }
                }
            }

            _uiState.update {
                it.copy(
                    translationProgress = null,
                    statusMessage = getString(
                        R.string.status_translation_done,
                        it.sourceLang.uppercase(Locale.US),
                        it.targetLang.uppercase(Locale.US)
                    )
                )
            }
        }
    }

    fun onPlaybackTick(positionMs: Long, durationMs: Long, isPlaying: Boolean) {
        val cue = _uiState.value.cues.firstOrNull { positionMs in it.startMs..it.endMs }
        _uiState.update {
            it.copy(
                currentPositionMs = positionMs,
                durationMs = durationMs.coerceAtLeast(0L),
                isPlaying = isPlaying,
                activeCue = cue
            )
        }
    }

    fun toggleControls() {
        _uiState.update { it.copy(showControls = !it.showControls) }
    }

    override fun onCleared() {
        modelDownloadJob?.cancel()
        translationJob?.cancel()
        translatorManager.release()
        super.onCleared()
    }
}
