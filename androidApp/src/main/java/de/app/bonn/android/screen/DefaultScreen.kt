package de.app.bonn.android.screen

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import de.app.bonn.android.common.LAST_VIDEO_NAME
import de.app.bonn.android.di.SharedPreferencesHelper
import de.app.bonn.android.material.LightGrassGreen
import de.app.bonn.android.network.data.responde.VersionDecider
import de.app.bonn.android.screen.viewmodel.VersionViewModel
import de.app.bonn.android.widget.VersionAlertDialog
import java.io.File

@Composable
fun DefaultScreen(
    versionViewModel: VersionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var initialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        SharedPreferencesHelper.ensureInitialized(context)
        initialized = true
        versionViewModel.getLatestVersion()
    }
    if (!initialized) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val videoName by SharedPreferencesHelper
        .getStringFlow(LAST_VIDEO_NAME)
        .collectAsState()

    val file = File(context.getExternalFilesDir(null), "$videoName")

    val versionState by versionViewModel.versionState.collectAsState()
    val version = versionState.version

    VersionAlertDialog(
        version = version,
        backgroundColor = Color(0xFFFAFAFA), // or any custom color
        onDismiss = { println("ok!") }
    )

    Box(modifier = Modifier.fillMaxSize().background(LightGrassGreen)) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize(),
        factory = {
            val videoView = VideoView(it).apply {
                setVideoURI(file.toUri())
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    mediaPlayer.setVolume(1f, 1f)
                    start()
                }
            }
            videoView
        },
        update = { view ->
            if (!view.isPlaying) {
                view.start()
            }
        }
    )
        // 📝 Overlay text centered
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Wallpaper applied!",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )
        }

        // 📖 Side menu (vertical)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 60.dp, start = 16.dp)
                .width(48.dp),
            verticalArrangement = Arrangement.Top
        ) {
            IconButton(onClick = { /* TODO: Open drawer or go home */ }) {
                Icon(Icons.Default.Person, contentDescription = "Home", tint = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = { /* TODO: Open favorites */ }) {
                Icon(Icons.Default.Info, contentDescription = "Favorites", tint = Color.White)
            }
        }

        // 🔘 Bottom buttons
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp)
                .background(Color.Transparent),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Drink a glass of water every morning!",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .fillMaxWidth()
            )
        }
    }
}
