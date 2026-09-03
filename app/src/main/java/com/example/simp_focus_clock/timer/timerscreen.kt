package com.example.simp_focus_clock.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TimerScreen(
    timerViewModel: TimerViewModel
) {

    val remainingMillis =
        timerViewModel.remainingMillis

    val isRunning =
        timerViewModel.isRunning

    val isPaused =
        timerViewModel.isPaused

    var showCustomDialog by remember {
        mutableStateOf(false)
    }

    val totalSeconds =
        remainingMillis / 1000L

    val displayMinutes =
        totalSeconds / 60L

    val displaySeconds =
        totalSeconds % 60L

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
            text = "Timer",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Card(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Column(
                modifier =
                    Modifier.padding(24.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text =
                        String.format(
                            "%02d:%02d",
                            displayMinutes,
                            displaySeconds
                        ),
                    style =
                        MaterialTheme.typography.displayLarge
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        /*
         * START / RESUME
         */
        Button(
            onClick = {

                if (isPaused) {
                    timerViewModel.resume()
                } else {
                    timerViewModel.start()
                }
            },

            enabled =
                !isRunning &&
                        remainingMillis > 0L,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    if (isPaused) {
                        "RESUME"
                    } else {
                        "START"
                    }
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        /*
         * PAUSE
         */
        Button(
            onClick = {
                timerViewModel.pause()
            },

            enabled = isRunning,

            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text("PAUSE")
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        /*
         * RESET
         */
        OutlinedButton(
            onClick = {
                timerViewModel.reset()
            },

            enabled =
                remainingMillis > 0L ||
                        isRunning ||
                        isPaused,

            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text("RESET")
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Text(
            text = "Presets",
            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /*
         * PRESET ROW 1
         */
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceEvenly
        ) {

            PresetButton(
                text = "1 min",
                enabled = !isRunning,
                onClick = {
                    timerViewModel.setTime(1)
                }
            )

            PresetButton(
                text = "5 min",
                enabled = !isRunning,
                onClick = {
                    timerViewModel.setTime(5)
                }
            )

            PresetButton(
                text = "10 min",
                enabled = !isRunning,
                onClick = {
                    timerViewModel.setTime(10)
                }
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        /*
         * PRESET ROW 2
         */
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceEvenly
        ) {

            PresetButton(
                text = "25 min",
                enabled = !isRunning,
                onClick = {
                    timerViewModel.setTime(25)
                }
            )

            PresetButton(
                text = "30 min",
                enabled = !isRunning,
                onClick = {
                    timerViewModel.setTime(30)
                }
            )

            PresetButton(
                text = "60 min",
                enabled = !isRunning,
                onClick = {
                    timerViewModel.setTime(60)
                }
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /*
         * CUSTOM TIME
         */
        OutlinedButton(
            onClick = {
                showCustomDialog = true
            },

            enabled = !isRunning,

            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text("CUSTOM TIME")
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )
    }

    /*
     * CUSTOM TIME DIALOG
     */
    if (showCustomDialog) {

        CustomTimeDialog(

            onDismiss = {
                showCustomDialog = false
            },

            onSetTime = {
                    hours,
                    customMinutes,
                    customSeconds ->

                timerViewModel.setTime(
                    minutes =
                        hours * 60 +
                                customMinutes,

                    seconds =
                        customSeconds
                )

                showCustomDialog = false
            }
        )
    }
}


/*
 * PRESET BUTTON
 */
@Composable
private fun PresetButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier =
            Modifier.width(105.dp)
    ) {
        Text(text)
    }
}


/*
 * CUSTOM TIME DIALOG
 */
@Composable
private fun CustomTimeDialog(
    onDismiss: () -> Unit,
    onSetTime: (
        Int,
        Int,
        Int
    ) -> Unit
) {

    var hoursText by remember {
        mutableStateOf("")
    }

    var minutesText by remember {
        mutableStateOf("")
    }

    var secondsText by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("Custom Timer")
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text =
                        "Set your own timer duration."
                )

                OutlinedTextField(

                    value = hoursText,

                    onValueChange = { value ->

                        if (
                            value.all {
                                it.isDigit()
                            } &&
                            value.length <= 3
                        ) {
                            hoursText = value
                            errorMessage = null
                        }
                    },

                    label = {
                        Text("Hours")
                    },

                    singleLine = true,

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),

                    modifier =
                        Modifier.fillMaxWidth()
                )

                OutlinedTextField(

                    value = minutesText,

                    onValueChange = { value ->

                        if (
                            value.all {
                                it.isDigit()
                            } &&
                            value.length <= 2
                        ) {
                            minutesText = value
                            errorMessage = null
                        }
                    },

                    label = {
                        Text("Minutes")
                    },

                    singleLine = true,

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),

                    modifier =
                        Modifier.fillMaxWidth()
                )

                OutlinedTextField(

                    value = secondsText,

                    onValueChange = { value ->

                        if (
                            value.all {
                                it.isDigit()
                            } &&
                            value.length <= 2
                        ) {
                            secondsText = value
                            errorMessage = null
                        }
                    },

                    label = {
                        Text("Seconds")
                    },

                    singleLine = true,

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),

                    modifier =
                        Modifier.fillMaxWidth()
                )

                if (
                    errorMessage != null
                ) {

                    Text(
                        text =
                            errorMessage!!,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("CANCEL")
            }
        },

        confirmButton = {

            TextButton(

                onClick = {

                    val hours =
                        hoursText
                            .toIntOrNull()
                            ?: 0

                    val customMinutes =
                        minutesText
                            .toIntOrNull()
                            ?: 0

                    val customSeconds =
                        secondsText
                            .toIntOrNull()
                            ?: 0

                    when {

                        customMinutes > 59 -> {

                            errorMessage =
                                "Minutes must be between 0 and 59."
                        }

                        customSeconds > 59 -> {

                            errorMessage =
                                "Seconds must be between 0 and 59."
                        }

                        hours == 0 &&
                                customMinutes == 0 &&
                                customSeconds == 0 -> {

                            errorMessage =
                                "Please enter a time greater than zero."
                        }

                        else -> {

                            onSetTime(
                                hours,
                                customMinutes,
                                customSeconds
                            )
                        }
                    }
                }
            ) {
                Text("SET TIMER")
            }
        }
    )
}