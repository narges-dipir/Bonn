package de.app.bonn.android.screen

import android.app.Activity
import android.net.Uri
import android.util.Log
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
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.NavHostController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import de.app.bonn.android.common.LAST_VIDEO_NAME
import de.app.bonn.android.common.VIDEO_URL
import de.app.bonn.android.di.SharedPreferencesHelper
import de.app.bonn.android.material.DarkGrassGreen
import de.app.bonn.android.material.LightBeige
import de.app.bonn.android.material.SchickBlack
import de.app.bonn.android.navigation.Screen
import de.app.bonn.android.screen.viewmodel.VersionViewModel
import de.app.bonn.android.widget.VersionAlertDialog
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

@Composable
fun DefaultScreen(
    navController: NavHostController,
    versionViewModel: VersionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val showInterstitial = rememberInterstitialAd (adUnitId = "ca-app-pub-1101142563208132/8385802577") //  ca-app-pub-3940256099942544/1033173712
    {
        Timber.i("Interstitial ad dismissed.")
    }

    var initialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        SharedPreferencesHelper.ensureInitialized(context)
        initialized = true
        versionViewModel.getLatestVersion()

        showInterstitial()
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
    val videoUrl by SharedPreferencesHelper.getStringFlow(VIDEO_URL).collectAsState()

    println("*** the vudeo name is: $videoUrl")
    val file = File(context.getExternalFilesDir(null), "$videoName")
    val title = videoName?.toSpacedWords() ?: "Stay Healthy!"

    val versionState by versionViewModel.versionState.collectAsState()
    val version = versionState.version

    val drawerState = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("Home") }

    VersionAlertDialog(
        version = version,
        backgroundColor = Color(0xFFFAFAFA),
        onDismiss = { Timber.i("ok!") }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(modifier = Modifier.padding(16.dp).background(color = LightBeige.copy(alpha = 0.3f))) {
                Text("Menu", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(8.dp))
                Spacer(modifier = Modifier.height(16.dp))

                DrawerItem("About the App") { currentScreen = "About"; scope.launch { navController.navigate(Screen.AboutScreen.route)} }
                DrawerItem("Latest Version") { currentScreen = "Version"; scope.launch { navController.navigate(Screen.VersionScreen.route) } }
                DrawerItem("How to Use the App") { currentScreen = "HowTo" ; scope.launch { navController.navigate(Screen.HowToScreen.route) } }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(DarkGrassGreen)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    VideoView(it).apply {
                        setVideoURI(videoUrl?.toUri())
                        setOnPreparedListener { mediaPlayer ->
                            mediaPlayer.isLooping = true
                            mediaPlayer.setVolume(1f, 1f)
                            start()
                        }
                    }
                },
                update = { view -> if (!view.isPlaying) view.start() }
            )

            // Open drawer button (top-left)
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }

            // Bottom screen name or title
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (currentScreen) {
                        "About" -> "This app promotes daily wellness with custom video wallpapers."
                        "Version" -> "You’re on version ${version.latestVersion}"
                        "HowTo" -> "To use the app, just tap 'Set Wallpaper' and enjoy!"
                        else -> "$title!"
                    },
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
}

@Composable
fun DrawerItem(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text(label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = SchickBlack
            )
    }
}


fun String.toSpacedWords(): String {
    val cleaned = removeSuffix(".mp4")
    return cleaned.replace(Regex("(?<!^)([A-Z])"), " $1")
}


@Composable
fun rememberInterstitialAd(
    adUnitId: String = "ca-app-pub-3940256099942544/1033173712",
    onAdClosed: () -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }
    var shouldShowAd by remember { mutableStateOf(false) }

    // Load the ad
    LaunchedEffect(Unit) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                Timber.tag("AdMob").d("✅ Interstitial ad loaded")
                interstitialAd = ad

                // If the user already requested to show it, now we can
                if (shouldShowAd) {
                    showAd(context as? Activity, interstitialAd, onAdClosed)
                    interstitialAd = null
                    shouldShowAd = false
                }
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Timber.tag("AdMob").e("❌ Failed to load interstitial ad: ${adError.message}")
                interstitialAd = null
            }
        })
    }

    // Return a trigger function
    return {
        val activity = context as? Activity
        if (interstitialAd != null && activity != null) {
            showAd(activity, interstitialAd, onAdClosed)
            interstitialAd = null
        } else {
            Timber.tag("AdMob").d("⏳ Ad not ready yet, will show later")
            shouldShowAd = true
        }
    }
}

private fun showAd(activity: Activity?, ad: InterstitialAd?, onAdClosed: () -> Unit) {
    ad?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
        override fun onAdShowedFullScreenContent() {
            Timber.tag("AdMob").d("✅ Interstitial ad showed")
        }

        override fun onAdDismissedFullScreenContent() {
            Timber.tag("AdMob").d("👋 Interstitial ad dismissed")
            onAdClosed()
        }

        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
            Timber.tag("AdMob").e("❌ Failed to show ad: ${adError.message}")
        }
    }

    ad?.show(activity!!)
}
