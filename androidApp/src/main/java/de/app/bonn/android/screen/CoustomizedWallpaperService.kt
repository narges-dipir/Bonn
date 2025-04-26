package de.app.bonn.android.screen

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import de.app.bonn.android.service.VideoLiveWallpaperService

@Composable
fun CustomizedWallpaperService() {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val context = LocalContext.current

        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, VideoLiveWallpaperService::class.java)
            )
        }
       context.startActivity(intent)

    }
}