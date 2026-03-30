package com.inversioncoach.app.ui.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inversioncoach.app.calibration.CalibrationCapture
import com.inversioncoach.app.calibration.CalibrationProfileProvider
import com.inversioncoach.app.calibration.CalibrationReadinessEvaluator
import com.inversioncoach.app.calibration.CalibrationSession
import com.inversioncoach.app.calibration.CalibrationStep
import com.inversioncoach.app.calibration.DrillMovementProfileRepository
import com.inversioncoach.app.calibration.StructuralCalibrationEngine
import com.inversioncoach.app.calibration.hold.HoldTemplateBlender
import com.inversioncoach.app.calibration.hold.HoldTemplateBuilder
import com.inversioncoach.app.model.DrillType
import com.inversioncoach.app.model.PoseFrame
import com.inversioncoach.app.model.SmoothedPoseFrame
import com.inversioncoach.app.storage.repository.SessionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class CalibrationViewModel(
    private val referenceDrillType: DrillType,
    private val calibrationProfileProvider: CalibrationProfileProvider,
    private val drillMovementProfileRepository: DrillMovementProfileRepository,
    private val repository: SessionRepository,
    private val engine: StructuralCalibrationEngine = StructuralCalibrationEngine(),
    private val holdTemplateBuilder: HoldTemplateBuilder = HoldTemplateBuilder(),
    private val holdTemplateBlender: HoldTemplateBlender = HoldTemplateBlender(),
) : ViewModel() {

    private val steps = listOf(
        CalibrationStep.FRONT_NEUTRAL,
        CalibrationStep.SIDE_NEUTRAL,
        CalibrationStep.ARMS_OVERHEAD,
    )

    private val session = CalibrationSession(drillType = referenceDrillType)
    private val readinessEvaluator = CalibrationReadinessEvaluator()
    private val _state = MutableStateFlow(CalibrationUiState())
    val state: StateFlow<CalibrationUiState> = _state.asStateFlow()

    private var stepStartedAtMs: Long = 0L
    private var rejectedForStep = 0
    private var previousFrame: PoseFrame? = null
    private var latestRawFrame: PoseFrame? = null
    private val acceptedFramesForStep = mutableListOf<PoseFrame>()

    fun beginCalibration() = startStep(0)

    fun onPoseFrame(frame: PoseFrame) {
        val current = _state.value
        if (current.phase != CalibrationPhase.CAPTURING) return

        latestRawFrame = frame
        val readiness = readinessEvaluator.evaluate(current.currentStep, frame)
        val stillEnough = isStillEnough(frame)
        val ready = readiness.usable && stillEnough

        _state.update {
            it.copy(
                latestFrame = frame.toSmoothedPoseFrame(),
                visibleJointCount = readiness.visibleJointCount,
                isReady = ready,
                missingRequiredJoints = readiness.missingRequiredJoints,
                readinessMessage = if (!stillEnough) "Hold still" else readiness.status,
                errorMessage = null,
            )
        }

        previousFrame = frame
        if (!ready) rejectedForStep += 1
    }

    fun captureStep() {
        val current = _state.value
        if (current.phase != CalibrationPhase.CAPTURING) return
        val frame = latestRawFrame
        if (!current.isReady || frame == null) {
            _state.update { it.copy(errorMessage = "Not ready yet. Adjust position first.") }
            return
        }
        acceptedFramesForStep += frame
        _state.update {
            it.copy(
                acceptedFrames = acceptedFramesForStep.size,
                hasCapturedFrame = acceptedFramesForStep.size >= it.requiredFrames,
                capturedFrame = acceptedFramesForStep.lastOrNull()?.toSmoothedPoseFrame(),
                stepResultMessage = "Captured ${acceptedFramesForStep.size}/${it.requiredFrames} quality frames",
                errorMessage = null,
            )
        }
    }

    fun retakeStep() {
        acceptedFramesForStep.clear()
        _state.update {
            it.copy(
                capturedFrame = null,
                hasCapturedFrame = false,
                acceptedFrames = 0,
                stepResultMessage = "Retake ready. Capture stable frames.",
                errorMessage = null,
            )
        }
    }

    fun continueToNextStep() {
        val current = _state.value
        if (current.phase != CalibrationPhase.CAPTURING) return
        if (acceptedFramesForStep.size < current.requiredFrames) {
            _state.update { it.copy(errorMessage = "Capture more stable frames before continuing.") }
            return
        }
        completeCurrentStep()
    }

    private fun completeCurrentStep() {
        val step = _state.value.currentStep
        session.record(
            CalibrationCapture(
                step = step,
                startedAtMs = stepStartedAtMs,
                completedAtMs = System.currentTimeMillis(),
                acceptedFrames = acceptedFramesForStep.toList(),
                rejectedFrameCount = rejectedForStep,
            ),
        )

        val currentIndex = steps.indexOf(step)
        if (currentIndex >= steps.lastIndex) {
            completeCalibration()
            return
        }

        _state.update {
            it.copy(
                stepResultMessage = "Step ${currentIndex + 1} complete.",
                completedSteps = it.completedSteps + step,
                isReady = false,
            )
        }

        viewModelScope.launch {
            delay(250)
            startStep(currentIndex + 1)
        }
    }

    fun completeCalibration() {
        viewModelScope.launch {
            val builtProfile = engine.buildProfile(session)
            if (builtProfile == null) {
                _state.update { it.copy(phase = CalibrationPhase.CAPTURING, errorMessage = "Body profile capture incomplete.") }
                return@launch
            }

            val existing = calibrationProfileProvider.resolve(referenceDrillType)
            val nextVersion = existing.profileVersion + 1
            val updatedAtMs = System.currentTimeMillis()
            val newProfile = existing.copy(
                profileVersion = nextVersion,
                userBodyProfile = builtProfile,
                updatedAtMs = updatedAtMs,
            )
            repository.saveCalibrationForActiveProfile(builtProfile)
            calibrationProfileProvider.save(newProfile)
            drillMovementProfileRepository.save(newProfile)

            _state.update {
                it.copy(
                    phase = CalibrationPhase.COMPLETED,
                    stepResultMessage = "Body Profile saved",
                    completedSteps = steps.toSet(),
                    savedProfileSummary = summarizeProfile(builtProfile),
                    savedAtMs = updatedAtMs,
                    savedProfile = builtProfile,
                    errorMessage = null,
                )
            }
        }
    }

    private data class StepCopy(val title: String, val instruction: String, val cameraPlacement: String)

    private fun copyFor(step: CalibrationStep): StepCopy = when (step) {
        CalibrationStep.FRONT_NEUTRAL -> StepCopy("Front Neutral", "Face camera, arms relaxed, full body visible.", "Back camera, whole body in frame.")
        CalibrationStep.SIDE_NEUTRAL -> StepCopy("Side Neutral", "Turn to side, stay upright and still.", "Head to feet fully visible.")
        CalibrationStep.ARMS_OVERHEAD -> StepCopy("Front Arms Overhead", "Face camera and raise both arms overhead.", "Keep wrists and ankles visible.")
        CalibrationStep.CONTROLLED_HOLD -> StepCopy("Controlled Hold", "Hold steady.", "Maintain full-body framing.")
    }

    private fun startStep(index: Int) {
        val step = steps[index]
        val copy = copyFor(step)
        rejectedForStep = 0
        previousFrame = null
        latestRawFrame = null
        acceptedFramesForStep.clear()
        stepStartedAtMs = System.currentTimeMillis()
        _state.value = _state.value.copy(
            phase = CalibrationPhase.CAPTURING,
            currentStep = step,
            stepIndex = index + 1,
            totalSteps = steps.size,
            title = copy.title,
            instruction = copy.instruction,
            cameraPlacement = copy.cameraPlacement,
            acceptedFrames = 0,
            requiredFrames = 6,
            isReady = false,
            readinessMessage = "Get into position...",
            missingRequiredJoints = emptyList(),
            requiredJointNames = readinessEvaluator.requiredJointNames(step),
            latestFrame = null,
            capturedFrame = null,
            hasCapturedFrame = false,
            stepResultMessage = null,
            errorMessage = null,
        )
    }

    private fun isStillEnough(frame: PoseFrame): Boolean {
        val previous = previousFrame ?: return true
        val previousNose = previous.joints.firstOrNull { it.name == "nose" }
        val currentNose = frame.joints.firstOrNull { it.name == "nose" }
        if (previousNose == null || currentNose == null) return true
        val movement = abs(previousNose.x - currentNose.x) + abs(previousNose.y - currentNose.y)
        return movement < 0.035f
    }

    private fun summarizeProfile(profile: com.inversioncoach.app.calibration.UserBodyProfile): String {
        val shoulder = (profile.segmentRatios.shoulderToTorso * 100).roundToInt()
        val arm = (profile.segmentRatios.armToTorso * 100).roundToInt()
        val symmetry = (profile.leftRightConsistency * 100).roundToInt()
        return "Shoulders ${shoulder}% torso • Arm ${arm}% torso • Symmetry $symmetry%"
    }

    private fun PoseFrame.toSmoothedPoseFrame(): SmoothedPoseFrame = SmoothedPoseFrame(
        timestampMs = timestampMs,
        joints = joints,
        confidence = confidence,
        analysisWidth = analysisWidth,
        analysisHeight = analysisHeight,
        analysisRotationDegrees = analysisRotationDegrees,
        mirrored = mirrored,
    )
}
