package de.app.bonn.android.screen

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import de.app.bonn.android.R
import de.app.bonn.android.material.Background
import de.app.bonn.android.material.ManropeWght
import de.app.bonn.android.material.SchickBlack
import de.app.bonn.android.widget.GreenRoundedButton

@Composable
fun NotificationPermissionScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(Background)
    ) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, handle accordingly
            } else {
                // Permission denied, handle accordingly
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Notification Permission",
            modifier = Modifier
                .fillMaxWidth()
            .align(Alignment.CenterHorizontally),
            style = TextStyle(
                fontFamily = ManropeWght,
                fontSize = 32.sp,
            ),
            textAlign = TextAlign.Center,
            color = Color.Gray,
        )

       Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "To use this app, please allow notifications.",
            modifier = Modifier.fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            style = TextStyle(
                fontFamily = ManropeWght,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            color = SchickBlack
        )

        Image(painter = painterResource(R.drawable.notification_permission),
            modifier = Modifier.wrapContentSize().align(Alignment.CenterHorizontally),
            contentDescription = "Notification Permission")

        GreenRoundedButton(
            text = "Turn on notifications",
        ) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Permission is not required for Android versions below 13
            }
        }

    }
}

@Preview
@Composable
fun PreviewNotificationPermissionScreen() {
    NotificationPermissionScreen()
}