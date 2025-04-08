package de.app.bonn.android

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import de.app.bonn.Greeting
import de.app.bonn.android.service.VideoLiveWallpaperService
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.common.GoogleApiAvailability
import de.app.bonn.android.screen.NotificationPermissionScreen

class MainActivity : ComponentActivity() {
private lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this@MainActivity, VideoLiveWallpaperService::class.java)
            )
        }
       startActivity(intent)

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
            val destination = if (isNotificationPermissionGranted(currentContext)) {
                "screen_2"
            } else {
                "screen_1"
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
        val destination = if (isNotificationPermissionGranted(context)) {
            "screen_2"
        } else {
            "screen_1"
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
            composable("notification_screen") {
                NotificationPermissionScreen()
            }
            composable("") {
                // Screen2()
            }
        }
    }

    fun decideNavigation(
        context: Context,
        navController: NavController
    ) {
        val destination = if (isNotificationPermissionGranted(context)) {
            "screen_2"
        } else {
            "screen_1"
        }

        // Only navigate if not already on that screen
        if (navController.currentDestination?.route != destination) {
            navController.navigate(destination) {
                popUpTo(0) // Clear back stack if needed
                launchSingleTop = true
            }
        }
    }


    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not needed before Android 13
        }
    }


    @Composable
    fun PermissionRequestFlow() {
        val context = LocalContext.current
        var showNotificationDialog by remember { mutableStateOf(false) }
        var showAppearOnTopDialog by remember { mutableStateOf(false) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isNotificationGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isNotificationGranted) {
                showNotificationDialog = true
            }
        }

        if (!Settings.canDrawOverlays(context)) {
            showAppearOnTopDialog = true
        }

        if (showNotificationDialog && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)) {
            NotificationPermissionDialog(
                onDismiss = { showNotificationDialog = false }
            )
        }

    }



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    fun NotificationPermissionDialog(
        onDismiss: () -> Unit
    ) {
        val context = LocalContext.current

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Toast.makeText(context, "All Permissions Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Some Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Permissions Required") },
            text = { Text("This feature requires access to set wallpaper and write external storage.") },
            confirmButton = {
                Button(
                    onClick = {
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        )

                    }
                ) {
                    Text("Grant Permissions")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }

    @Composable
    fun AppearOnTopPermissionDialog(onDismiss: () -> Unit) {
        val context = LocalContext.current
        val activity = context as? Activity

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Appear on Top Permission Required") },
            text = { Text("This app needs permission to appear over other apps.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (!Settings.canDrawOverlays(context)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            activity?.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Permission already granted", Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }


}
@Composable
fun GreetingView(text: String) {
    Text(text = "nothing to see here, youll see it on the lockScreen")
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("nothing to see here, youll see it on the lockScreen")
    }
}
