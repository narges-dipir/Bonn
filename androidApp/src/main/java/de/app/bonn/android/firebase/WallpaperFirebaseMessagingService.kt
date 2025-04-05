package de.app.bonn.android.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.app.bonn.android.worker.VideoDownloadWorker
import timber.log.Timber


class WallpaperFirebaseMessagingService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Handle the new token as needed
        println("firebase New token: $token")
    }
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Handle the received message as needed
        Timber.i("Message received: $message")
        println("Message received: ${message.data}")
         val videoUrl = message.data["video_url"] ?: ""
        val videoName = message.data["video_name"] ?: ""
        VideoDownloadWorker.initiate(this, videoUrl, videoName)
    }
}