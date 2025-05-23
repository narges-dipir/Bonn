package de.app.bonn.android.repository

import de.app.bonn.android.network.data.responde.Video
import de.app.bonn.android.network.data.responde.VideoResponse

fun VideoResponse.toVideo(): Video {
    return Video(
        id = id,
        name = name,
        url = url,
        uploadedAt = uploadedAt
    )
}
