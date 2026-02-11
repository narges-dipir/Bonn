package de.app.bonn.android.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.provider.Settings
import de.app.bonn.android.BuildConfig
import de.app.bonn.android.material.LightBeige
import de.app.bonn.shared.SharedGraph

@Composable
fun VersionScreen() {
    val context = LocalContext.current
    val versionName = BuildConfig.VERSION_NAME
    val sharedViewModel = remember { SharedGraph.createHomeViewModel() }
    val sharedState by sharedViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "android-device"
        sharedViewModel.refresh(deviceId = deviceId, versionCode = BuildConfig.VERSION_CODE)
    }

    DisposableEffect(Unit) {
        onDispose {
            sharedViewModel.clear()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBeige)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "App Version",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = versionName,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (sharedState.isLoading) "Loading shared state..." else "Shared KMP state loaded",
                fontSize = 14.sp
            )
            Text(
                text = "Latest: ${sharedState.version?.latestVersion ?: 0}",
                fontSize = 14.sp
            )
            Text(
                text = "Video: ${sharedState.video?.name ?: "-"}",
                fontSize = 14.sp
            )
        }
    }

}

@Preview
@Composable
fun VersionScreenPreview() {
    VersionScreen()
}
