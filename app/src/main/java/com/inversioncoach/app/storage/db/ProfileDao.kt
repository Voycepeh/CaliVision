package com.inversioncoach.app.storage.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.inversioncoach.app.model.ProfileCalibrationEntity
import com.inversioncoach.app.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query(
        """
        SELECT p.*, CASE WHEN c.profileId IS NULL THEN 0 ELSE 1 END AS hasCalibration
        FROM user_profiles p
        LEFT JOIN profile_calibrations c ON c.profileId = p.id
        WHERE p.isArchived = 0
        ORDER BY p.createdAtMs ASC
        """,
    )
    fun observeProfiles(): Flow<List<UserProfileWithCalibration>>

    @Query(
        """
        SELECT p.*, CASE WHEN c.profileId IS NULL THEN 0 ELSE 1 END AS hasCalibration
        FROM user_profiles p
        LEFT JOIN profile_calibrations c ON c.profileId = p.id
        WHERE p.isArchived = 0 AND p.isActive = 1
        LIMIT 1
        """,
    )
    fun observeActiveProfile(): Flow<UserProfileWithCalibration?>

    @Query(
        """
        SELECT p.*, CASE WHEN c.profileId IS NULL THEN 0 ELSE 1 END AS hasCalibration
        FROM user_profiles p
        LEFT JOIN profile_calibrations c ON c.profileId = p.id
        WHERE p.isArchived = 0 AND p.isActive = 1
        LIMIT 1
        """,
    )
    suspend fun getActiveProfile(): UserProfileWithCalibration?

    @Query("SELECT * FROM user_profiles WHERE id = :profileId AND isArchived = 0 LIMIT 1")
    suspend fun getProfile(profileId: Long): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProfile(profile: UserProfileEntity): Long

    @Query("UPDATE user_profiles SET isActive = 0, updatedAtMs = :updatedAtMs WHERE isActive = 1")
    suspend fun clearActiveProfile(updatedAtMs: Long)

    @Query("UPDATE user_profiles SET isActive = 1, updatedAtMs = :updatedAtMs WHERE id = :profileId AND isArchived = 0")
    suspend fun activateProfile(profileId: Long, updatedAtMs: Long): Int

    @Transaction
    suspend fun setActiveProfile(profileId: Long, updatedAtMs: Long): Boolean {
        clearActiveProfile(updatedAtMs)
        return activateProfile(profileId, updatedAtMs) > 0
    }

    @Query("UPDATE user_profiles SET displayName = :displayName, updatedAtMs = :updatedAtMs WHERE id = :profileId AND isArchived = 0")
    suspend fun renameProfile(profileId: Long, displayName: String, updatedAtMs: Long): Int

    @Query("UPDATE user_profiles SET isArchived = 1, isActive = 0, updatedAtMs = :updatedAtMs WHERE id = :profileId")
    suspend fun archiveProfile(profileId: Long, updatedAtMs: Long): Int

    @Query("SELECT COUNT(*) FROM user_profiles WHERE isArchived = 0")
    suspend fun countActiveProfiles(): Int

    @Query("SELECT * FROM user_profiles WHERE isArchived = 0 ORDER BY createdAtMs ASC LIMIT 1")
    suspend fun getOldestUnarchivedProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCalibration(calibration: ProfileCalibrationEntity)

    @Query("DELETE FROM profile_calibrations WHERE profileId = :profileId")
    suspend fun deleteCalibration(profileId: Long)

    @Query("SELECT * FROM profile_calibrations WHERE profileId = :profileId LIMIT 1")
    fun observeCalibration(profileId: Long): Flow<ProfileCalibrationEntity?>

    @Query("SELECT * FROM profile_calibrations WHERE profileId = :profileId LIMIT 1")
    suspend fun getCalibration(profileId: Long): ProfileCalibrationEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM profile_calibrations WHERE profileId = :profileId)")
    suspend fun hasCalibration(profileId: Long): Boolean
}

data class UserProfileWithCalibration(
    val id: Long,
    val displayName: String,
    val isActive: Boolean,
    val isArchived: Boolean,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val hasCalibration: Boolean,
)
