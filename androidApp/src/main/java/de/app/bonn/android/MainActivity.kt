package de.app.bonn.android

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import de.app.bonn.android.common.IS_WALLPAPER_SET
import de.app.bonn.android.di.DeviceIdProvider
import de.app.bonn.android.di.SharedPreferencesHelper
import de.app.bonn.android.navigation.Screen
import de.app.bonn.android.network.remote.ApiService
import de.app.bonn.android.screen.CustomizedWallpaperService
import de.app.bonn.android.screen.DefaultScreen
import de.app.bonn.android.screen.NotificationPermissionScreen
import de.app.bonn.android.service.VideoLiveWallpaperService
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
private lateinit var navController: NavHostController
    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var deviceIDProvider: DeviceIdProvider


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesHelper.putBoolean(IS_WALLPAPER_SET, isMyLiveWallpaperActive(this))
        println("**** isMyLiveWallpaperActive: ${SharedPreferencesHelper.getBoolean(IS_WALLPAPER_SET)}")
        setContent {
           navController = rememberNavController()
            AppNavGraph(navController)
            PermissionBasedEntryPoint(navController)
        }
    }

    override fun onResume() {
        super.onResume()
        println(" *** im in onResume")
        if (::navController.isInitialized) {
            navigateBasedOnPermission(this, navController)
        }
    }

    @Composable
    fun PermissionBasedEntryPoint(navController: NavHostController) {
        val context = LocalContext.current
        val currentContext by rememberUpdatedState(context)
        println(" *** the result is ${isMyLiveWallpaperActive(currentContext)}")
        LaunchedEffect(Unit) {
            val destination = if(SharedPreferencesHelper.getBoolean(IS_WALLPAPER_SET)) {
                Screen.DefaultWallpaperScreen.route
            } else if (!isNotificationPermissionGranted(currentContext)) {
                Screen.NotificationScreen.route
            } else {
                Screen.WallpaperScreen.route
            }
//            val destination = if (!isNotificationPermissionGranted(currentContext)) {
//                Screen.NotificationScreen.route
//            } else if (!sharedPreferencesHelper.getBoolean(IS_WALLPAPER_SET)) {
//                Screen.WallpaperScreen.route
//            } else {
//                Screen.DefaultWallpaperScreen.route
//            }

            if (navController.currentDestination?.route != destination) {
                navController.navigate(destination) {
                    popUpTo(0) // optional: clears stack
                    launchSingleTop = true
                }
            }
        }

    }

    private fun navigateBasedOnPermission(
        context: Context,
        navController: NavHostController
    ) {
        val destination = if(SharedPreferencesHelper.getBoolean(IS_WALLPAPER_SET)) {
            Screen.DefaultWallpaperScreen.route
        } else if (!isNotificationPermissionGranted(context)) {
            Screen.NotificationScreen.route
        } else {
            Screen.WallpaperScreen.route
        }

        if (navController.currentDestination?.route != destination) {
            navController.navigate(destination) {
                popUpTo(0)
                launchSingleTop = true
            }
        }
    }

    @Composable
    fun AppNavGraph(navController: NavHostController) {
        NavHost(navController = navController, startDestination = "notification_screen") {
            composable(Screen.NotificationScreen.route) {
                NotificationPermissionScreen(deviceIDProvider = deviceIDProvider)
            }
            composable(Screen.WallpaperScreen.route) {
                CustomizedWallpaperService(deviceIDProvider = deviceIDProvider)
            }
            composable(Screen.DefaultWallpaperScreen.route) {
                DefaultScreen()
            }

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
