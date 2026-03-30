package com.inversioncoach.app.movementprofile

import com.inversioncoach.app.model.ReferenceTemplateRecord
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ReferenceTemplateBuilder {
    fun buildFromSingleReference(
        drillId: String,
        displayName: String,
        sourceProfileId: String,
        snapshot: StoredProfileSnapshot,
        createdAtMs: Long = System.currentTimeMillis(),
        sourceType: String = "REFERENCE_UPLOAD",
        sourceSessionId: Long? = null,
        isBaseline: Boolean = false,
    ): ReferenceTemplateRecord {
        val phases = snapshot.phaseDurationsMs.entries.toList()
        val phasePosesJson = JSONObject().apply {
            put(
                "phases",
                JSONArray().apply {
                    phases.forEachIndexed { index, (phaseId, durationMs) ->
                        put(
                            JSONObject().apply {
                                put("phaseId", phaseId)
                                put("sequenceIndex", index)
                                put("durationMs", durationMs)
                                put("targetFeatures", JSONObject(snapshot.featureMeans.mapValues { it.value }))
                                put("stability", JSONObject(snapshot.stabilityJitter.mapValues { it.value }))
                            },
                        )
                    }
                },
            )
        }.toString()
        val keyframesJson = JSONObject().apply {
            put(
                "keyframes",
                JSONArray().apply {
                    phases.forEachIndexed { index, (phaseId, _) ->
                        val progress = if (phases.size <= 1) 0f else index.toFloat() / (phases.lastIndex.toFloat())
                        put(
                            JSONObject().apply {
                                put("phaseId", phaseId)
                                put("progress", progress)
                                put("metrics", JSONObject(snapshot.featureMeans.mapValues { it.value }))
                            },
                        )
                    }
                },
            )
        }.toString()
        return ReferenceTemplateRecord(
            id = "template-${UUID.randomUUID()}",
            drillId = drillId,
            displayName = displayName,
            templateType = "SINGLE_REFERENCE",
            sourceType = sourceType,
            sourceSessionId = sourceSessionId,
            title = displayName,
            phasePosesJson = phasePosesJson,
            keyframesJson = keyframesJson,
            fpsHint = null,
            durationMs = snapshot.phaseDurationsMs.values.sum().takeIf { it > 0L },
            updatedAtMs = createdAtMs,
            isBaseline = isBaseline,
            sourceProfileIdsJson = sourceProfileId,
            checkpointJson = JSONObject().apply {
                put("phaseTimingsMs", JSONObject(snapshot.phaseDurationsMs.mapValues { it.value }))
            }.toString(),
            toleranceJson = JSONObject().apply {
                put("featureMeans", JSONObject(snapshot.featureMeans.mapValues { it.value }))
                put("stabilityJitter", JSONObject(snapshot.stabilityJitter.mapValues { it.value }))
            }.toString(),
            createdAtMs = createdAtMs,
        )
    }
}
