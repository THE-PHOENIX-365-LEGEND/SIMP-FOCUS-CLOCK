package com.example.simp_focus_clock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {

    companion object {

        const val EXTRA_ALARM_ID =
            "extra_alarm_id"

        const val EXTRA_LABEL =
            "extra_alarm_label"

        const val EXTRA_REPEAT_DAYS =
            "extra_repeat_days"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val alarmId =
            intent.getIntExtra(
                EXTRA_ALARM_ID,
                -1
            )

        val label =
            intent.getStringExtra(
                EXTRA_LABEL
            ) ?: "Alarm"

        val repeatDays =
            intent.getIntArrayExtra(
                EXTRA_REPEAT_DAYS
            )?.toSet()
                ?: emptySet()

        /*
         * Temporary alarm trigger.
         *
         * Sound/notification will be added later,
         * as planned.
         */
        Toast.makeText(
            context,
            "Alarm: $label",
            Toast.LENGTH_LONG
        ).show()

        /*
         * If this is a repeating alarm,
         * schedule the next occurrence.
         */
        if (
            alarmId != -1 &&
            repeatDays.isNotEmpty()
        ) {

            val alarm =
                AlarmItem(
                    id = alarmId,
                    hour =
                        intent.getIntExtra(
                            "extra_hour",
                            0
                        ),
                    minute =
                        intent.getIntExtra(
                            "extra_minute",
                            0
                        ),
                    label = label,
                    enabled = true,
                    repeatDays = repeatDays
                )

            AlarmScheduler(
                context.applicationContext
            ).schedule(alarm)
        }
    }
}