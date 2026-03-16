package com.inversioncoach.app.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.inversioncoach.app.model.UserSettings
import com.inversioncoach.app.motion.DrillCatalog
import com.inversioncoach.app.storage.ServiceLocator
import com.inversioncoach.app.ui.common.computeSessionDurationMs
import com.inversioncoach.app.ui.common.formatSessionDateTime
import com.inversioncoach.app.ui.common.formatSessionDuration
import com.inversioncoach.app.ui.common.formatLimiterText
import com.inversioncoach.app.ui.components.ScaffoldedScreen
import kotlin.time.Duration.Companion.days

@Composable
fun HistoryScreen(onBack: () -> Unit, onOpenSession: (Long) -> Unit) {
    val context = LocalContext.current
    val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val repository = remember { ServiceLocator.repository(context) }
    val sessions by repository.observeSessions().collectAsState(initial = emptyList())
    val settings by repository.observeSettings().collectAsState(initial = UserSettings())
    val topIssue = sessions
        .flatMap { it.issues.split(",").map(String::trim).filter(String::isNotBlank) }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
        ?: "No consistent issue yet"

    val sessionSizes = remember { mutableStateMapOf<Long, Long>() }
    val categoryOptions = remember(sessions) {
        sessions.map { DrillCatalog.byType(it.drillType).category }
            .distinct()
            .sorted()
    }
    var selectedSort by remember { mutableStateOf(HistorySort.NEWEST) }
    var totalStorageBytes by remember { mutableLongStateOf(0L) }

    LaunchedEffect(sessions) {
        val sizes = sessions.associate { it.id to repository.sessionStorageBytes(it.id) }
        sessionSizes.clear()
        sessionSizes.putAll(sizes)
        totalStorageBytes = repository.totalStorageBytes()
    }

    val maxStorageBytes = settings.maxStorageMb.toLong() * 1024L * 1024L
    val sortedSessions = remember(sessions, selectedSort) {
        when (selectedSort) {
            HistorySort.NEWEST -> sessions.sortedByDescending { it.startedAtMs }
            HistorySort.CATEGORY -> sessions.sortedWith(
                compareBy(
                    { DrillCatalog.byType(it.drillType).category },
                    { it.drillType.displayName },
                    { -it.startedAtMs },
                ),
            )
        }
    }

    ScaffoldedScreen(title = "History", onBack = onBack) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Session insights", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Total sessions", "${sessions.size}", Modifier.weight(1f))
                MetricCard("With logged issues", "${sessions.count { it.issues.isNotBlank() }}", Modifier.weight(1f))
            }
            MetricCard("Top issue", topIssue, modifier = Modifier.fillMaxWidth())
            MetricCard(
                "Storage",
                "${formatMb(totalStorageBytes)} MB used • ${formatMb((maxStorageBytes - totalStorageBytes).coerceAtLeast(0L))} MB left",
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedSort == HistorySort.NEWEST,
                    onClick = { selectedSort = HistorySort.NEWEST },
                    label = { Text("Newest") },
                )
                FilterChip(
                    selected = selectedSort == HistorySort.CATEGORY,
                    onClick = { selectedSort = HistorySort.CATEGORY },
                    label = { Text("Category") },
                )
                if (selectedSort == HistorySort.CATEGORY) {
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "${categoryOptions.size} categories",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(sortedSessions) { session ->
                    val sizeMb = formatMb(sessionSizes[session.id] ?: 0L)
                    val status = videoStatus(session)
                    val progress = uploadProgress(status)
                    val retentionText = retentionTimeLeft(session.completedAtMs, settings.retainDays)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSession(session.id) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        ),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(session.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${session.drillType.displayName} • ${DrillCatalog.byType(session.drillType).category}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text("Time: ${formatSessionDateTime(session.startedAtMs)}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Duration: ${formatSessionDuration(computeSessionDurationMs(session.startedAtMs, session.completedAtMs))}")
                            Text("Limiter: ${formatLimiterText(session)}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Updated: ${formatSessionDateTime(session.completedAtMs)}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Video: $status", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                            Text("Storage: $sizeMb MB • Time left: $retentionText", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (isDebuggable && session.annotatedExportStatus.name == "FAILED") {
                                Text("Reason: ${session.annotatedExportFailureReason.orEmpty()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class HistorySort { NEWEST, CATEGORY }

private fun videoStatus(session: com.inversioncoach.app.model.SessionRecord): String = when {
    !session.annotatedVideoUri.isNullOrBlank() -> "Annotated ready"
    !session.rawVideoUri.isNullOrBlank() -> "Raw ready"
    session.annotatedExportStatus.name == "FAILED" -> "Processing failed"
    else -> "Processing"
}

private fun uploadProgress(status: String): Float = when (status) {
    "Annotated ready" -> 1f
    "Raw ready" -> 0.75f
    "Processing failed" -> 0.4f
    else -> 0.25f
}

private fun retentionTimeLeft(completedAtMs: Long, retainDays: Int): String {
    val expiresAt = completedAtMs + retainDays.days.inWholeMilliseconds
    val leftMs = expiresAt - System.currentTimeMillis()
    if (leftMs <= 0) return "expired"
    val leftDays = (leftMs / 86_400_000L).coerceAtLeast(0)
    return "$leftDays days"
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun formatMb(bytes: Long): String {
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    return "%.1f".format(mb)
}
