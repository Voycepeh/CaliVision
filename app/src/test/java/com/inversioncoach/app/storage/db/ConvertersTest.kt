package com.inversioncoach.app.storage.db

import com.inversioncoach.app.model.DrillType
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun drillTypeFromStringReadsCurrentAndLegacyValues() {
        assertEquals(DrillType.CHEST_TO_WALL_HANDSTAND, converters.drillTypeFromString("CHEST_TO_WALL_HANDSTAND"))
        assertEquals(DrillType.FREESTANDING_HANDSTAND_FUTURE, converters.drillTypeFromString("FREE_HANDSTAND_FUTURE"))
        assertEquals(DrillType.CHEST_TO_WALL_HANDSTAND, converters.drillTypeFromString("chest_to_wall_hanstand"))
    }
}
