package de.app.bonn.android

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import de.app.bonn.android.di.DeviceIdProvider
import de.app.bonn.android.navigation.Screen
import de.app.bonn.android.network.remote.ApiService
import de.app.bonn.android.screen.CustomizedWallpaperService
import de.app.bonn.android.screen.NotificationPermissionScreen
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

        setContent {
           navController = rememberNavController()
            AppNavGraph(navController)
            PermissionBasedEntryPoint(navController)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::navController.isInitialized) {
            navigateBasedOnPermission(this, navController)
        }
    }

    @Composable
    fun PermissionBasedEntryPoint(navController: NavHostController) {
        val context = LocalContext.current
        val currentContext by rememberUpdatedState(context)

        LaunchedEffect(Unit) {
            val destination = if (!isNotificationPermissionGranted(currentContext)) {
                Screen.NotificationScreen.route
            } else {
                Screen.WallpaperScreen.route
            }

            if (navController.currentDestination?.route != destination) {
                navController.navigate(destination) {
                    popUpTo(0) // optional: clears stack
                    launchSingleTop = true
                }
            }
        }

    }

    fun navigateBasedOnPermission(
        context: Context,
        navController: NavHostController
    ) {
        val destination = if (!isNotificationPermissionGranted(context)) {
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
                CustomizedWallpaperService()
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
}


@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
    }
}
