package com.inversioncoach.app.drillpackage

import com.inversioncoach.app.drillpackage.io.DrillPackageJsonCodec
import com.inversioncoach.app.drillpackage.model.DrillManifest
import com.inversioncoach.app.drillpackage.model.DrillPackage
import com.inversioncoach.app.drillpackage.model.PortableDrill
import com.inversioncoach.app.drillpackage.model.PortablePhase
import com.inversioncoach.app.drillpackage.model.PortableViewType
import com.inversioncoach.app.drillpackage.model.SchemaVersion
import org.junit.Assert.assertEquals
import org.junit.Test

class DrillPackageJsonCodecTest {
    @Test
    fun encodesAndDecodesPackage() {
        val pkg = DrillPackage(
            manifest = DrillManifest("catalog-v1", SchemaVersion(1, 0), "android", 42L),
            drills = listOf(
                PortableDrill(
                    id = "push_up",
                    title = "Push-up",
                    description = "",
                    family = "push",
                    movementType = "REP",
                    cameraView = PortableViewType.SIDE,
                    supportedViews = listOf(PortableViewType.SIDE),
                    comparisonMode = "POSE_TIMELINE",
                    normalizationBasis = "HIPS",
                    keyJoints = listOf("left_shoulder"),
                    tags = listOf("seeded"),
                    phases = listOf(PortablePhase("setup", "Setup", 0)),
                    poses = emptyList(),
                    metricThresholds = emptyMap(),
                ),
            ),
        )

        val encoded = DrillPackageJsonCodec.encode(pkg)
        val decoded = DrillPackageJsonCodec.decode(encoded)

        assertEquals(pkg.manifest.packageId, decoded.manifest.packageId)
        assertEquals(pkg.manifest.schemaVersion.major, decoded.manifest.schemaVersion.major)
        assertEquals(pkg.drills.first().id, decoded.drills.first().id)
    }
}
