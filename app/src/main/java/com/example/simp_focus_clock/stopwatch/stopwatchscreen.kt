package com.example.simp_focus_clock.stopwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun StopwatchScreen(
    stopwatchViewModel: StopwatchViewModel
) {
    val elapsedMillis =
        stopwatchViewModel.elapsedMillis

    val isRunning =
        stopwatchViewModel.isRunning

    val laps =
        stopwatchViewModel.laps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "Stopwatch",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = formatStopwatchTime(
                        elapsedMillis
                    ),
                    style =
                        MaterialTheme.typography.displayLarge
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Button(
                onClick = {
                    stopwatchViewModel.start()
                },
                enabled = !isRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text =
                        if (elapsedMillis > 0L)
                            "RESUME"
                        else
                            "START"
                )
            }

            Button(
                onClick = {
                    stopwatchViewModel.pause()
                },
                enabled = isRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text("PAUSE")
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            OutlinedButton(
                onClick = {
                    stopwatchViewModel.addLap()
                },
                enabled = isRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text("LAP")
            }

            OutlinedButton(
                onClick = {
                    stopwatchViewModel.reset()
                },
                enabled =
                    elapsedMillis > 0L ||
                            laps.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("RESET")
            }
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        if (laps.isNotEmpty()) {

            Text(
                text = "Laps",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            laps.forEachIndexed {
                    index,
                    lapTime ->

                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                bottom = 8.dp
                            )
                ) {

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    16.dp
                                ),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            text =
                                "Lap ${index + 1}"
                        )

                        Text(
                            text =
                                formatStopwatchTime(
                                    lapTime
                                )
                        )
                    }
                }
            }
        }
    }
}

private fun formatStopwatchTime(
    millis: Long
): String {

    val totalSeconds =
        millis / 1000L

    val hours =
        totalSeconds / 3600L

    val minutes =
        (totalSeconds % 3600L) / 60L

    val seconds =
        totalSeconds % 60L

    val milliseconds =
        (millis % 1000L) / 10L

    return if (hours > 0L) {

        String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d.%02d",
            hours,
            minutes,
            seconds,
            milliseconds
        )

    } else {

        String.format(
            Locale.getDefault(),
            "%02d:%02d.%02d",
            minutes,
            seconds,
            milliseconds
        )
    }
}