package de.app.bonn.android

import android.Manifest
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.app.bonn.Greeting
import de.app.bonn.android.service.VideoLiveWallpaperService
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import de.app.bonn.android.forgroundService.WallpaperForegroundService
import de.app.bonn.android.worker.VideoDownloadWorker

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VideoDownloadWorker.initiate(this)
        val serviceIntent = Intent(this, WallpaperForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this@MainActivity, VideoLiveWallpaperService::class.java)
            )
        }
        this.startActivity(intent)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GreetingView(Greeting().greet())

                }
            }
        }
    }
}
@Composable
fun GreetingView(text: String) {
    Text(text = "nothing to see here, youll see it on the lockScreen")
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("nothing to see here, youll see it on the lockScreen")
    }
}
