package de.app.bonn.android.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import de.app.bonn.android.di.DeviceIdProvider
import de.app.bonn.android.di.LocalTimeProvider
import de.app.bonn.android.network.ApiService
import de.app.bonn.android.network.data.TokenRequest
import de.app.bonn.android.worker.VideoDownloadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WallpaperFirebaseMessagingService: FirebaseMessagingService() {
    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var deviceIDProvider: DeviceIdProvider

    @Inject
    lateinit var localTimeProvider: LocalTimeProvider

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.registerToken(TokenRequest(token, deviceIDProvider.getDeviceId(), localTimeProvider.timeZone()))
                if (response.isSuccessful) {
                    Timber.i("FCM", "Token registered successfully")
                } else {
                    Timber.e("FCM", "Failed to register token: ${response.code()}")
                }
            } catch (e: Exception) {
                Timber.e("FCM", "Error sending token", e)
            }
        }
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