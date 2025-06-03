package de.app.bonn.android.network.data.responde

data class VideoDecider(
    val isCacheAvailable: Boolean,
    val video: String ?= "starter.mp4",
    val name: String
)