package de.app.bonn.android.navigation

sealed class Screen(val route: String) {
    object NotificationScreen: Screen("notification_screen")
    object WallpaperScreen: Screen("wallpaper_screen")
    object DefaultWallpaperScreen: Screen("default_wallpaper_screen")
    object UserAgreementScreen: Screen("user_agreement_screen")

    object HowToScreen: Screen("how_to_screen")
    object VersionScreen: Screen("version_screen")
    object AboutScreen: Screen("about_screen")
}