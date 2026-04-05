package com.inversioncoach.app.drills.runtime

data class RuntimeDrillDefinition(
    val id: String,
    val name: String,
    val movementMode: String,
    val cameraView: String,
    val status: String,
    val phases: List<String>,
    val keyJoints: Set<String>,
    val normalizationBasis: String,
)
