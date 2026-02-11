package de.app.bonn.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    val token: String,
    val userId: String,
    val timeZone: String,
)

@Serializable
data class VersionRequest(
    val deviceId: String,
    val version: Int,
)

@Serializable
data class VideoResponse(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("url")
    val url: String,
    @SerialName("silentUrl")
    val silentUrl: String,
    @SerialName("uploadedAt")
    val uploadedAt: String,
)

@Serializable
data class VersionResponse(
    @SerialName("mustUpdate")
    val mustUpdate: Boolean,
    @SerialName("shouldUpdate")
    val shouldUpdate: Boolean,
    @SerialName("minVersion")
    val minVersion: Int,
    @SerialName("latestVersion")
    val latestVersion: Int,
    @SerialName("message")
    val message: String,
    @SerialName("playStoreUrl")
    val playStoreUrl: String,
)

data class VideoDecider(
    val isCacheAvailable: Boolean,
    val video: String,
    val silentUrl: String,
    val name: String,
)

data class VersionDecider(
    val mustUpdate: Boolean,
    val shouldUpdate: Boolean,
    val minVersion: Int,
    val latestVersion: Int,
    val message: String,
    val playStoreUrl: String,
)
