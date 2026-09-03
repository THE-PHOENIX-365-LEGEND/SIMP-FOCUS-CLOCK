package com.example.simp_focus_clock.stopwatch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StopwatchViewModel : ViewModel() {

    var elapsedMillis by mutableLongStateOf(0L)
        private set

    var isRunning by mutableStateOf(false)
        private set

    val laps = mutableStateListOf<Long>()

    private var stopwatchJob: Job? = null

    private var startTimeMillis = 0L
    private var accumulatedMillis = 0L

    fun start() {

        if (isRunning) return

        isRunning = true

        startTimeMillis =
            System.currentTimeMillis() -
                    accumulatedMillis

        stopwatchJob?.cancel()

        stopwatchJob =
            viewModelScope.launch {

                while (isRunning) {

                    elapsedMillis =
                        System.currentTimeMillis() -
                                startTimeMillis

                    delay(50L)
                }
            }
    }

    fun pause() {

        if (!isRunning) return

        elapsedMillis =
            System.currentTimeMillis() -
                    startTimeMillis

        accumulatedMillis =
            elapsedMillis

        isRunning = false

        stopwatchJob?.cancel()
        stopwatchJob = null
    }

    fun reset() {

        stopwatchJob?.cancel()
        stopwatchJob = null

        isRunning = false

        elapsedMillis = 0L
        accumulatedMillis = 0L
        startTimeMillis = 0L

        laps.clear()
    }

    fun addLap() {

        if (!isRunning) return

        val currentElapsed =
            System.currentTimeMillis() -
                    startTimeMillis

        laps.add(currentElapsed)
    }

    override fun onCleared() {

        stopwatchJob?.cancel()

        super.onCleared()
    }
}