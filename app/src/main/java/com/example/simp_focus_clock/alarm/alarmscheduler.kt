package com.example.simp_focus_clock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class AlarmScheduler(
    private val context: Context
) {

    private val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager


    /*
     * =========================
     * SCHEDULE ALARM
     * =========================
     */
    fun schedule(
        alarm: AlarmItem
    ) {

        if (!alarm.enabled) return


        val triggerTime =
            calculateNextTriggerTime(
                alarm
            )


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
                    AlarmReceiver.EXTRA_HOUR,
                    alarm.hour
                )

                putExtra(
                    AlarmReceiver.EXTRA_MINUTE,
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


        scheduleExact(
            triggerTime,
            pendingIntent
        )
    }


    /*
     * =========================
     * EXACT ALARM SCHEDULING
     * =========================
     */
    private fun scheduleExact(
        triggerTime: Long,
        pendingIntent: PendingIntent
    ) {

        try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S
            ) {

                if (
                    alarmManager.canScheduleExactAlarms()
                ) {

                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )

                } else {

                    /*
                     * Fallback if exact alarm
                     * permission is unavailable.
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

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }


    /*
     * =========================
     * CANCEL ALARM
     * =========================
     */
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


    /*
     * =========================
     * CALCULATE NEXT TRIGGER
     * =========================
     */
    private fun calculateNextTriggerTime(
        alarm: AlarmItem
    ): Long {

        val now =
            Calendar.getInstance()


        /*
         * =========================
         * ONCE ALARM
         * =========================
         */
        if (
            alarm.repeatDays.isEmpty()
        ) {

            val target =
                Calendar.getInstance()

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
             * If today's time has already
             * passed, schedule tomorrow.
             */
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
         * =========================
         * REPEATING ALARM
         * =========================
         *
         * Check today + next 7 days
         * and return the first selected
         * day that is still in the future.
         */
        for (
        daysAhead in 0..7
        ) {

            val candidate =
                Calendar.getInstance()


            candidate.add(
                Calendar.DAY_OF_YEAR,
                daysAhead
            )


            candidate.set(
                Calendar.HOUR_OF_DAY,
                alarm.hour
            )

            candidate.set(
                Calendar.MINUTE,
                alarm.minute
            )

            candidate.set(
                Calendar.SECOND,
                0
            )

            candidate.set(
                Calendar.MILLISECOND,
                0
            )


            val modelDay =
                when (
                    candidate.get(
                        Calendar.DAY_OF_WEEK
                    )
                ) {

                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    Calendar.SUNDAY -> 7

                    else -> 0
                }


            /*
             * Selected repeat day
             * and time must be future.
             */
            if (
                modelDay in alarm.repeatDays &&
                candidate.timeInMillis >
                now.timeInMillis
            ) {

                return candidate.timeInMillis
            }
        }


        /*
         * Safety fallback:
         * one week later.
         */
        val fallback =
            Calendar.getInstance()

        fallback.add(
            Calendar.DAY_OF_YEAR,
            7
        )

        fallback.set(
            Calendar.HOUR_OF_DAY,
            alarm.hour
        )

        fallback.set(
            Calendar.MINUTE,
            alarm.minute
        )

        fallback.set(
            Calendar.SECOND,
            0
        )

        fallback.set(
            Calendar.MILLISECOND,
            0
        )

        return fallback.timeInMillis
    }
}