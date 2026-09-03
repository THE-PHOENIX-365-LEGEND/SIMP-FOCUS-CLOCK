package com.example.simp_focus_clock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.util.Calendar

class AlarmScheduler(
    private val context: Context
) {

    private val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    fun schedule(
        alarm: AlarmItem
    ) {

        if (!alarm.enabled) return

        val triggerTime =
            calculateNextTriggerTime(alarm)

        val intent =
            Intent(
                context,
                AlarmReceiver::class.java
            ).apply {

                putExtra(
                    AlarmReceiver.EXTRA_ALARM_ID,
                    alarm.id
                )

                putExtra(
                    AlarmReceiver.EXTRA_LABEL,
                    alarm.label
                )

                putExtra(
                    AlarmReceiver.EXTRA_REPEAT_DAYS,
                    alarm.repeatDays.toIntArray()
                )

                putExtra(
                    "extra_hour",
                    alarm.hour
                )

                putExtra(
                    "extra_minute",
                    alarm.minute
                )
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S
            ) {

                if (
                    alarmManager
                        .canScheduleExactAlarms()
                ) {

                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )

                } else {

                    /*
                     * Fallback when exact-alarm
                     * permission is not available.
                     */
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }

            } else {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

        } catch (_: SecurityException) {

            /*
             * Safe fallback.
             */
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancel(
        alarm: AlarmItem
    ) {

        val intent =
            Intent(
                context,
                AlarmReceiver::class.java
            )

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(
            pendingIntent
        )

        pendingIntent.cancel()
    }

    private fun calculateNextTriggerTime(
        alarm: AlarmItem
    ): Long {

        val now =
            Calendar.getInstance()

        val target =
            Calendar.getInstance()

        /*
         * Device's current timezone.
         */
        target.set(
            Calendar.HOUR_OF_DAY,
            alarm.hour
        )

        target.set(
            Calendar.MINUTE,
            alarm.minute
        )

        target.set(
            Calendar.SECOND,
            0
        )

        target.set(
            Calendar.MILLISECOND,
            0
        )

        /*
         * One-time alarm.
         */
        if (
            alarm.repeatDays.isEmpty()
        ) {

            if (
                target.timeInMillis <=
                now.timeInMillis
            ) {

                target.add(
                    Calendar.DAY_OF_YEAR,
                    1
                )
            }

            return target.timeInMillis
        }

        /*
         * Repeating alarm.
         */
        for (
        daysAhead in 0..6
        ) {

            val candidate =
                Calendar.getInstance()

            candidate.timeInMillis =
                target.timeInMillis

            candidate.add(
                Calendar.DAY_OF_YEAR,
                daysAhead
            )

            val calendarDay =
                candidate.get(
                    Calendar.DAY_OF_WEEK
                )

            val modelDay =
                when (calendarDay) {

                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    Calendar.SUNDAY -> 7

                    else -> 0
                }

            if (
                modelDay in alarm.repeatDays &&
                candidate.timeInMillis >
                now.timeInMillis
            ) {

                return candidate.timeInMillis
            }
        }

        target.add(
            Calendar.DAY_OF_YEAR,
            7
        )

        return target.timeInMillis
    }
}