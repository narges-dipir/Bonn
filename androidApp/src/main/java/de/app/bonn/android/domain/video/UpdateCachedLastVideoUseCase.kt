package de.app.bonn.android.domain.video

import de.app.bonn.android.network.data.responde.VideoDecider
import de.app.bonn.android.repository.getVideo.VideoBackgroundRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateCachedLastVideoUseCase @Inject constructor(
    private val videoBackgroundRepository: VideoBackgroundRepository
) {
    suspend operator fun invoke(videoDecider: VideoDecider) {
        videoBackgroundRepository.updateCachedLastVideo(videoDecider)
    }
}