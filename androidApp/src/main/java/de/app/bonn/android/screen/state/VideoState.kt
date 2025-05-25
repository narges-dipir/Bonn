package de.app.bonn.android.screen.state

import de.app.bonn.android.network.data.responde.VideoDecider

data class VideoState (
    val video: VideoDecider = VideoDecider(name = "", video = "", isCacheAvailable = false),
    val isLoading: Boolean = false,
    val error: String? = null
)