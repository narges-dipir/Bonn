package de.app.bonn.android.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.app.bonn.android.BuildConfig
import de.app.bonn.android.material.LightBeige

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBeige)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "About Bunn",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Version: ${BuildConfig.VERSION_NAME}",
            fontSize = 16.sp
        )

        Text(
            text = "Developed by: Bunn Team",
            fontSize = 16.sp
        )

        Text(
            text = "Bunn is a lifestyle and wellness-themed app that displays emotionally resonant video clips on your device. It is not intended to provide medical or therapeutic advice. Always consult professionals for your concerns.",
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Preview
@Composable
fun ShowAboutScreenPreview() {
    AboutScreen()
}