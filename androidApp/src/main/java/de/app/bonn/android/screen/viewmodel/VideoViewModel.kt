package de.app.bonn.android.screen.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.app.bonn.android.domain.video.GetVideoUseCase
import de.app.bonn.android.screen.state.VideoState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import de.app.bonn.android.common.Result
import de.app.bonn.android.domain.video.UpdateBackGroundVideoUseCase
import de.app.bonn.android.manager.VideoManager
import de.app.bonn.android.worker.VideoDownloadWorker
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val getVideoUseCase: GetVideoUseCase,
    private val videoManager: VideoManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VideoState())
    val uiState = _uiState.asStateFlow()



    fun getVideo(deviceId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        getVideoUseCase(deviceId).onEach { video ->
            when (video) {
                is Result.Success -> {
                    val data = video.data
                    videoManager.downloadVideoIfNeeded(
                        videoUrl = data.video ?: "starter.mp4",
                        videoName = data.name,
                        isCached = data.isCacheAvailable
                    )
                }
                is Result.Error -> {
                    Timber.i("Error fetching video: ")
                }

                is Result.Loading<*> -> {
                    videoManager.downloadVideoIfNeeded(
                        videoUrl = "",
                        videoName = "starter.mp4",
                        isCached = true
                    )
                }
            }

        }.launchIn(viewModelScope)

    }

}