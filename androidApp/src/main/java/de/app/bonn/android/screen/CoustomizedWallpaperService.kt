package de.app.bonn.android.screen

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import de.app.bonn.android.MainActivity
import de.app.bonn.android.R
import de.app.bonn.android.di.DeviceIdProvider
import de.app.bonn.android.material.DarkGrassGreen
import de.app.bonn.android.material.GrassGreen
import de.app.bonn.android.screen.viewmodel.VideoViewModel
import de.app.bonn.android.service.VideoLiveWallpaperService
import de.app.bonn.android.widget.GradientAlertDialog
import android.provider.Settings
import java.io.File

@Composable
fun CustomizedWallpaperService(
    videoViewModel: VideoViewModel = hiltViewModel(),
    deviceIDProvider: DeviceIdProvider
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val shouldLaunchPicker = remember { mutableStateOf(false) }

    // Every time the screen becomes visible, check if our live wallpaper is set
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isActive = isMyLiveWallpaperActive(context)
                if (isActive) {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(intent)
                }
            }
        }

        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    // Trigger wallpaper picker intent
    if (shouldLaunchPicker.value) {
        LaunchedEffect(Unit) {
            shouldLaunchPicker.value = false
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, VideoLiveWallpaperService::class.java)
                )
            }
            context.startActivity(intent)
        }
    }

    // Load the video content
    val deviceId = deviceIDProvider.getDeviceId()
    videoViewModel.getVideo(deviceId = deviceId)

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.cart_for_wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GradientAlertDialog(
                onDismissRequest = null,
                onConfirmation = { shouldLaunchPicker.value = true },
                dialogTitle = "Permission Required",
                dialogText = "Set the first wallpaper true to enable our service!",
                icon = Icons.Default.Check,
                colors = listOf(DarkGrassGreen, GrassGreen)
            )
        }
    }
}


private fun isMyLiveWallpaperActive(context: Context): Boolean {
    val flagFile = File(context.filesDir, "wallpaper_active.flag")
    val isActive = flagFile.exists()
    return (isActive)
}
