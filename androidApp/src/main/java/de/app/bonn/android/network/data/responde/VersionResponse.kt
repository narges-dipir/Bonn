package de.app.bonn.android.network.data.responde

import com.google.gson.annotations.SerializedName

data class VersionResponse(
    @SerializedName("mustUpdate")
    val mustUpdate : Boolean,
    @SerializedName("shouldUpdate")
    val shouldUpdate : Boolean ,
    @SerializedName("minVersion")
    val minVersion : Int,
    @SerializedName("latestVersion")
    val latestVersion: Int ,
    @SerializedName("message")
    val message: String ,
    @SerializedName("playStoreUrl")
    val playStoreUrl: String
)
