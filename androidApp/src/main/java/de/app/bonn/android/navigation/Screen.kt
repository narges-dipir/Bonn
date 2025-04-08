package de.app.bonn.android.navigation

sealed class Screen(val route: String) {
    object NotificationScreen: Screen("notification_screen")
    object WallpaperScreen: Screen("wallpaper_screen")
}