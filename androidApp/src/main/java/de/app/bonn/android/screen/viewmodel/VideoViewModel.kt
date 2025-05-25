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
import de.app.bonn.android.worker.VideoDownloadWorker
import timber.log.Timber

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val getVideoUseCase: GetVideoUseCase,
    private val updateBackGroundVideoUseCase: UpdateBackGroundVideoUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(VideoState())
    val uiState = _uiState.asStateFlow()

    init {
        observeNewVideo()
    }

    private fun observeNewVideo() {
        updateBackGroundVideoUseCase().onEach { videoDecider ->
            println(" **** im in observeNewVideo ****")
            notifyWallpaperService(context, videoDecider.name)
        }
    }
    fun getVideo(deviceId: String, context: Context) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        getVideoUseCase(deviceId).onEach { video ->
            println(" **** im in getVideo **** $video")
            when (video) {
                is Result.Success -> {
                    when (video.data.isCacheAvailable) {
                        true -> {
                            notifyWallpaperService(context, video.data.name)
                        }
                        false -> {
                            VideoDownloadWorker.initiate(context, video.data.video, video.data.name)
                        }
                    }
                }
                is Result.Error -> {
                    Timber.i("Error fetching video: ")
                }
            }

        }.launchIn(viewModelScope)

    }

    private fun notifyWallpaperService(context: Context, video_name: String) {
        val intent = Intent("UPDATE_LIVE_WALLPAPER").apply {
            setPackage("de.app.bonn.android")
            putExtra("video_name", video_name)
        }
        context.sendBroadcast(intent)
    }

}