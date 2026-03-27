package com.inversioncoach.app.ui.calibration

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.inversioncoach.app.camera.CameraSessionManager
import com.inversioncoach.app.model.DrillType
import com.inversioncoach.app.model.SessionMode
import com.inversioncoach.app.overlay.DrillCameraSide
import com.inversioncoach.app.overlay.FreestyleViewMode
import com.inversioncoach.app.overlay.OverlayRenderer
import com.inversioncoach.app.pose.PoseAnalyzer
import com.inversioncoach.app.pose.PoseScaleMode
import com.inversioncoach.app.storage.ServiceLocator
import com.inversioncoach.app.ui.components.ScaffoldedScreen
import java.util.concurrent.Executors

@Composable
fun CalibrationScreen(drillType: DrillType, onBack: () -> Unit) {
    val context = LocalContext.current
    val vm = remember {
        CalibrationViewModel(
            drillType = drillType,
            calibrationProfileProvider = ServiceLocator.calibrationProfileProvider(context),
            drillMovementProfileRepository = ServiceLocator.drillMovementProfileRepository(context),
        )
    }
    val state by vm.state.collectAsState()
    val cameraManager = remember { CameraSessionManager(context) }
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember {
        PoseAnalyzer(
            onPoseFrame = vm::onPoseFrame,
            onAnalyzerWarning = { },
            backgroundExecutor = analyzerExecutor,
        )
    }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        onDispose {
            analyzer.close()
            analyzerExecutor.shutdown()
            cameraManager.release()
        }
    }

    ScaffoldedScreen(title = "Structural Calibration", onBack = onBack) { padding ->
        when (state.phase) {
            CalibrationPhase.INTRO -> CalibrationIntroScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                onStart = vm::beginCalibration,
            )

            CalibrationPhase.CAPTURING -> CalibrationCaptureContent(
                state = state,
                drillType = drillType,
                lifecycleOwner = lifecycleOwner,
                cameraManager = cameraManager,
                analyzer = analyzer,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                onExit = onBack,
            )

            CalibrationPhase.COMPLETED -> CalibrationCompleteScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                onDone = onBack,
            )
        }
    }
}

@Composable
private fun CalibrationCaptureContent(
    state: CalibrationUiState,
    drillType: DrillType,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraManager: CameraSessionManager,
    analyzer: PoseAnalyzer,
    modifier: Modifier,
    onExit: () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Step ${state.stepIndex}/${state.totalSteps}: ${state.title}", style = MaterialTheme.typography.titleMedium)
        Text(state.instruction)
        Text("Camera: ${state.cameraPlacement}")
        Icon(Icons.Default.Accessibility, contentDescription = null)

        LinearProgressIndicator(
            progress = { state.acceptedFrames / state.requiredFrames.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            strokeCap = StrokeCap.Round,
        )
        Text("Accepted frames: ${state.acceptedFrames}/${state.requiredFrames}")

        Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Readiness: ${if (state.isReady) "Ready" else "Not ready"}")
                Text(state.readinessMessage)
                if (state.missingRequiredJoints.isNotEmpty()) {
                    Text("Missing required joints: ${state.missingRequiredJoints.joinToString()}")
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        post {
                            cameraManager.bind(
                                lifecycleOwner = lifecycleOwner,
                                previewView = this,
                                analyzer = analyzer,
                                zoomOutCamera = true,
                            ) { _, _ -> }
                        }
                    }
                },
            )

            OverlayRenderer(
                frame = state.latestFrame,
                drillType = drillType,
                sessionMode = SessionMode.DRILL,
                modifier = Modifier.fillMaxSize(),
                scaleMode = PoseScaleMode.FILL,
                showIdealLine = false,
                showDebugOverlay = false,
                drillCameraSide = DrillCameraSide.LEFT,
                freestyleViewMode = FreestyleViewMode.UNKNOWN,
            )

            CalibrationGuideOverlay(
                modifier = Modifier.fillMaxSize(),
                state = state,
            )
        }

        state.stepResultMessage?.let { Text(it) }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
            Text("Exit calibration")
        }
    }
}

@Composable
private fun CalibrationGuideOverlay(modifier: Modifier, state: CalibrationUiState) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val margin = size.minDimension * 0.08f
            drawRect(
                color = if (state.isReady) Color(0xFF4CAF50) else Color(0xFFFFA000),
                topLeft = Offset(margin, margin),
                size = androidx.compose.ui.geometry.Size(size.width - margin * 2, size.height - margin * 2),
                style = Stroke(width = 4f),
            )

            val jointsByName = state.latestFrame?.joints?.associateBy { it.name }.orEmpty()
            val missing = state.missingRequiredJoints.toSet()
            state.requiredJointNames.forEach { name ->
                val p = jointsByName[name] ?: return@forEach
                drawCircle(
                    color = if (missing.contains(name)) Color.Red else Color(0xFF00E676),
                    radius = 8f,
                    center = Offset(p.x * size.width, p.y * size.height),
                )
            }
        }

        Text(
            text = if (state.isReady) "Ready" else "Adjust",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
