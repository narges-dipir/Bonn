package de.app.bonn.android

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.AndroidEntryPoint
import de.app.bonn.android.common.IS_USER_AGREED
import de.app.bonn.android.common.IS_WALLPAPER_SET
import de.app.bonn.android.di.DeviceIdProvider
import de.app.bonn.android.di.SharedPreferencesHelper
import de.app.bonn.android.navigation.Screen
import de.app.bonn.android.screen.AboutScreen
import de.app.bonn.android.screen.CustomizedWallpaperService
import de.app.bonn.android.screen.DefaultScreen
import de.app.bonn.android.screen.HowToScreen
import de.app.bonn.android.screen.NotificationPermissionScreen
import de.app.bonn.android.screen.VersionScreen
import de.app.bonn.android.screen.viewmodel.MainViewModel
import de.app.bonn.android.service.VideoLiveWallpaperService
import de.app.bonn.android.widget.UserAgreementDialog
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var deviceIDProvider: DeviceIdProvider

    private val viewModel: MainViewModel by viewModels()

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPreferencesHelper.putBoolean(IS_WALLPAPER_SET, isMyLiveWallpaperActive(this))
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("5FD1F9629E8F65542DD32F922AD2406F"))
                .build()
        )
        MobileAds.initialize(this)


        setContent {
            val context = this
            navController = rememberNavController()

            // Determine start destination before rendering anything
            val startDestination = when {
                !SharedPreferencesHelper.getBoolean(IS_USER_AGREED) -> Screen.UserAgreementScreen.route
                SharedPreferencesHelper.getBoolean(IS_WALLPAPER_SET) -> Screen.DefaultWallpaperScreen.route
                !isNotificationPermissionGranted(context) -> Screen.NotificationScreen.route
                else -> Screen.WallpaperScreen.route
            }

            AppNavGraph(
                navController = navController,
                startDestination = startDestination,
                deviceIDProvider = deviceIDProvider
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (::navController.isInitialized && viewModel.hasResumedOnce.value) {
            navigateBasedOnPermission(this, navController)
        }
        viewModel.hasResumedOnce.value = true
    }

    private fun navigateBasedOnPermission(
        context: Context,
        navController: NavHostController
    ) {
        val destination = when {
            SharedPreferencesHelper.getBoolean(IS_WALLPAPER_SET) -> Screen.DefaultWallpaperScreen.route
            !isNotificationPermissionGranted(context) -> Screen.NotificationScreen.route
            else -> Screen.WallpaperScreen.route
        }

        if (navController.currentDestination?.route != destination) {
            navController.navigate(destination) {
                popUpTo(0)
                launchSingleTop = true
            }
        }
    }

    @Composable
    fun AppNavGraph(
        navController: NavHostController,
        startDestination: String,
        deviceIDProvider: DeviceIdProvider
    ) {
        NavHost(navController = navController, startDestination = startDestination) {
            composable(Screen.NotificationScreen.route) {
                NotificationPermissionScreen(deviceIDProvider = deviceIDProvider)
            }
            composable(Screen.WallpaperScreen.route) {
                CustomizedWallpaperService(deviceIDProvider = deviceIDProvider)
            }
            composable(Screen.DefaultWallpaperScreen.route) {
                DefaultScreen(navController = navController)
            }
            composable(Screen.UserAgreementScreen.route) {
                UserAgreementDialog(
                    onAgree = {
                        SharedPreferencesHelper.putBoolean(IS_USER_AGREED, true)

                        val nextDestination = when {
                            SharedPreferencesHelper.getBoolean(IS_WALLPAPER_SET) -> Screen.DefaultWallpaperScreen.route
                            !isNotificationPermissionGranted(this@MainActivity) -> Screen.NotificationScreen.route
                            else -> Screen.WallpaperScreen.route
                        }

                        navController.navigate(nextDestination) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                )
            }
            // side screens
            composable(Screen.AboutScreen.route) { AboutScreen() }
            composable(Screen.HowToScreen.route) { HowToScreen() }
            composable(Screen.VersionScreen.route) { VersionScreen() }
        }
    }


    private fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun isMyLiveWallpaperActive(context: Context): Boolean {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val currentWallpaper = wallpaperManager.wallpaperInfo
        val myComponent = ComponentName(context, VideoLiveWallpaperService::class.java)
        return currentWallpaper?.component == myComponent
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
    }
}
