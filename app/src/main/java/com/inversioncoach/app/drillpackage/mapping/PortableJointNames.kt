package com.inversioncoach.app.drillpackage.mapping

import com.inversioncoach.app.motion.BodyJoint

object PortableJointNames {
    private val bodyJointToCanonical = mapOf(
        BodyJoint.HEAD to "head",
        BodyJoint.NECK to "neck",
        BodyJoint.LEFT_SHOULDER to "left_shoulder",
        BodyJoint.RIGHT_SHOULDER to "right_shoulder",
        BodyJoint.LEFT_ELBOW to "left_elbow",
        BodyJoint.RIGHT_ELBOW to "right_elbow",
        BodyJoint.LEFT_WRIST to "left_wrist",
        BodyJoint.RIGHT_WRIST to "right_wrist",
        BodyJoint.RIBCAGE to "ribcage",
        BodyJoint.PELVIS to "pelvis",
        BodyJoint.LEFT_HIP to "left_hip",
        BodyJoint.RIGHT_HIP to "right_hip",
        BodyJoint.LEFT_KNEE to "left_knee",
        BodyJoint.RIGHT_KNEE to "right_knee",
        BodyJoint.LEFT_ANKLE to "left_ankle",
        BodyJoint.RIGHT_ANKLE to "right_ankle",
    )

    private val aliasToCanonical = mapOf(
        "nose" to "head",
        "shoulder_left" to "left_shoulder",
        "shoulder_right" to "right_shoulder",
        "elbow_left" to "left_elbow",
        "elbow_right" to "right_elbow",
        "wrist_left" to "left_wrist",
        "wrist_right" to "right_wrist",
        "hip_left" to "left_hip",
        "hip_right" to "right_hip",
        "knee_left" to "left_knee",
        "knee_right" to "right_knee",
        "ankle_left" to "left_ankle",
        "ankle_right" to "right_ankle",
    )

    private val canonicalToBodyJoint = bodyJointToCanonical.entries.associate { it.value to it.key }

    fun fromBodyJoint(joint: BodyJoint): String = bodyJointToCanonical[joint] ?: joint.name.lowercase()

    fun canonicalize(name: String): String {
        val normalized = name.trim().lowercase()
        return aliasToCanonical[normalized] ?: normalized
    }

    fun toBodyJointOrNull(name: String): BodyJoint? = canonicalToBodyJoint[canonicalize(name)]
}
