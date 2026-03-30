package com.inversioncoach.app.ui.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inversioncoach.app.calibration.BodyProfileStickmanGenerator
import com.inversioncoach.app.calibration.UserBodyProfile
import com.inversioncoach.app.model.DrillType
import com.inversioncoach.app.model.SessionMode
import com.inversioncoach.app.overlay.DrillCameraSide
import com.inversioncoach.app.overlay.FreestyleViewMode
import com.inversioncoach.app.overlay.OverlayRenderer
import com.inversioncoach.app.pose.PoseScaleMode
import java.text.DateFormat
import java.util.Date

@Composable
fun CalibrationCompleteScreen(
    modifier: Modifier = Modifier,
    profileSummary: String?,
    savedAtMs: Long?,
    profile: UserBodyProfile?,
    onDone: () -> Unit,
) {
    var previewMode by remember { mutableStateOf("front") }
    val generator = remember { BodyProfileStickmanGenerator() }
    val previewFrame = profile?.let {
        when (previewMode) {
            "side" -> generator.generateSide(it)
            "overhead" -> generator.generateOverhead(it)
            else -> generator.generateFront(it)
        }
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Body Profile saved", style = MaterialTheme.typography.headlineSmall)
        Text("Estimated body proportions were captured for a reusable body profile.")

        if (previewFrame != null) {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { previewMode = "front" }) { Text("Front") }
                OutlinedButton(onClick = { previewMode = "side" }) { Text("Side") }
                OutlinedButton(onClick = { previewMode = "overhead" }) { Text("Overhead") }
            }
            Box(modifier = Modifier.fillMaxWidth().height(260.dp).padding(vertical = 4.dp)) {
                OverlayRenderer(
                    frame = previewFrame,
                    drillType = DrillType.FREE_HANDSTAND,
                    sessionMode = SessionMode.DRILL,
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    showIdealLine = false,
                    drillCameraSide = DrillCameraSide.LEFT,
                    freestyleViewMode = FreestyleViewMode.UNKNOWN,
                    scaleMode = PoseScaleMode.FIT,
                )
            }
        }
        profileSummary?.let { Text("Key metrics: $it") }
        savedAtMs?.let { Text("Updated: ${DateFormat.getDateTimeInstance().format(Date(it))}") }

        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}
