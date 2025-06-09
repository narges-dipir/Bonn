package de.app.bonn.android.network.data.responde


data class VersionDecider(
    val mustUpdate : Boolean,
    val shouldUpdate : Boolean ,
    val minVersion : Int,
    val latestVersion: Int ,
    val message: String ,
    val playStoreUrl: String
)
