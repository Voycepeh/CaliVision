package com.inversioncoach.app.motion

import com.inversioncoach.app.model.AlignmentStrictness

data class AlignmentTolerancePolicy(
    val lineDeviationNormMax: Float,
)

object AlignmentPolicy {
    fun forStrictness(strictness: AlignmentStrictness): AlignmentTolerancePolicy = when (strictness) {
        AlignmentStrictness.EASY -> AlignmentTolerancePolicy(lineDeviationNormMax = 0.17f)
        AlignmentStrictness.STANDARD -> AlignmentTolerancePolicy(lineDeviationNormMax = 0.13f)
        AlignmentStrictness.STRICT -> AlignmentTolerancePolicy(lineDeviationNormMax = 0.10f)
    }
}
