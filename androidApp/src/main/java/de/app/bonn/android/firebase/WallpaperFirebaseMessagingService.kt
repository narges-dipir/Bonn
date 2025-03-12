package de.app.bonn.android.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.app.bonn.android.worker.VideoDownloadWorker

class WallpaperFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        message.data["video_url"]?.let { videoUrl ->
            VideoDownloadWorker.initiate(this, videoUrl)
        }
    }
    override fun onNewToken(token: String) {
        Log.d("FCM", "New Token: $token")
    }
}