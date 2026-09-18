package com.example.simp_focus_clock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.core.content.ContextCompat

import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.simp_focus_clock.alarm.AlarmScreen
import com.example.simp_focus_clock.alarm.AlarmViewModel

import com.example.simp_focus_clock.data.AppPreferences
import com.example.simp_focus_clock.repository.AppSelectorRepository

import com.example.simp_focus_clock.stopwatch.StopwatchScreen
import com.example.simp_focus_clock.stopwatch.StopwatchViewModel

import com.example.simp_focus_clock.timer.TimerScreen
import com.example.simp_focus_clock.timer.TimerViewModel

import com.example.simp_focus_clock.ui.focus.AppSelectorScreen
import com.example.simp_focus_clock.ui.focus.FocusScreen
import com.example.simp_focus_clock.ui.focus.FocusViewModel

import com.example.simp_focus_clock.ui.theme.SIMPFOCUSCLOCKTheme


private enum class MainScreen {
    ALARM,
    STOPWATCH,
    TIMER,
    FOCUS
}


class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (!granted) {

                Toast.makeText(
                    this,
                    "Notification permission is required for notifications.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)


        setContent {

            SIMPFOCUSCLOCKTheme {

                val context =
                    LocalContext.current


                /*
                 * =========================
                 * NOTIFICATION PERMISSION
                 * =========================
                 */
                LaunchedEffect(Unit) {

                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.TIRAMISU
                    ) {

                        val permissionGranted =
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) ==
                                    PackageManager.PERMISSION_GRANTED


                        if (!permissionGranted) {

                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    }
                }


                /*
                 * =========================
                 * CURRENT BOTTOM NAV SCREEN
                 * =========================
                 */
                var selectedScreenIndex by remember {

                    mutableIntStateOf(3)
                }


                /*
                 * =========================
                 * APP SELECTOR VISIBILITY
                 * =========================
                 */
                var showAppSelector by remember {

                    mutableStateOf(false)
                }


                /*
                 * =========================
                 * ALARM
                 * =========================
                 */
                val alarmViewModel:
                        AlarmViewModel =
                    viewModel()


                alarmViewModel.initialize(
                    applicationContext
                )


                /*
                 * =========================
                 * STOPWATCH
                 * =========================
                 */
                val stopwatchViewModel:
                        StopwatchViewModel =
                    viewModel()


                /*
                 * =========================
                 * TIMER
                 * =========================
                 */
                val timerViewModel:
                        TimerViewModel =
                    viewModel()


                timerViewModel.initialize(
                    applicationContext
                )


                /*
                 * =========================
                 * FOCUS
                 * =========================
                 */
                val focusViewModel:
                        FocusViewModel =
                    viewModel()


                val appPreferences =
                    remember {

                        AppPreferences(
                            applicationContext
                        )
                    }


                focusViewModel.initialize(
                    appPreferences,
                    applicationContext
                )


                /*
                 * =========================
                 * APP SELECTOR REPOSITORY
                 * =========================
                 */
                val appSelectorRepository =
                    remember {

                        AppSelectorRepository(
                            applicationContext
                        )
                    }


                /*
                 * =========================
                 * FOCUS STATE
                 * =========================
                 */
                val focusState by
                focusViewModel
                    .focusState
                    .collectAsState()


                /*
                 * =========================
                 * FOCUS REMAINING TIME
                 * =========================
                 */
                val remainingMillis by
                focusViewModel
                    .remainingMillis
                    .collectAsState()


                /*
                 * =========================
                 * EMERGENCY CODE
                 * =========================
                 */
                val emergencyCode by
                focusViewModel
                    .emergencyCode
                    .collectAsState()


                /*
                 * =========================
                 * CURRENT SCREEN
                 * =========================
                 */
                val currentScreen =
                    MainScreen.entries[
                        selectedScreenIndex
                    ]


                /*
                 * =========================
                 * BACK BUTTON
                 * =========================
                 */
                BackHandler(
                    enabled = showAppSelector
                ) {

                    showAppSelector = false
                }


                /*
                 * =========================
                 * MAIN SCAFFOLD
                 * =========================
                 */
                Scaffold(

                    modifier =
                        Modifier.fillMaxSize(),


                    /*
                     * =========================
                     * BOTTOM NAVIGATION
                     * =========================
                     */
                    bottomBar = {

                        if (!showAppSelector) {

                            NavigationBar {


                                /*
                                 * ALARM
                                 */
                                NavigationBarItem(

                                    selected =
                                        currentScreen ==
                                                MainScreen.ALARM,

                                    onClick = {

                                        selectedScreenIndex = 0
                                    },

                                    icon = {

                                        Text("A")
                                    },

                                    label = {

                                        Text("Alarm")
                                    }
                                )


                                /*
                                 * STOPWATCH
                                 */
                                NavigationBarItem(

                                    selected =
                                        currentScreen ==
                                                MainScreen.STOPWATCH,

                                    onClick = {

                                        selectedScreenIndex = 1
                                    },

                                    icon = {

                                        Text("S")
                                    },

                                    label = {

                                        Text("Stopwatch")
                                    }
                                )


                                /*
                                 * TIMER
                                 */
                                NavigationBarItem(

                                    selected =
                                        currentScreen ==
                                                MainScreen.TIMER,

                                    onClick = {

                                        selectedScreenIndex = 2
                                    },

                                    icon = {

                                        Text("T")
                                    },

                                    label = {

                                        Text("Timer")
                                    }
                                )


                                /*
                                 * FOCUS
                                 */
                                NavigationBarItem(

                                    selected =
                                        currentScreen ==
                                                MainScreen.FOCUS,

                                    onClick = {

                                        selectedScreenIndex = 3
                                    },

                                    icon = {

                                        Text("F")
                                    },

                                    label = {

                                        Text("Focus")
                                    }
                                )
                            }
                        }
                    }

                ) { innerPadding ->


                    Box(

                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(
                                    innerPadding
                                )
                    ) {


                        /*
                         * =========================
                         * APP SELECTOR
                         * =========================
                         */
                        if (showAppSelector) {

                            AppSelectorScreen(

                                repository =
                                    appSelectorRepository,

                                onAppSelected = { app ->

                                    focusViewModel
                                        .selectApp(
                                            app
                                        )

                                    showAppSelector =
                                        false
                                }
                            )


                        } else {


                            /*
                             * =========================
                             * MAIN SCREENS
                             * =========================
                             */
                            when (currentScreen) {


                                /*
                                 * ALARM
                                 */
                                MainScreen.ALARM -> {

                                    AlarmScreen(

                                        alarmViewModel =
                                            alarmViewModel
                                    )
                                }


                                /*
                                 * STOPWATCH
                                 */
                                MainScreen.STOPWATCH -> {

                                    StopwatchScreen(

                                        stopwatchViewModel =
                                            stopwatchViewModel
                                    )
                                }


                                /*
                                 * TIMER
                                 */
                                MainScreen.TIMER -> {

                                    TimerScreen(

                                        timerViewModel =
                                            timerViewModel
                                    )
                                }


                                /*
                                 * FOCUS
                                 */
                                MainScreen.FOCUS -> {

                                    FocusScreen(

                                        selectedAppName =
                                            focusViewModel
                                                .selectedApp
                                                ?.appName,


                                        focusDurationMinutes =
                                            focusViewModel
                                                .focusDurationMinutes,


                                        cooldownDurationMinutes =
                                            focusViewModel
                                                .cooldownDurationMinutes,


                                        focusState =
                                            focusState,


                                        remainingMillis =
                                            remainingMillis,


                                        emergencyCode =
                                            emergencyCode,


                                        /*
                                         * SELECT APP
                                         */
                                        onSelectApp = {

                                            showAppSelector =
                                                true
                                        },


                                        /*
                                         * FOCUS DURATION
                                         */
                                        onFocusDurationChanged = {

                                                minutes: Int ->

                                            focusViewModel
                                                .setFocusDuration(
                                                    minutes
                                                )
                                        },


                                        /*
                                         * COOLDOWN DURATION
                                         */
                                        onCooldownDurationChanged = {

                                                minutes: Int ->

                                            focusViewModel
                                                .setCooldownDuration(
                                                    minutes
                                                )
                                        },


                                        /*
                                         * START FOCUS
                                         */
                                        onStartFocus = {

                                            focusViewModel
                                                .startFocus(

                                                    onStarted = {
                                                        // Focus started
                                                    },

                                                    onError = {

                                                            error ->

                                                        Toast.makeText(
                                                            context,
                                                            error,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                )
                                        },


                                        /*
                                         * EMERGENCY UNLOCK
                                         */
                                        onEmergencyUnlockForSession = {

                                                sessionId: String,
                                                enteredCode: String ->

                                            focusViewModel
                                                .cancelCooldown(

                                                    sessionId =
                                                        sessionId,

                                                    enteredCode =
                                                        enteredCode,

                                                    onSuccess = {

                                                        Toast.makeText(
                                                            context,
                                                            "Cooldown unlocked.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },

                                                    onFailure = {

                                                        Toast.makeText(
                                                            context,
                                                            "Invalid emergency code.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                )
                                        },


                                        /*
                                         * ACCESSIBILITY SETTINGS
                                         */
                                        onOpenAccessibilitySettings = {

                                            context.startActivity(

                                                Intent(
                                                    Settings.ACTION_ACCESSIBILITY_SETTINGS
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}