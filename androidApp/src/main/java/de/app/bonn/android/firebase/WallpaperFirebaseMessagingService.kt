package de.app.bonn.android.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.app.bonn.android.worker.VideoDownloadWorker
import timber.log.Timber

class WallpaperFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        Timber.i("Message received: $message")
        println("Message received: ${message.data}")
         val videoUrl = message.data["video_url"] ?: ""
        val videoName = message.data["video_name"] ?: ""
        VideoDownloadWorker.initiate(this, videoUrl, videoName)
    }
    override fun onNewToken(token: String) {
        Log.d("FCM", "New Token: $token")
    }
}