package de.app.bonn.android.domain.video

import de.app.bonn.android.network.data.responde.VideoDecider
import de.app.bonn.android.repository.getVideo.VideoBackgroundRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateBackGroundVideoUseCase @Inject constructor(
    private val videoBackgroundRepository: VideoBackgroundRepository
) {
    operator fun invoke() : Flow<VideoDecider> = videoBackgroundRepository.newVideo
}