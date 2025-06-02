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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import de.app.bonn.android.common.LAST_VIDEO_NAME
import de.app.bonn.android.di.SharedPreferencesHelper
import java.io.File

@Composable
fun DefaultScreen() {
    val context = LocalContext.current
    val videoName =  remember {   SharedPreferencesHelper.getString(LAST_VIDEO_NAME) }
    val file = File(context.getExternalFilesDir(null), "$videoName")

    Box(modifier = Modifier.fillMaxSize()) {
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
                Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = { /* TODO: Open favorites */ }) {
                Icon(Icons.Default.Favorite, contentDescription = "Favorites", tint = Color.White)
            }
        }

        // 🔘 Bottom buttons
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { /* Set another wallpaper */ }) {
                Text("Set another wallpaper")
            }
        }
    }
}
