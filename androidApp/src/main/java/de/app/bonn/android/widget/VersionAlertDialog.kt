package de.app.bonn.android.widget

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Colors
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import de.app.bonn.android.network.data.responde.VersionDecider
import androidx.core.net.toUri
import de.app.bonn.android.R
import de.app.bonn.android.material.DarkRed
import de.app.bonn.android.material.DarkYellow
import de.app.bonn.android.material.DarkerYellow
import de.app.bonn.android.material.LightRed
import de.app.bonn.android.material.LightYellow
import de.app.bonn.android.material.Orange

@Composable
fun VersionAlertDialog(
    version: VersionDecider,
    backgroundColor: Color = Color.Yellow,
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val openPlayStore = {
        val intent = Intent(Intent.ACTION_VIEW, version.playStoreUrl.toUri())
        context.startActivity(intent)
    }
    val openAlertDialog = remember { mutableStateOf(!version.mustUpdate) }
        if (version.mustUpdate) {
            GradientAlertDialog(
                onDismissRequest = null,
                onConfirmation = openPlayStore,
                dialogTitle = "Update Available",
                dialogText = version.message,
                icon = Icons.Default.Build,
                colors = listOf(DarkRed, Orange)
            )
        } else if (version.shouldUpdate && onDismiss != null) {
            when {
                openAlertDialog.value -> {
            GradientAlertDialog(
                onDismissRequest = { openAlertDialog.value = false },
                onConfirmation = openPlayStore,
                dialogTitle = "Update Available",
                dialogText = version.message,
                icon = Icons.Default.Build,
                colors = listOf(DarkerYellow, Orange)
            )
        }
    }
}
}


@Composable
fun GradientAlertDialog(
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    colors: List<Color>
) {
    Dialog(onDismissRequest = { onDismissRequest?.invoke() }) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .wrapContentSize()
        ) {
            // Main dialog card
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = colors.map { it.copy(alpha = 0.3f) } //   listOf(Color(0xFF89F7FE), Color(0xFF66A6FF))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {
                Text(
                    text = dialogTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dialogText,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismissRequest ?: { }) {
                        Text("Dismiss", color = Color.White)
                    }
                    TextButton(onClick = onConfirmation) {
                        Text("Confirm", color = Color.White)
                    }
                }
            }


            Icon(
                imageVector = icon,
                contentDescription = "Dialog Icon",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-27).dp)
                    .background(
                        color = colors.last().copy(alpha = 0.3f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(12.dp)
            )
        }
    }
}
