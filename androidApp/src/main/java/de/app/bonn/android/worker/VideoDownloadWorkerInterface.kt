package de.app.bonn.android.worker

import de.app.bonn.android.network.data.responde.VideoDecider
import kotlinx.coroutines.flow.Flow

interface VideoDownloadWorkerInterface {
    val downloadedVideo: Flow<VideoDecider>
}