package de.app.bonn.android.widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.app.bonn.android.material.GrassGreen
import de.app.bonn.android.material.LightGrassGreen

@Composable
fun GreenRoundedButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
        contentColor = GrassGreen,
        containerColor = LightGrassGreen
    ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 24.dp)
    ){
            Text(
                text = text,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )
        }
}