package com.example.simp_focus_clock.alarm

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun AlarmScreen(
    alarmViewModel: AlarmViewModel
) {

    val alarms = alarmViewModel.alarms

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Alarm",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = {
                showAddDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ADD ALARM")
        }

        if (alarms.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No alarms set.",
                    modifier = Modifier.padding(16.dp)
                )
            }

        } else {

            alarms.forEach { alarm ->

                AlarmCard(
                    alarm = alarm,
                    onToggle = {
                        alarmViewModel.toggleAlarm(
                            alarm.id
                        )
                    },
                    onDelete = {
                        alarmViewModel.deleteAlarm(
                            alarm.id
                        )
                    }
                )
            }
        }
    }

    if (showAddDialog) {

        AddAlarmDialog(
            alarmViewModel = alarmViewModel,
            onDismiss = {
                showAddDialog = false
            },
            onAlarmAdded = {
                showAddDialog = false
            }
        )
    }
}


/* =========================================================
   ALARM CARD
   ========================================================= */

@Composable
private fun AlarmCard(
    alarm: AlarmItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = formatAlarmTime(
                            alarm.hour,
                            alarm.minute
                        ),
                        style =
                            MaterialTheme
                                .typography
                                .headlineMedium
                    )

                    Text(
                        text = alarm.label,
                        style =
                            MaterialTheme
                                .typography
                                .bodyLarge
                    )

                    Text(
                        text = formatRepeatDays(
                            alarm.repeatDays
                        ),
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                    )
                }

                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = {
                        onToggle()
                    }
                )
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("DELETE")
            }
        }
    }
}


/* =========================================================
   ADD ALARM DIALOG
   ========================================================= */

@Composable
private fun AddAlarmDialog(
    alarmViewModel: AlarmViewModel,
    onDismiss: () -> Unit,
    onAlarmAdded: () -> Unit
) {

    val context = LocalContext.current

    var selectedHour by remember {
        mutableStateOf(
            alarmViewModel.selectedHour
        )
    }

    var selectedMinute by remember {
        mutableStateOf(
            alarmViewModel.selectedMinute
        )
    }

    var labelText by remember {
        mutableStateOf(
            alarmViewModel.alarmLabel
        )
    }

    var repeatMode by remember {
        mutableStateOf("Once")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }


    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("Set Alarm")
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Alarm time",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )


                /*
                 * CLOCK TIME BUTTON
                 */
                OutlinedButton(

                    onClick = {

                        TimePickerDialog(

                            context,

                            { _, hour, minute ->

                                selectedHour = hour
                                selectedMinute = minute
                            },

                            selectedHour,
                            selectedMinute,
                            false

                        ).show()
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = formatAlarmTime(
                            selectedHour,
                            selectedMinute
                        ),

                        style =
                            MaterialTheme
                                .typography
                                .titleLarge
                    )
                }


                Text(
                    text = "Tap the time to choose your alarm.",
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )


                Text(
                    text = "Repeat",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )


                RepeatOption(
                    text = "Once",
                    selected =
                        repeatMode == "Once",
                    onClick = {

                        repeatMode = "Once"

                        alarmViewModel.setOnce()
                    }
                )


                RepeatOption(
                    text = "Every day",
                    selected =
                        repeatMode == "Every day",
                    onClick = {

                        repeatMode = "Every day"

                        alarmViewModel.setEveryDay()
                    }
                )


                RepeatOption(
                    text = "Weekdays",
                    selected =
                        repeatMode == "Weekdays",
                    onClick = {

                        repeatMode = "Weekdays"

                        alarmViewModel.setWeekdays()
                    }
                )


                RepeatOption(
                    text = "Weekends",
                    selected =
                        repeatMode == "Weekends",
                    onClick = {

                        repeatMode = "Weekends"

                        alarmViewModel.setWeekends()
                    }
                )


                RepeatOption(
                    text = "Custom",
                    selected =
                        repeatMode == "Custom",
                    onClick = {

                        repeatMode = "Custom"
                    }
                )


                if (repeatMode == "Custom") {

                    CustomDays(
                        alarmViewModel =
                            alarmViewModel
                    )
                }


                OutlinedTextField(

                    value = labelText,

                    onValueChange = {
                        labelText = it
                    },

                    label = {
                        Text("Label")
                    },

                    singleLine = true,

                    modifier =
                        Modifier.fillMaxWidth()
                )


                if (errorMessage != null) {

                    Text(
                        text = errorMessage!!,
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

            Button(

                onClick = {

                    if (
                        repeatMode == "Custom" &&
                        alarmViewModel
                            .selectedRepeatDays
                            .isEmpty()
                    ) {

                        errorMessage =
                            "Select at least one day."

                    } else {

                        alarmViewModel.setHour(
                            selectedHour
                        )

                        alarmViewModel.setMinute(
                            selectedMinute
                        )

                        alarmViewModel.setLabel(
                            labelText
                        )

                        alarmViewModel.addAlarm()

                        onAlarmAdded()
                    }
                }
            ) {

                Text("SET ALARM")
            }
        }
    )
}


/* =========================================================
   REPEAT OPTION
   ========================================================= */

@Composable
private fun RepeatOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            text = text,
            modifier =
                Modifier.padding(
                    start = 4.dp
                )
        )
    }
}


/* =========================================================
   CUSTOM DAYS
   ========================================================= */

@Composable
private fun CustomDays(
    alarmViewModel: AlarmViewModel
) {

    val days = listOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )

    Column {

        days.forEach { (day, name) ->

            val selected =
                alarmViewModel
                    .selectedRepeatDays
                    .contains(day)

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = selected,
                    onCheckedChange = {

                        alarmViewModel
                            .toggleRepeatDay(day)
                    }
                )

                Text(
                    text = name,
                    modifier =
                        Modifier.padding(
                            start = 4.dp
                        )
                )
            }
        }
    }
}


/* =========================================================
   FORMAT TIME
   ========================================================= */

private fun formatAlarmTime(
    hour: Int,
    minute: Int
): String {

    val period =
        if (hour >= 12) {
            "PM"
        } else {
            "AM"
        }

    val displayHour =
        when {

            hour == 0 -> 12

            hour > 12 ->
                hour - 12

            else ->
                hour
        }

    return String.format(
        Locale.getDefault(),
        "%02d:%02d %s",
        displayHour,
        minute,
        period
    )
}


/* =========================================================
   FORMAT REPEAT DAYS
   ========================================================= */

private fun formatRepeatDays(
    days: Set<Int>
): String {

    if (days.isEmpty()) {
        return "Once"
    }

    if (days.size == 7) {
        return "Every day"
    }

    if (
        days == setOf(
            1, 2, 3, 4, 5
        )
    ) {
        return "Weekdays"
    }

    if (
        days == setOf(
            6, 7
        )
    ) {
        return "Weekends"
    }

    val names = mapOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )

    return days
        .sorted()
        .joinToString(", ") {
            names[it] ?: ""
        }
}