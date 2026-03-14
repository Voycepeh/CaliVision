package com.inversioncoach.app.biomechanics

import com.inversioncoach.app.model.DrillType

class BackToWallAnalyzer(
    private val calibratedWallX: Float = 0.95f,
) : BaseDrillAnalyzer(DrillType.BACK_TO_WALL_HANDSTAND) {
    override fun wallX(): Float = calibratedWallX
}
