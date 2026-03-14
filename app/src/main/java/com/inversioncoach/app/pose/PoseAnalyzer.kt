package com.inversioncoach.app.pose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.toBitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.inversioncoach.app.model.JointPoint
import com.inversioncoach.app.model.PoseFrame
import java.util.concurrent.TimeUnit
import java.util.concurrent.ExecutorService

class PoseAnalyzer(
    context: Context,
    private val onPoseFrame: (PoseFrame) -> Unit,
    private val onAnalyzerWarning: (String) -> Unit,
    private val backgroundExecutor: ExecutorService,
) : ImageAnalysis.Analyzer {
    private companion object {
        private const val TAG = "PoseAnalyzer"
        private const val MIN_VISIBLE_JOINT_CONFIDENCE = 0.25f
        private const val LOG_INTERVAL_MS = 2_000L
    }

    private val landmarkNames = listOf(
        "nose", "left_eye_inner", "left_eye", "left_eye_outer", "right_eye_inner", "right_eye", "right_eye_outer",
        "left_ear", "right_ear", "mouth_left", "mouth_right", "left_shoulder", "right_shoulder", "left_elbow",
        "right_elbow", "left_wrist", "right_wrist", "left_pinky", "right_pinky", "left_index", "right_index",
        "left_thumb", "right_thumb", "left_hip", "right_hip", "left_knee", "right_knee", "left_ankle", "right_ankle",
        "left_heel", "right_heel", "left_foot_index", "right_foot_index",
    )

    private var lastWarningAtMs = 0L
    private var lastPerfLogAtMs = 0L
    private var reusableBitmap: Bitmap? = null

    @Suppress("unused")
    private val poseLandmarker: PoseLandmarker by lazy {
        val baseOptions = BaseOptions.builder().setModelAssetPath("pose_landmarker_lite.task").build()
        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener(::onResult)
            .setErrorListener { _, _ -> onAnalyzerWarning("Pose model error. Reposition camera and retry.") }
            .build()
        PoseLandmarker.createFromOptions(context, options)
    }

    private fun onResult(result: PoseLandmarkerResult, inputImage: com.google.mediapipe.framework.image.MPImage) {
        val landmarks = result.landmarks().firstOrNull().orEmpty()
        val joints = landmarks.mapIndexedNotNull { index, lm ->
            val visibility = lm.visibility().orElse(0f)
            if (visibility < MIN_VISIBLE_JOINT_CONFIDENCE) return@mapIndexedNotNull null
            val jointName = landmarkNames.getOrElse(index) { "joint_$index" }
            JointPoint(jointName, lm.x(), lm.y(), lm.z(), visibility)
        }.toMutableList()

        val shoulder = joints.firstOrNull { it.name == "left_shoulder" }
        val hip = joints.firstOrNull { it.name == "left_hip" }
        if (shoulder != null && hip != null) {
            joints += JointPoint(
                name = "left_rib_proxy",
                x = (shoulder.x + hip.x) / 2f,
                y = (shoulder.y + hip.y) / 2f,
                z = (shoulder.z + hip.z) / 2f,
                visibility = minOf(shoulder.visibility, hip.visibility),
            )
        }

        val confidence = if (landmarks.isEmpty()) 0f else landmarks.map { it.visibility().orElse(0f) }.average().toFloat()
        val timestampMs = result.timestampMs()
        backgroundExecutor.execute {
            onPoseFrame(PoseFrame(timestampMs = timestampMs, joints = joints, confidence = confidence))
        }

        if (confidence < 0.45f) {
            throttleWarning("Low landmark confidence. Improve lighting and keep full body in side view.")
        }

        val now = System.currentTimeMillis()
        if (now - lastPerfLogAtMs >= LOG_INTERVAL_MS) {
            lastPerfLogAtMs = now
            Log.d(TAG, "Pose stream active: landmarks=${joints.size}, confidence=${"%.2f".format(confidence)}")
        }
    }

    override fun analyze(image: ImageProxy) {
        try {
            val sourceBitmap = image.toBitmap()
            val rotatedBitmap = sourceBitmap.rotate(image.imageInfo.rotationDegrees, reusableBitmap)
            reusableBitmap = rotatedBitmap
            val frameTimestampMs = TimeUnit.NANOSECONDS.toMillis(image.imageInfo.timestamp)
            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            poseLandmarker.detectAsync(mpImage, frameTimestampMs)
        } catch (t: Throwable) {
            Log.e(TAG, "Pose analysis frame failed", t)
            throttleWarning("Pose inference dropped a frame. Hold steady and retry.")
        } finally {
            image.close()
        }
    }

    private fun Bitmap.rotate(rotationDegrees: Int, reuseBuffer: Bitmap?): Bitmap {
        if (rotationDegrees == 0) return this
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotated = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        if (reuseBuffer == null || reuseBuffer == rotated) return rotated
        if (reuseBuffer != this && !reuseBuffer.isRecycled) reuseBuffer.recycle()
        return rotated
    }

    private fun throttleWarning(message: String) {
        val now = System.currentTimeMillis()
        if (now - lastWarningAtMs > 3000) {
            lastWarningAtMs = now
            onAnalyzerWarning(message)
        }
    }
}
