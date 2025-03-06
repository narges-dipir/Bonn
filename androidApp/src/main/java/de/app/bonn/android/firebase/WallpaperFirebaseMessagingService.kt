package de.app.bonn.android.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.app.bonn.android.worker.VideoDownloadWorker

class WallpaperFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        message.data["video_url"]?.let { videoUrl ->
            VideoDownloadWorker.initiate(this, videoUrl)
        }
    }
}