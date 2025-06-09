package de.app.bonn.android.screen

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import de.app.bonn.android.MainActivity
import de.app.bonn.android.di.DeviceIdProvider
import de.app.bonn.android.material.DarkGrassGreen
import de.app.bonn.android.material.DarkerYellow
import de.app.bonn.android.material.GrassGreen
import de.app.bonn.android.material.Orange
import de.app.bonn.android.screen.viewmodel.VideoViewModel
import de.app.bonn.android.service.VideoLiveWallpaperService
import de.app.bonn.android.widget.GradientAlertDialog

@Composable
fun CustomizedWallpaperService(
    videoViewModel: VideoViewModel = hiltViewModel(),
    deviceIDProvider: DeviceIdProvider
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }

    val deviceId = deviceIDProvider.getDeviceId()
    videoViewModel.getVideo(deviceId = deviceId)

    val shouldLaunchWallpaperIntent = remember { mutableStateOf(false) }

    if (shouldLaunchWallpaperIntent.value) {
        LaunchedEffect(Unit) {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, VideoLiveWallpaperService::class.java)
                )
            }
            launcher.launch(intent)
        }
    }

    GradientAlertDialog(
        onDismissRequest = null,
        onConfirmation = { shouldLaunchWallpaperIntent.value = true },
        dialogTitle = "Permission Required",
        dialogText = "Set the first wallpaper true to enable our service!",
        icon = Icons.Default.Check,
        colors = listOf(DarkGrassGreen, GrassGreen)
    )
}
