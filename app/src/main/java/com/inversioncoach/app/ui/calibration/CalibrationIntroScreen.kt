package com.inversioncoach.app.ui.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CalibrationIntroScreen(modifier: Modifier = Modifier, onStart: () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Structural Calibration", style = MaterialTheme.typography.headlineSmall)
        IntroCard("Use the back camera.")
        IntroCard("Place phone 2.5–4 m away.")
        IntroCard("Keep the camera upright and level.")
        IntroCard("Keep your full body visible.")
        IntroCard("For the wall handstand step, place the camera side-on.")
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text("Start calibration")
        }
    }
}

@Composable
private fun IntroCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(text = text, modifier = Modifier.padding(12.dp))
    }
}
