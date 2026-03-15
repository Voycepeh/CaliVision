package com.inversioncoach.app.storage.db

import androidx.room.TypeConverter
import com.inversioncoach.app.model.CueStyle
import com.inversioncoach.app.model.DrillType

class Converters {
    @TypeConverter
    fun drillTypeFromString(raw: String): DrillType =
        DrillType.entries.firstOrNull { it.name == raw }
            ?: legacyDrillTypeMap[raw]
            ?: DrillType.CHEST_TO_WALL_HANDSTAND

    @TypeConverter
    fun drillTypeToString(value: DrillType): String = value.name

    @TypeConverter
    fun cueStyleFromString(raw: String): CueStyle = CueStyle.valueOf(raw)

    @TypeConverter
    fun cueStyleToString(value: CueStyle): String = value.name

    private companion object {
        val legacyDrillTypeMap = mapOf(
            "FREE_HANDSTAND_FUTURE" to DrillType.FREESTANDING_HANDSTAND_FUTURE,
            "chest_to_wall_hanstand" to DrillType.CHEST_TO_WALL_HANDSTAND,
            "CHEST_TO_WALL_HANSTAND" to DrillType.CHEST_TO_WALL_HANDSTAND,
        )
    }
}
