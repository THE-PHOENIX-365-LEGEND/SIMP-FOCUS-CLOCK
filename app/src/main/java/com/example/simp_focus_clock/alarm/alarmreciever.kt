package com.example.simp_focus_clock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    companion object {

        const val EXTRA_ALARM_ID =
            "extra_alarm_id"

        const val EXTRA_LABEL =
            "extra_alarm_label"

        const val EXTRA_REPEAT_DAYS =
            "extra_repeat_days"

        const val EXTRA_HOUR =
            "extra_hour"

        const val EXTRA_MINUTE =
            "extra_minute"

        const val ACTION_SNOOZE =
            "com.example.simp_focus_clock.SNOOZE"
    }


    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        /*
         * =========================
         * SNOOZE
         * =========================
         */
        if (intent.action == ACTION_SNOOZE) {

            handleSnooze(
                context,
                intent
            )

            return
        }


        /*
         * =========================
         * NORMAL ALARM
         * =========================
         */
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
         * =========================
         * IMPORTANT:
         * Schedule the next repeat FIRST.
         *
         * This happens before starting
         * the alarm UI/service.
         * =========================
         */
        if (
            alarmId != -1 &&
            repeatDays.isNotEmpty()
        ) {

            val hour =
                intent.getIntExtra(
                    EXTRA_HOUR,
                    0
                )


            val minute =
                intent.getIntExtra(
                    EXTRA_MINUTE,
                    0
                )


            val repeatingAlarm =
                AlarmItem(
                    id = alarmId,
                    hour = hour,
                    minute = minute,
                    label = label,
                    enabled = true,
                    repeatDays = repeatDays
                )


            AlarmScheduler(
                context.applicationContext
            ).schedule(
                repeatingAlarm
            )
        }


        /*
         * =========================
         * START ALARM SOUND
         * =========================
         */
        val serviceIntent =
            Intent(
                context,
                AlarmSoundService::class.java
            ).apply {

                putExtra(
                    EXTRA_LABEL,
                    label
                )

                putExtra(
                    EXTRA_ALARM_ID,
                    alarmId
                )
            }


        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            context.startForegroundService(
                serviceIntent
            )

        } else {

            context.startService(
                serviceIntent
            )
        }
    }


    /*
     * =========================
     * HANDLE SNOOZE
     * =========================
     */
    private fun handleSnooze(
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


        /*
         * Unique request code
         * for every alarm.
         */
        val snoozeRequestCode =
            3000 +
                    if (alarmId >= 0) {
                        alarmId
                    } else {
                        1
                    }


        val snoozeIntent =
            Intent(
                context,
                AlarmReceiver::class.java
            ).apply {

                putExtra(
                    EXTRA_LABEL,
                    label
                )

                putExtra(
                    EXTRA_ALARM_ID,
                    alarmId
                )
            }


        val snoozePendingIntent =
            PendingIntent.getBroadcast(
                context,
                snoozeRequestCode,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )


        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager


        /*
         * Snooze for 5 minutes.
         */
        val triggerTime =
            System.currentTimeMillis() +
                    5 * 60 * 1000L


        try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S &&
                alarmManager.canScheduleExactAlarms()
            ) {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    snoozePendingIntent
                )

            } else {

                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    snoozePendingIntent
                )
            }

        } catch (_: SecurityException) {

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                snoozePendingIntent
            )
        }


        /*
         * Stop currently ringing alarm.
         */
        val stopIntent =
            Intent(
                context,
                AlarmSoundService::class.java
            ).apply {

                action =
                    AlarmSoundService.ACTION_STOP_ALARM
            }


        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            context.startForegroundService(
                stopIntent
            )

        } else {

            context.startService(
                stopIntent
            )
        }
    }
}