package de.app.bonn.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WallpaperUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println(" ****** ${intent?.action}")
        if (intent?.action == "UPDATE_LIVE_WALLPAPER") {
            val videoName = intent.getStringExtra("video_name")
            val serviceIntent = Intent(context, VideoLiveWallpaperService::class.java)
            serviceIntent.putExtra("video_name", videoName)
            context?.startService(serviceIntent)
        }
    }
}