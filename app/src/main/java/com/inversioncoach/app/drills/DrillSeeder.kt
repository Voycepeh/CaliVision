package com.inversioncoach.app.drills

import com.inversioncoach.app.model.CalibrationConfigRecord
import com.inversioncoach.app.model.DrillDefinitionRecord
import org.json.JSONObject

object DrillSeeder {
    fun seedDrills(nowMs: Long): List<DrillDefinitionRecord> = listOf(
        seededDrill(nowMs, id = "seed_push_up", name = "Push-Up", category = "Push", movementMode = DrillMovementMode.REP, cameraView = DrillCameraView.LEFT, description = "Standard bodyweight push pattern.", phaseSchema = "setup|lower|press", keyJoints = "shoulders|elbows|hips|ankles"),
        seededDrill(nowMs, id = "seed_bar_dip", name = "Bar Dip", category = "Push", movementMode = DrillMovementMode.REP, cameraView = DrillCameraView.LEFT, description = "Dip performed between parallel bars.", phaseSchema = "setup|lower|press", keyJoints = "shoulders|elbows|hips"),
        seededDrill(nowMs, id = "seed_pike_push_up", name = "Pike Push Up", category = "Push", movementMode = DrillMovementMode.REP, cameraView = DrillCameraView.LEFT, description = "Shoulder-dominant push with hips piked.", phaseSchema = "setup|lower|press", keyJoints = "shoulders|elbows|hips"),
        seededDrill(nowMs, id = "seed_elevated_pike_push_up", name = "Elevated Pike Push Up", category = "Push", movementMode = DrillMovementMode.REP, cameraView = DrillCameraView.LEFT, description = "Pike push-up variation with feet elevated.", phaseSchema = "setup|lower|press", keyJoints = "shoulders|elbows|hips"),
        seededDrill(nowMs, id = "seed_pull_up", name = "Pull-Up", category = "Pull", movementMode = DrillMovementMode.REP, cameraView = DrillCameraView.LEFT, description = "Vertical pulling strength movement.", phaseSchema = "setup|pull|lower", keyJoints = "shoulders|elbows|hips"),
        seededDrill(nowMs, id = "seed_inverted_row", name = "Inverted Row", category = "Pull", movementMode = DrillMovementMode.REP, cameraView = DrillCameraView.LEFT, description = "Horizontal bodyweight pulling movement.", phaseSchema = "setup|pull|lower", keyJoints = "shoulders|elbows|hips"),
        seededDrill(nowMs, id = "seed_bodyweight_squat", name = "Bodyweight Squat", category = "Legs", movementMode = DrillMovementMode.REP, cameraView = DrillCameraView.LEFT, description = "Foundational bilateral squat pattern.", phaseSchema = "setup|lower|stand", keyJoints = "hips|knees|ankles"),
        seededDrill(nowMs, id = "seed_forward_lunge", name = "Forward Lunge", category = "Legs", movementMode = DrillMovementMode.REP, cameraView = DrillCameraView.LEFT, description = "Alternating unilateral lunge pattern.", phaseSchema = "setup|step|drive", keyJoints = "hips|knees|ankles"),
        seededDrill(nowMs, id = "seed_pistol_squat", name = "Pistol Squat", category = "Legs", movementMode = DrillMovementMode.REP, cameraView = DrillCameraView.LEFT, description = "Single-leg squat progression.", phaseSchema = "setup|lower|stand", keyJoints = "hips|knees|ankles"),
        seededDrill(nowMs, id = "seed_front_plank", name = "Front Plank", category = "Core", movementMode = DrillMovementMode.HOLD, cameraView = DrillCameraView.LEFT, description = "Prone isometric trunk stabilization hold.", phaseSchema = "setup|brace|hold", keyJoints = "shoulders|hips|ankles"),
        seededDrill(nowMs, id = "seed_side_plank", name = "Side Plank", category = "Core", movementMode = DrillMovementMode.HOLD, cameraView = DrillCameraView.LEFT, description = "Lateral chain isometric stabilization hold.", phaseSchema = "setup|stack|hold", keyJoints = "shoulders|hips|ankles"),
        seededDrill(nowMs, id = "seed_leg_raise", name = "Leg Raise", category = "Core", movementMode = DrillMovementMode.REP, cameraView = DrillCameraView.LEFT, description = "Supine core movement emphasizing hip flexion.", phaseSchema = "setup|raise|lower", keyJoints = "hips|knees|ankles"),
        seededDrill(nowMs, id = "seed_hollow_hold", name = "Hollow Hold", category = "Core", movementMode = DrillMovementMode.HOLD, cameraView = DrillCameraView.LEFT, description = "Global anterior chain bracing hold.", phaseSchema = "setup|brace|hold", keyJoints = "shoulders|hips|ankles"),
        seededDrill(nowMs, id = "seed_free_handstand", name = "Handstand Hold", category = "Inversion", movementMode = DrillMovementMode.HOLD, cameraView = DrillCameraView.FREESTYLE, description = "Freestanding handstand hold progression.", phaseSchema = "setup|stack|hold", keyJoints = "shoulders|hips|ankles", legacyType = "FREE_HANDSTAND"),
        seededDrill(nowMs, id = "seed_wall_handstand", name = "Handstand Push Up", category = "Inversion", movementMode = DrillMovementMode.REP, cameraView = DrillCameraView.LEFT, description = "Handstand push-up strength progression.", phaseSchema = "setup|lower|press", keyJoints = "shoulders|elbows|hips", legacyType = "WALL_HANDSTAND"),
    )

    private fun seededDrill(
        nowMs: Long,
        id: String,
        name: String,
        category: String,
        movementMode: String,
        cameraView: String,
        description: String,
        phaseSchema: String,
        keyJoints: String,
        legacyType: String? = null,
    ): DrillDefinitionRecord {
        val legacyToken = legacyType?.let { "legacyDrillType:$it|" }.orEmpty()
        return DrillDefinitionRecord(
            id = id,
            name = name,
            description = description,
            movementMode = movementMode,
            cameraView = cameraView,
            phaseSchemaJson = phaseSchema,
            keyJointsJson = keyJoints,
            normalizationBasisJson = "hips",
            cueConfigJson = "${legacyToken}seedKey:$id|seedCategory:$category|comparisonMode:POSE_TIMELINE",
            sourceType = DrillSourceType.SEEDED,
            status = DrillStatus.READY,
            version = 2,
            createdAtMs = nowMs,
            updatedAtMs = nowMs,
        )
    }

    fun seedCalibration(nowMs: Long): List<CalibrationConfigRecord> = listOf(
        CalibrationConfigRecord(
            id = "seed_calibration_free_handstand",
            drillId = "seed_free_handstand",
            displayName = "Default Free Handstand Calibration",
            configJson = JSONObject().apply {
                put("angleThreshold", 12)
                put("stabilityTolerance", 0.08)
                put("scoreWeightAlignment", 0.35)
                put("scoreWeightTiming", 0.4)
                put("scoreWeightStability", 0.25)
            }.toString(),
            scoringVersion = 1,
            featureVersion = 1,
            isActive = true,
            createdAtMs = nowMs,
            updatedAtMs = nowMs,
        ),
        CalibrationConfigRecord(
            id = "seed_calibration_wall_handstand",
            drillId = "seed_wall_handstand",
            displayName = "Default Wall Handstand Calibration",
            configJson = JSONObject().apply {
                put("angleThreshold", 10)
                put("stabilityTolerance", 0.06)
                put("scoreWeightAlignment", 0.35)
                put("scoreWeightTiming", 0.4)
                put("scoreWeightStability", 0.25)
            }.toString(),
            scoringVersion = 1,
            featureVersion = 1,
            isActive = true,
            createdAtMs = nowMs,
            updatedAtMs = nowMs,
        ),
    )
}

object DrillSeedSynchronizer {
    fun reconcile(
        existing: List<DrillDefinitionRecord>,
        seededCatalog: List<DrillDefinitionRecord>,
        nowMs: Long,
    ): List<DrillDefinitionRecord> {
        val existingById = existing.associateBy { it.id }
        return seededCatalog.mapNotNull { seeded ->
            val current = existingById[seeded.id]
            when {
                current == null -> seeded
                current.sourceType != DrillSourceType.SEEDED -> null
                current.version >= seeded.version && current.name == seeded.name && current.description == seeded.description &&
                    current.movementMode == seeded.movementMode && current.cameraView == seeded.cameraView &&
                    current.phaseSchemaJson == seeded.phaseSchemaJson && current.keyJointsJson == seeded.keyJointsJson &&
                    current.normalizationBasisJson == seeded.normalizationBasisJson && current.cueConfigJson == seeded.cueConfigJson &&
                    current.status == seeded.status -> null
                else -> seeded.copy(createdAtMs = current.createdAtMs, updatedAtMs = nowMs)
            }
        }
    }
}
