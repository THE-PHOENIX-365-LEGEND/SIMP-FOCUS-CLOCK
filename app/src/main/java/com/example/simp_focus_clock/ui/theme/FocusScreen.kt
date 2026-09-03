package com.example.simp_focus_clock.ui.focus

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.simp_focus_clock.model.FocusSession
import com.example.simp_focus_clock.model.FocusState
import com.example.simp_focus_clock.model.FocusStatus
import kotlinx.coroutines.delay

@Composable
fun FocusScreen(
    selectedAppName: String?,
    focusDurationMinutes: Int,
    cooldownDurationMinutes: Int,
    focusState: FocusState,
    remainingMillis: Long,
    emergencyCode: String?,
    onSelectApp: () -> Unit,
    onFocusDurationChanged: (Int) -> Unit,
    onCooldownDurationChanged: (Int) -> Unit,
    onStartFocus: () -> Unit,
    onEmergencyUnlockForSession: (String, String) -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {

    var showEmergencyCodeDialog by remember {
        mutableStateOf(false)
    }

    var codeDialogShownFor by remember {
        mutableStateOf<String?>(null)
    }

    /*
     * Whenever a NEW emergency code is generated,
     * automatically show it once.
     */
    LaunchedEffect(emergencyCode) {

        if (
            emergencyCode != null &&
            emergencyCode != codeDialogShownFor
        ) {
            codeDialogShownFor = emergencyCode
            showEmergencyCodeDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Focus",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text =
                "Focus on your work and temporarily restrict selected apps.",
            style = MaterialTheme.typography.bodyMedium
        )

        /*
         * APP SELECTION
         */
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Target App",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text =
                        selectedAppName
                            ?: "No app selected",
                    style = MaterialTheme.typography.bodyLarge
                )

                OutlinedButton(
                    onClick = onSelectApp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SELECT APP")
                }
            }
        }

        /*
         * FOCUS DURATION
         */
        DurationSelector(
            title = "Focus Duration",
            selectedMinutes = focusDurationMinutes,
            options = listOf(
                5,
                10,
                15,
                20,
                25,
                30,
                45,
                60,
                90,
                120
            ),
            onSelected = onFocusDurationChanged
        )

        /*
         * COOLDOWN DURATION
         */
        DurationSelector(
            title = "Cooldown Duration",
            selectedMinutes = cooldownDurationMinutes,
            options = listOf(
                15,
                30,
                60,
                90,
                120,
                180,
                240,
                360,
                720,
                1440
            ),
            onSelected = onCooldownDurationChanged
        )

        /*
         * ACCESSIBILITY PROTECTION
         */
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = "Focus Protection",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text =
                        "Accessibility protection is required so " +
                                "Focus Mode can restrict the selected app.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick =
                        onOpenAccessibilitySettings,
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text("ENABLE FOCUS PROTECTION")
                }
            }
        }

        /*
         * START FOCUS
         */
        Button(
            onClick = onStartFocus,
            enabled =
                selectedAppName != null &&
                        focusState.sessions.size < 10,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("START FOCUS")
        }

        /*
         * ACTIVE SESSION COUNT
         */
        Text(
            text =
                "Active Sessions: " +
                        "${focusState.sessions.size}/10",
            style = MaterialTheme.typography.titleMedium
        )

        /*
         * SESSION LIST
         */
        if (focusState.sessions.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "No active Focus sessions.",
                    modifier = Modifier.padding(16.dp)
                )
            }

        } else {

            focusState.sessions.forEach { session ->

                SessionCard(
                    session = session,
                    remainingMillis =
                        if (
                            session.id ==
                            focusState.sessions
                                .maxByOrNull {
                                    it.focusStartTime
                                }
                                ?.id
                        ) {
                            remainingMillis
                        } else {
                            0L
                        },
                    onEmergencyUnlock = {
                            sessionId,
                            code ->

                        onEmergencyUnlockForSession(
                            sessionId,
                            code
                        )
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )
    }

    /*
     * EMERGENCY CODE DISPLAY
     */
    if (
        showEmergencyCodeDialog &&
        emergencyCode != null
    ) {

        AlertDialog(

            onDismissRequest = {
                showEmergencyCodeDialog = false
            },

            title = {
                Text("Emergency Unlock Code")
            },

            text = {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text =
                            "Save this 6-digit code somewhere safe.",
                        style =
                            MaterialTheme.typography.bodyMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(20.dp)
                    )

                    Text(
                        text = emergencyCode,
                        style =
                            MaterialTheme.typography.headlineLarge
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "You may need this code if you " +
                                    "want to unlock a cooldown early.",
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {
                        showEmergencyCodeDialog = false
                    }
                ) {
                    Text("I SAVED IT")
                }
            }
        )
    }
}


/*
 * DURATION SELECTOR
 */
@Composable
private fun DurationSelector(
    title: String,
    selectedMinutes: Int,
    options: List<Int>,
    onSelected: (Int) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            BoxForDropdown(
                selectedMinutes = selectedMinutes,
                options = options,
                expanded = expanded,
                onExpandedChange = {
                    expanded = it
                },
                onSelected = {
                    onSelected(it)
                    expanded = false
                }
            )
        }
    }
}


/*
 * DROPDOWN BOX
 */
@Composable
private fun BoxForDropdown(
    selectedMinutes: Int,
    options: List<Int>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (Int) -> Unit
) {

    Column {

        OutlinedButton(
            onClick = {
                onExpandedChange(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    formatMinutes(selectedMinutes)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            }
        ) {

            options.forEach { minutes ->

                DropdownMenuItem(

                    text = {
                        Text(
                            formatMinutes(minutes)
                        )
                    },

                    onClick = {
                        onSelected(minutes)
                    }
                )
            }
        }
    }
}


/*
 * SESSION CARD
 */
@Composable
private fun SessionCard(
    session: FocusSession,
    remainingMillis: Long,
    onEmergencyUnlock: (String, String) -> Unit
) {

    var liveRemaining by remember(
        session.id,
        session.status,
        session.focusEndTime,
        session.cooldownEndTime
    ) {
        mutableLongStateOf(
            calculateSessionRemaining(session)
        )
    }

    LaunchedEffect(
        session.id,
        session.status,
        session.focusEndTime,
        session.cooldownEndTime
    ) {

        while (true) {

            liveRemaining =
                calculateSessionRemaining(session)

            delay(1000L)
        }
    }

    var showUnlockDialog by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = session.appName,
                style =
                    MaterialTheme.typography.titleLarge
            )

            Text(
                text =
                    "Status: ${
                        when (session.status) {
                            FocusStatus.FOCUSING ->
                                "FOCUSING"

                            FocusStatus.COOLDOWN ->
                                "COOLDOWN"

                            FocusStatus.IDLE ->
                                "IDLE"
                        }
                    }",
                style =
                    MaterialTheme.typography.bodyMedium
            )

            Text(
                text =
                    when (session.status) {

                        FocusStatus.FOCUSING ->
                            "Focus remaining: ${
                                formatRemainingTime(
                                    liveRemaining
                                )
                            }"

                        FocusStatus.COOLDOWN ->
                            "Cooldown remaining: ${
                                formatRemainingTime(
                                    liveRemaining
                                )
                            }"

                        FocusStatus.IDLE ->
                            "Inactive"
                    }
            )

            if (
                session.status ==
                FocusStatus.COOLDOWN
            ) {

                Button(
                    onClick = {
                        showUnlockDialog = true
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text("EMERGENCY UNLOCK")
                }
            }
        }
    }

    /*
     * EMERGENCY UNLOCK DIALOG
     */
    if (showUnlockDialog) {

        EmergencyUnlockDialog(

            onDismiss = {
                showUnlockDialog = false
            },

            onSubmit = { enteredCode ->

                /*
                 * MainActivity sends the code to
                 * FocusViewModel.
                 */
                onEmergencyUnlock(
                    session.id,
                    enteredCode
                )

                /*
                 * Close after submission.
                 * MainActivity shows success/failure Toast.
                 */
                showUnlockDialog = false
            }
        )
    }
}


/*
 * EMERGENCY UNLOCK DIALOG
 */
@Composable
private fun EmergencyUnlockDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {

    var enteredCode by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("Emergency Unlock")
        },

        text = {

            Column {

                Text(
                    text =
                        "Enter your 6-digit emergency code."
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedTextField(

                    value = enteredCode,

                    onValueChange = { value ->

                        if (
                            value.length <= 6 &&
                            value.all {
                                it.isDigit()
                            }
                        ) {
                            enteredCode = value
                        }
                    },

                    label = {
                        Text("Emergency Code")
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

                enabled =
                    enteredCode.length == 6,

                onClick = {
                    onSubmit(enteredCode)
                }
            ) {
                Text("UNLOCK")
            }
        }
    )
}


/*
 * TIME CALCULATION
 */
private fun calculateSessionRemaining(
    session: FocusSession
): Long {

    val now =
        System.currentTimeMillis()

    return when (session.status) {

        FocusStatus.FOCUSING ->
            (session.focusEndTime - now)
                .coerceAtLeast(0L)

        FocusStatus.COOLDOWN ->
            (session.cooldownEndTime - now)
                .coerceAtLeast(0L)

        FocusStatus.IDLE ->
            0L
    }
}


/*
 * TIME FORMAT
 */
private fun formatRemainingTime(
    millis: Long
): String {

    val totalSeconds =
        (millis / 1000L)
            .coerceAtLeast(0L)

    val hours =
        totalSeconds / 3600L

    val minutes =
        (totalSeconds % 3600L) / 60L

    val seconds =
        totalSeconds % 60L

    return if (hours > 0) {

        String.format(
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )

    } else {

        String.format(
            "%02d:%02d",
            minutes,
            seconds
        )
    }
}


/*
 * DURATION FORMAT
 */
private fun formatMinutes(
    minutes: Int
): String {

    return if (minutes < 60) {

        "$minutes min"

    } else {

        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        if (remainingMinutes == 0) {

            "$hours hr"

        } else {

            "$hours hr $remainingMinutes min"
        }
    }
}