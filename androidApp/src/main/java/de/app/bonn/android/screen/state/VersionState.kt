package de.app.bonn.android.screen.state

import de.app.bonn.android.network.data.responde.VersionDecider

data class VersionState (
    val version: VersionDecider = VersionDecider(mustUpdate = false, shouldUpdate = false, minVersion = 0, latestVersion = 0, message = "", playStoreUrl = ""),
    val isLoading: Boolean = false,
    val error: String? = null
)