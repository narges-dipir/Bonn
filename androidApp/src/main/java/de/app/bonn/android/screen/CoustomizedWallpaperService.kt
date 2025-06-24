package de.app.bonn.android.screen

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.app.bonn.android.MainActivity
import de.app.bonn.android.R
import de.app.bonn.android.di.DeviceIdProvider
import de.app.bonn.android.material.DarkGrassGreen
import de.app.bonn.android.material.DarkerYellow
import de.app.bonn.android.material.GrassGreen
import de.app.bonn.android.material.Orange
import de.app.bonn.android.screen.viewmodel.VideoViewModel
import de.app.bonn.android.service.VideoLiveWallpaperService
import de.app.bonn.android.widget.GradientAlertDialog
import kotlinx.coroutines.delay

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

    LaunchedEffect(shouldLaunchWallpaperIntent.value) {
        if (shouldLaunchWallpaperIntent.value) {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, VideoLiveWallpaperService::class.java)
                )
            }
            launcher.launch(intent)

            if (waitForWallpaperToBeSet(context)) {
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                println("*** im here")
                context.startActivity(mainIntent)
            }
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.cart_for_wallpaper), // replace with your image resource
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
                onConfirmation = { shouldLaunchWallpaperIntent.value = true },
                dialogTitle = "Permission Required",
                dialogText = "Set the first wallpaper true to enable our service!",
                icon = Icons.Default.Check,
                colors = listOf(DarkGrassGreen, GrassGreen)
            )
        }
    }
}

suspend fun waitForWallpaperToBeSet(context: Context): Boolean {
    val wm = WallpaperManager.getInstance(context)
    repeat(10) {
        val info = wm.wallpaperInfo
        println("*** Checking wallpaper info: ${info.toString()}")
        if (info?.serviceName?.endsWith("VideoLiveWallpaperService") == true) {
            return true
        }
        delay(1000L)
    }
    return false
}

