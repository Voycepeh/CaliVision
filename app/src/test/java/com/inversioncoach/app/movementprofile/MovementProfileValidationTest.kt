package com.inversioncoach.app.movementprofile

import org.junit.Assert.assertTrue
import org.junit.Test

class MovementProfileValidationTest {
    @Test
    fun repProfileRequiresRepRule() {
        val profile = MovementProfile(
            id = "",
            displayName = "",
            drillType = null,
            movementType = MovementType.REP,
            allowedViews = setOf(CameraViewConstraint.ANY),
            phaseDefinitions = emptyList(),
            alignmentRules = emptyList(),
            readinessRule = ReadinessRule(0.3f, emptySet(), 4, sideViewPrimary = false),
            keyJoints = emptySet(),
        )
        val errors = profile.validate()
        assertTrue(errors.any { it.contains("rep movement requires repRule") })
        assertTrue(errors.any { it.contains("profile.id is required") })
    }
}
