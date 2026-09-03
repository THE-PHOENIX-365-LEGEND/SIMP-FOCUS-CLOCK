package com.example.simp_focus_clock.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.simp_focus_clock.R
import android.annotation.SuppressLint

class FocusNotificationManager(
    private val context: Context
) {

    companion object {

        private const val CHANNEL_ID =
            "focus_clock_channel"

        private const val CHANNEL_NAME =
            "Focus Clock"

        private const val CHANNEL_DESCRIPTION =
            "Notifications for Focus sessions and cooldowns"

        private const val FOCUS_NOTIFICATION_ID =
            1001

        private const val COOLDOWN_NOTIFICATION_ID =
            1002

        private const val COMPLETE_NOTIFICATION_ID =
            1003
    }

    private val notificationManager =
        context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {

                description =
                    CHANNEL_DESCRIPTION
            }

        notificationManager.createNotificationChannel(
            channel
        )
    }

    @SuppressLint("MissingPermission")
    fun showFocusStarted(
        appName: String,
        durationMinutes: Int
    ) {

        if (!hasNotificationPermission()) {
            return
        }

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_launcher_foreground
                )
                .setContentTitle(
                    "Focus Started"
                )
                .setContentText(
                    "Focus session started for $appName."
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "Focus session started for " +
                                    "$appName for " +
                                    "$durationMinutes minutes."
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(false)
                .setOngoing(true)
                .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                FOCUS_NOTIFICATION_ID,
                notification
            )
    }

    @SuppressLint("MissingPermission")
    fun showCooldownStarted(
        appName: String,
        cooldownMinutes: Int
    ) {

        if (!hasNotificationPermission()) {
            return
        }

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_launcher_foreground
                )
                .setContentTitle(
                    "Cooldown Started"
                )
                .setContentText(
                    "$appName is now restricted."
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "$appName is now restricted " +
                                    "for $cooldownMinutes minutes."
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(false)
                .setOngoing(true)
                .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                COOLDOWN_NOTIFICATION_ID,
                notification
            )
    }

    @SuppressLint("MissingPermission")
    fun showCooldownCompleted(
        appName: String
    ) {

        if (!hasNotificationPermission()) {
            return
        }

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_launcher_foreground
                )
                .setContentTitle(
                    "Cooldown Completed"
                )
                .setContentText(
                    "$appName is available again."
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .setOngoing(false)
                .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                COMPLETE_NOTIFICATION_ID,
                notification
            )

        cancelFocusNotification()
        cancelCooldownNotification()
    }

    fun cancelFocusNotification() {

        NotificationManagerCompat
            .from(context)
            .cancel(
                FOCUS_NOTIFICATION_ID
            )
    }

    fun cancelCooldownNotification() {

        NotificationManagerCompat
            .from(context)
            .cancel(
                COOLDOWN_NOTIFICATION_ID
            )
    }

    private fun hasNotificationPermission(): Boolean {

        return if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.TIRAMISU
        ) {

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        } else {

            true
        }
    }
}