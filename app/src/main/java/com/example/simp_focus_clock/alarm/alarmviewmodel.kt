package com.example.simp_focus_clock.alarm

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simp_focus_clock.data.AlarmPreferences
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlarmViewModel : ViewModel() {

    val alarms =
        mutableStateListOf<AlarmItem>()

    private var nextAlarmId = 1

    private var scheduler:
            AlarmScheduler? = null

    private var alarmPreferences:
            AlarmPreferences? = null

    private var isInitialized = false


    var selectedHour by mutableStateOf(6)
        private set

    var selectedMinute by mutableStateOf(0)
        private set

    var alarmLabel by mutableStateOf("Wake Up")
        private set


    val selectedRepeatDays =
        mutableStateListOf<Int>()


    fun initialize(
        context: Context
    ) {

        /*
         * Prevent multiple collectors
         */
        if (isInitialized) {
            return
        }

        isInitialized = true


        val appContext =
            context.applicationContext


        scheduler =
            AlarmScheduler(
                appContext
            )


        alarmPreferences =
            AlarmPreferences(
                appContext
            )


        /*
         * Load saved alarms automatically.
         */
        viewModelScope.launch {

            alarmPreferences
                ?.alarms
                ?.collectLatest { savedAlarms ->

                    alarms.clear()

                    alarms.addAll(
                        savedAlarms.sortedBy {
                            it.id
                        }
                    )


                    /*
                     * Generate next safe ID.
                     */
                    nextAlarmId =
                        (savedAlarms.maxOfOrNull {
                            it.id
                        } ?: 0) + 1


                    /*
                     * Re-schedule enabled alarms.
                     */
                    savedAlarms
                        .filter {
                            it.enabled
                        }
                        .forEach { alarm ->

                            scheduler?.schedule(
                                alarm
                            )
                        }
                }
        }
    }


    fun setHour(
        hour: Int
    ) {

        if (hour in 0..23) {

            selectedHour = hour
        }
    }


    fun setMinute(
        minute: Int
    ) {

        if (minute in 0..59) {

            selectedMinute = minute
        }
    }


    fun setLabel(
        label: String
    ) {

        alarmLabel = label
    }


    fun setRepeatDays(
        days: Set<Int>
    ) {

        selectedRepeatDays.clear()

        selectedRepeatDays.addAll(
            days
                .filter {
                    it in 1..7
                }
                .sorted()
        )
    }


    fun clearRepeatDays() {

        selectedRepeatDays.clear()
    }


    fun setOnce() {

        clearRepeatDays()
    }


    fun setEveryDay() {

        setRepeatDays(
            setOf(
                1,
                2,
                3,
                4,
                5,
                6,
                7
            )
        )
    }


    fun setWeekdays() {

        setRepeatDays(
            setOf(
                1,
                2,
                3,
                4,
                5
            )
        )
    }


    fun setWeekends() {

        setRepeatDays(
            setOf(
                6,
                7
            )
        )
    }


    fun toggleRepeatDay(
        day: Int
    ) {

        if (day !in 1..7) {
            return
        }


        if (
            selectedRepeatDays.contains(day)
        ) {

            selectedRepeatDays.remove(day)

        } else {

            selectedRepeatDays.add(day)

            selectedRepeatDays.sort()
        }
    }


    fun addAlarm() {

        val alarm =
            AlarmItem(
                id = nextAlarmId++,

                hour = selectedHour,

                minute = selectedMinute,

                label =
                    alarmLabel.ifBlank {
                        "Alarm"
                    },

                enabled = true,

                repeatDays =
                    selectedRepeatDays.toSet()
            )


        /*
         * Update UI immediately.
         */
        alarms.add(
            alarm
        )


        /*
         * Schedule immediately.
         */
        scheduler?.schedule(
            alarm
        )


        /*
         * Save permanently.
         */
        saveAlarms()


        resetEditor()
    }


    fun toggleAlarm(
        id: Int
    ) {

        val index =
            alarms.indexOfFirst {
                it.id == id
            }


        if (index == -1) {
            return
        }


        val alarm =
            alarms[index]


        val updatedAlarm =
            alarm.copy(
                enabled = !alarm.enabled
            )


        alarms[index] =
            updatedAlarm


        if (
            updatedAlarm.enabled
        ) {

            scheduler?.schedule(
                updatedAlarm
            )

        } else {

            scheduler?.cancel(
                updatedAlarm
            )
        }


        /*
         * Save updated state.
         */
        saveAlarms()
    }


    fun deleteAlarm(
        id: Int
    ) {

        val alarm =
            alarms.firstOrNull {
                it.id == id
            }


        if (alarm != null) {

            scheduler?.cancel(
                alarm
            )
        }


        alarms.removeAll {
            it.id == id
        }


        /*
         * Save after deletion.
         */
        saveAlarms()
    }


    private fun saveAlarms() {

        val preferences =
            alarmPreferences ?: return


        val alarmsToSave =
            alarms.toList()


        viewModelScope.launch {

            preferences.saveAlarms(
                alarmsToSave
            )
        }
    }


    fun resetEditor() {

        selectedHour = 6

        selectedMinute = 0

        alarmLabel = "Wake Up"

        selectedRepeatDays.clear()
    }
}