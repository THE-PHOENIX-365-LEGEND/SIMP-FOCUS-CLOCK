package com.example.simp_focus_clock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
                 * Notification permission
                 */
                LaunchedEffect(Unit) {

                    if (
                        android.os.Build.VERSION.SDK_INT >=
                        android.os.Build.VERSION_CODES.TIRAMISU
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
                 * Current bottom navigation screen
                 */
                var selectedScreenIndex by remember {

                    mutableIntStateOf(3)
                }


                /*
                 * App selector visibility
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
                 * App selector repository
                 */
                val appSelectorRepository =
                    remember {

                        AppSelectorRepository(
                            applicationContext
                        )
                    }


                /*
                 * Focus state
                 */
                val focusState by
                focusViewModel
                    .focusState
                    .collectAsState()


                /*
                 * Focus remaining time
                 */
                val remainingMillis by
                focusViewModel
                    .remainingMillis
                    .collectAsState()


                /*
                 * Emergency code
                 */
                val emergencyCode by
                focusViewModel
                    .emergencyCode
                    .collectAsState()


                /*
                 * Current screen
                 */
                val currentScreen =
                    MainScreen.entries[
                        selectedScreenIndex
                    ]


                /*
                 * Back button when App Selector is open
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
                                 * Alarm
                                 */
                                NavigationBarItem(

                                    selected =
                                        currentScreen ==
                                                MainScreen.ALARM,

                                    onClick = {

                                        selectedScreenIndex =
                                            0
                                    },

                                    icon = {

                                        Text("A")
                                    },

                                    label = {

                                        Text("Alarm")
                                    }
                                )


                                /*
                                 * Stopwatch
                                 */
                                NavigationBarItem(

                                    selected =
                                        currentScreen ==
                                                MainScreen.STOPWATCH,

                                    onClick = {

                                        selectedScreenIndex =
                                            1
                                    },

                                    icon = {

                                        Text("S")
                                    },

                                    label = {

                                        Text("Stopwatch")
                                    }
                                )


                                /*
                                 * Timer
                                 */
                                NavigationBarItem(

                                    selected =
                                        currentScreen ==
                                                MainScreen.TIMER,

                                    onClick = {

                                        selectedScreenIndex =
                                            2
                                    },

                                    icon = {

                                        Text("T")
                                    },

                                    label = {

                                        Text("Timer")
                                    }
                                )


                                /*
                                 * Focus
                                 */
                                NavigationBarItem(

                                    selected =
                                        currentScreen ==
                                                MainScreen.FOCUS,

                                    onClick = {

                                        selectedScreenIndex =
                                            3
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


                    /*
                     * Use Scaffold padding
                     */
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
                                 * -------------------------
                                 * ALARM
                                 * -------------------------
                                 */
                                MainScreen.ALARM -> {

                                    AlarmScreen(

                                        alarmViewModel =
                                            alarmViewModel
                                    )
                                }


                                /*
                                 * -------------------------
                                 * STOPWATCH
                                 * -------------------------
                                 */
                                MainScreen.STOPWATCH -> {

                                    StopwatchScreen(

                                        stopwatchViewModel =
                                            stopwatchViewModel
                                    )
                                }


                                /*
                                 * -------------------------
                                 * TIMER
                                 * -------------------------
                                 */
                                MainScreen.TIMER -> {

                                    TimerScreen(

                                        timerViewModel =
                                            timerViewModel
                                    )
                                }


                                /*
                                 * -------------------------
                                 * FOCUS
                                 * -------------------------
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
                                         * Select app
                                         */
                                        onSelectApp = {

                                            showAppSelector =
                                                true
                                        },


                                        /*
                                         * Focus duration
                                         */
                                        onFocusDurationChanged = {

                                                minutes: Int ->

                                            focusViewModel
                                                .setFocusDuration(
                                                    minutes
                                                )
                                        },


                                        /*
                                         * Cooldown duration
                                         */
                                        onCooldownDurationChanged = {

                                                minutes: Int ->

                                            focusViewModel
                                                .setCooldownDuration(
                                                    minutes
                                                )
                                        },


                                        /*
                                         * Start Focus
                                         */
                                        onStartFocus = {

                                            focusViewModel
                                                .startFocus(

                                                    onStarted = {
                                                        // Started
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
                                         * Emergency unlock
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
                                         * Accessibility settings
                                         */
                                        onOpenAccessibilitySettings = {

                                            context.startActivity(

                                                Intent(
                                                    Settings
                                                        .ACTION_ACCESSIBILITY_SETTINGS
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