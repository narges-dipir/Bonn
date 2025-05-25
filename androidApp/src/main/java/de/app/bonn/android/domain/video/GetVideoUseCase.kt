package de.app.bonn.android.domain.video

import de.app.bonn.android.common.Result
import de.app.bonn.android.network.data.responde.VideoDecider
import de.app.bonn.android.repository.getVideo.VideoBackgroundRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVideoUseCase @Inject constructor(
    private val videoBackgroundRepository: VideoBackgroundRepository
) {
    operator fun invoke(deviceId: String) : Flow<Result<VideoDecider>> {
        return videoBackgroundRepository.getLastVideo(deviceId)
    }
}