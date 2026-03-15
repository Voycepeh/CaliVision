package com.inversioncoach.app.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inversioncoach.app.ui.common.formatSessionDuration
import com.inversioncoach.app.ui.components.ScaffoldedScreen

@Composable
fun SessionTooShortScreen(elapsedSessionMs: Long, minSessionDurationSeconds: Int, onBackToHome: () -> Unit) {
    ScaffoldedScreen(title = "Session not recorded") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Session is not recorded", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Session is shorter than the minimum session length.")
            Text("Your session duration: ${formatSessionDuration(elapsedSessionMs)}")
            Text("Minimum session length from settings: ${minSessionDurationSeconds}s")
            Button(onClick = onBackToHome, modifier = Modifier.fillMaxWidth()) {
                Text("Back to main app page")
            }
        }
    }
}
