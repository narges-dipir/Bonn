package de.app.bonn.android.widget

import android.hardware.lights.Light
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import de.app.bonn.android.material.Background
import de.app.bonn.android.material.LightBeige

@Composable
fun UserAgreementDialog(
    onAgree: () -> Unit
) {
    val scrollState = rememberScrollState()
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(color = LightBeige)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .background(color = LightBeige)
                    .padding(6.dp)
            ) {
                Text(
                    text = "User Agreement & Disclaimer",
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Last Updated: 20/6/2025",
                    style = MaterialTheme.typography.body2
                )
                Spacer(Modifier.height(16.dp))
                    Text(
                        text = """

User Agreement & Disclaimer for Bunn

Last Updated: 20/6/2025

Welcome to Bunn (“we,” “us,” or “our”). By installing, accessing, or using this mobile application (“Bunn”), you agree to be bound by the terms of this User Agreement and Disclaimer. If you do not agree, do not use this App.

1. General Disclaimer
Bunn is a lifestyle and wellness-themed mobile application that displays video clips on your device’s home screen and/or lock screen. These clips may include emotionally stimulating, disturbing, or provocative content, both positive and negative, intended solely to inspire reflection and promote general lifestyle awareness. We make no guarantees regarding the accuracy, safety, effectiveness, or appropriateness of any content.

2. No Medical or Psychological Advice
The App is not a substitute for medical, psychological, psychiatric, or therapeutic advice. All content is for informational and motivational purposes only and should not be interpreted as professional guidance. Any action taken based on the content is at your own discretion and risk. Consult a licensed physician, dietitian, or mental health professional for actual medical or psychological advice.

3. Triggering & Sensitive Content
Some clips may depict content that is:
- Emotionally intense, disturbing, or graphic
- Related to eating, body image, illness, or lifestyle habits
- Subjective, unverified, or anecdotal in nature

This content may not be appropriate for individuals with eating disorders, anxiety, PTSD, or other mental health conditions. If you are sensitive to such material, we strongly advise against using this App.

4. Not Intended for Minors
You must be at least 18 years of age to use this App. By using the App, you represent and warrant that you meet this requirement. We do not knowingly collect or display content for users under 18.

5. Use at Your Own Risk
You acknowledge and agree that:
- Use of this App is entirely at your own risk.
- We are not responsible for how you interpret or respond to the content.
- We shall not be liable for any injury, emotional distress, mental harm, weight change, habit development, or life decisions made as a result of using this App.

6. No Guarantee of Accuracy or Truth
Content may be fictionalized, exaggerated, dramatized, symbolic, or speculative. We do not verify claims made in any video, nor do we guarantee that any result or outcome shown or implied is achievable, safe, or based in fact.

7. Content May Change Without Notice
We reserve the right to:
- Add, remove, or modify content at any time without notice.
- Show content that may be inconsistent, contradictory, or experimental in nature.

8. No Warranty
To the maximum extent permitted by law, the App is provided "as is" and "as available", without warranty of any kind, including:
- Fitness for a particular purpose
- Non-infringement
- Reliability or accuracy of the content

9. Limitation of Liability
To the fullest extent permitted by law:
- We shall not be liable for any direct, indirect, incidental, special, punitive, or consequential damages arising from your use or misuse of the App or reliance on its content.
- This includes, but is not limited to, damages related to health, mental state, lifestyle decisions, device functionality, or third-party issues.

10. Indemnification
You agree to defend, indemnify, and hold harmless Bunn, its creators, affiliates, employees, and partners from any claims, demands, losses, damages, liabilities, costs, and expenses (including legal fees) arising from your use of the App or violation of this Agreement.

11. No Guarantee of Benefit
We do not guarantee that this App will:
- Improve your life
- Prevent unhealthy behavior
- Deliver any specific wellness or lifestyle result

12. Jurisdiction & Governing Law
This Agreement shall be governed by the laws of your local jurisdiction. Any disputes shall be resolved exclusively in the competent courts of that jurisdiction.

13. Updates and Modifications
We may update this Agreement at any time without prior notice. Continued use of the App after changes constitutes acceptance of the new terms.

14. Contact
For questions or concerns about this agreement, please contact us at +493040007950.

By using Bunn, you acknowledge that you have read, understood, and agreed to all the terms in this User Agreement and Disclaimer.

""".trimIndent(),
                        style = MaterialTheme.typography.body1,
                        lineHeight = 20.sp,

                    )

                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                   // TextButton(onClick = onDismiss) { Text("Cancel") }
                  //  Spacer(Modifier.width(8.dp))
                    GreenRoundedButton(onClick = onAgree, text = "Agree")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
