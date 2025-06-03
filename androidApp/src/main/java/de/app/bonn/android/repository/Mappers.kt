package de.app.bonn.android.repository

import de.app.bonn.android.network.data.responde.Video
import de.app.bonn.android.network.data.responde.VideoDecider
import de.app.bonn.android.network.data.responde.VideoResponse
import de.app.bonn.android.source.db.model.VideoCached

fun VideoResponse.toVideo(): Video {
    return Video(
        id = id,
        name = name,
        url = url,
        uploadedAt = uploadedAt
    )
}

fun VideoDecider.toVideoCached(): VideoCached {
    return VideoCached(
        name = name,
        storagePath = video ?: "starter.mp4",
    )
}

fun VideoCached.toVideoDecider(): VideoDecider {
    return VideoDecider(
        isCacheAvailable = true,
        video = storagePath,
        name = name
    )
}