package com.inversioncoach.app.ui.calibration

import com.inversioncoach.app.calibration.CalibrationStep
import com.inversioncoach.app.model.SmoothedPoseFrame

enum class CalibrationPhase {
    INTRO,
    CAPTURING,
    COMPLETED,
}

data class CalibrationUiState(
    val phase: CalibrationPhase = CalibrationPhase.INTRO,
    val currentStep: CalibrationStep = CalibrationStep.FRONT_NEUTRAL,
    val stepIndex: Int = 1,
    val totalSteps: Int = 4,
    val title: String = "Front Neutral",
    val instruction: String = "Stand facing the camera with arms relaxed.",
    val cameraPlacement: String = "Back camera, full body visible, camera at hip-to-chest height.",
    val acceptedFrames: Int = 0,
    val requiredFrames: Int = 20,
    val isReady: Boolean = false,
    val readinessMessage: String = "Position yourself fully in frame.",
    val requiredJointNames: List<String> = emptyList(),
    val missingRequiredJoints: List<String> = emptyList(),
    val isCapturing: Boolean = false,
    val isComplete: Boolean = false,
    val errorMessage: String? = null,
    val stepResultMessage: String? = null,
    val visibleJointCount: Int = 0,
    val latestFrame: SmoothedPoseFrame? = null,
)
