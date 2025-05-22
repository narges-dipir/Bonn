package de.app.bonn.android.network.data.responde

import com.google.gson.annotations.SerializedName

data class VideoResponse(
@SerializedName("id")
val id: Int,
@SerializedName("name")
val name: String,
@SerializedName("url")
val url: String,
@SerializedName("uploadedAt")
val uploadedAt: String
)
