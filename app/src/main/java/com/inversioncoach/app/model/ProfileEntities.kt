package com.inversioncoach.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_profiles",
    indices = [Index(value = ["displayName"], unique = true), Index(value = ["isActive"])],
)
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val isActive: Boolean,
    val isArchived: Boolean,
    val createdAtMs: Long,
    val updatedAtMs: Long,
)

@Entity(
    tableName = "profile_calibrations",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["profileId"], unique = true)],
)
data class ProfileCalibrationEntity(
    @PrimaryKey val profileId: Long,
    val profileVersion: Int,
    val updatedAtMs: Long,
    val calibrationPayloadJson: String,
    val appVersion: String? = null,
    val calibrationMethod: String? = null,
)
