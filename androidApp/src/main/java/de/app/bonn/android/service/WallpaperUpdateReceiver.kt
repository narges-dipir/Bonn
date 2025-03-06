package de.app.bonn.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WallpaperUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "UPDATE_LIVE_WALLPAPER") {
            val serviceIntent = Intent(context, VideoLiveWallpaperService::class.java)
            context?.startService(serviceIntent) // Restart wallpaper service
        }
    }
}