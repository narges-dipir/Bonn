package de.app.bonn.android.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.app.bonn.android.material.LightBeige

@Composable
fun HowToScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBeige)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "How to Use Bunn",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "1. Accept the User Agreement",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Before using Bunn, you’ll be asked to review and accept the terms and disclaimer."
        )

        Text(
            text = "2. Grant Notification Permission",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Allow Bunn to send you relevant updates or reminders through notifications."

        )

        Text(
            text = "3. Set the Live Wallpaper",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Choose Bunn as your live wallpaper to begin seeing personalized video clips throughout the day."

        )

        Text(
            text = "4. Reflect and Stay Mindful",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Each video is designed to promote reflection, motivation, or awareness. Take a moment to consider what the clip brings up for you."

        )

        Text(
            text = "5. You’re in Control",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "You can update or disable the wallpaper and notifications at any time via system settings or within the app."
        )
    }
}

@Preview
@Composable
fun HowToScreenPreview() {
    HowToScreen()
}