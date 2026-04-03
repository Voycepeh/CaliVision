package com.inversioncoach.app.media

import android.net.Uri
import com.inversioncoach.app.model.SessionRecord
import java.io.File

object SessionMediaOwnership {
    private val rawInvalidFailureReasons = setOf(
        "RAW_REPLAY_INVALID",
        "RAW_MEDIA_CORRUPT",
        "SOURCE_VIDEO_UNREADABLE",
    )

    fun rawCandidates(session: SessionRecord): List<String> = listOfNotNull(
        session.rawFinalUri,
        session.rawVideoUri,
        session.rawMasterUri,
    ).filter { it.isNotBlank() }

    fun annotatedCandidates(session: SessionRecord): List<String> = listOfNotNull(
        session.annotatedFinalUri,
        session.annotatedVideoUri,
        session.annotatedMasterUri,
    ).filter { it.isNotBlank() }

    fun canonicalRawUri(session: SessionRecord): String? = rawCandidates(session).firstOrNull()

    fun canonicalAnnotatedUri(session: SessionRecord): String? = annotatedCandidates(session).firstOrNull()

    fun isRawReplayBlocked(session: SessionRecord): Boolean =
        session.rawPersistFailureReason in rawInvalidFailureReasons

    fun rawReplayPlayable(session: SessionRecord): Boolean {
        if (isRawReplayBlocked(session)) return false
        val rawUri = canonicalRawUri(session) ?: return false
        val bestPlayableUri = session.bestPlayableUri
        return bestPlayableUri.isNullOrBlank() || bestPlayableUri == rawUri
    }

    fun isOwnedAppFile(uri: String?, ownedRoots: List<File>): Boolean {
        if (uri.isNullOrBlank()) return false
        val path = runCatching { Uri.parse(uri).path }.getOrNull() ?: return false
        val file = File(path)
        val canonical = runCatching { file.canonicalFile }.getOrNull() ?: return false
        return ownedRoots.any { root ->
            val canonicalRoot = runCatching { root.canonicalFile }.getOrNull() ?: return@any false
            canonical.path == canonicalRoot.path || canonical.path.startsWith("${canonicalRoot.path}${File.separator}")
        }
    }
}
