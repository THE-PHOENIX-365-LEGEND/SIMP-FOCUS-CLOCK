package com.example.simp_focus_clock.timer

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    var remainingMillis by mutableLongStateOf(0L)
        private set

    var isRunning by mutableStateOf(false)
        private set

    var isPaused by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    private var notificationManager:
            TimerNotificationManager? = null

    fun initialize(context: Context) {

        if (notificationManager == null) {

            notificationManager =
                TimerNotificationManager(
                    context.applicationContext
                )
        }
    }

    fun setTime(
        minutes: Int,
        seconds: Int = 0
    ) {

        if (isRunning) return

        val totalMillis =
            minutes.toLong() * 60_000L +
                    seconds.toLong() * 1_000L

        if (totalMillis <= 0L) return

        remainingMillis = totalMillis
        isPaused = false
    }

    fun start() {

        if (remainingMillis <= 0L) return
        if (isRunning) return

        isRunning = true
        isPaused = false

        timerJob?.cancel()

        timerJob =
            viewModelScope.launch {

                while (
                    remainingMillis > 0L &&
                    isRunning
                ) {

                    delay(1000L)

                    remainingMillis =
                        (
                                remainingMillis -
                                        1000L
                                ).coerceAtLeast(0L)
                }

                if (remainingMillis <= 0L) {

                    remainingMillis = 0L
                    isRunning = false
                    isPaused = false

                    notificationManager
                        ?.showTimerFinished()
                }
            }
    }

    fun pause() {

        if (!isRunning) return

        isRunning = false
        isPaused = true

        timerJob?.cancel()
        timerJob = null
    }

    fun resume() {

        if (!isPaused) return
        if (remainingMillis <= 0L) return

        start()
    }

    fun reset() {

        timerJob?.cancel()
        timerJob = null

        remainingMillis = 0L
        isRunning = false
        isPaused = false

        notificationManager
            ?.cancelTimerNotification()
    }

    override fun onCleared() {

        timerJob?.cancel()

        super.onCleared()
    }
}