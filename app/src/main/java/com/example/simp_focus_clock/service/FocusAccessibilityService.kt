package com.example.simp_focus_clock.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import android.widget.TextView
import com.example.simp_focus_clock.data.AppPreferences
import com.example.simp_focus_clock.manager.FocusSessionManager
import com.example.simp_focus_clock.model.FocusSession
import com.example.simp_focus_clock.model.FocusState
import com.example.simp_focus_clock.model.FocusStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FocusAccessibilityService : AccessibilityService() {

    private lateinit var appPreferences: AppPreferences
    private lateinit var focusSessionManager: FocusSessionManager
    private lateinit var windowManager: WindowManager

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Main.immediate
        )

    private var currentFocusState =
        FocusState()

    /*
     * This is the package Android most recently reported
     * as the foreground application.
     */
    private var currentForegroundPackage: String? =
        null

    private var blockingView: View? =
        null

    private var blockedSessionId: String? =
        null

    override fun onServiceConnected() {
        super.onServiceConnected()

        appPreferences =
            AppPreferences(applicationContext)

        focusSessionManager =
            FocusSessionManager(appPreferences)

        windowManager =
            getSystemService(
                WINDOW_SERVICE
            ) as WindowManager

        configureAccessibilityService()

        startFocusStateObserver()

        startSafetyMonitor()
    }

    private fun configureAccessibilityService() {

        val info =
            serviceInfo

        /*
         * The XML already requests the required events.
         *
         * We only change the notification timeout here.
         * We intentionally do NOT modify flags here because
         * your current Android Studio setup was reporting a
         * flags type-resolution error.
         */
        info.notificationTimeout = 0L

        serviceInfo = info
    }

    private fun startFocusStateObserver() {

        serviceScope.launch {

            focusSessionManager
                .focusState
                .collect { state ->

                    currentFocusState =
                        state

                    /*
                     * Important:
                     * If the target app is already open when
                     * FOCUSING changes to COOLDOWN, this check
                     * blocks it immediately.
                     */
                    checkCurrentApp()
                }
        }
    }

    private fun startSafetyMonitor() {

        serviceScope.launch {

            while (true) {

                try {

                    currentFocusState =
                        focusSessionManager
                            .updateStateFromCurrentTime()

                    /*
                     * Use root only as a supplementary check.
                     *
                     * It is NOT allowed to blindly overwrite the
                     * foreground package with System UI/Launcher.
                     */
                    updateForegroundFromRootIfUseful()

                    checkCurrentApp()

                } catch (_: Exception) {
                }

                /*
                 * Fast backup check.
                 */
                delay(100L)
            }
        }
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (event == null) {
            return
        }

        when (event.eventType) {

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {

                val eventPackage =
                    event.packageName
                        ?.toString()

                /*
                 * Android's event package is our PRIMARY
                 * foreground signal.
                 *
                 * Never replace it with our own package.
                 */
                if (
                    !eventPackage.isNullOrBlank() &&
                    eventPackage != packageName
                ) {

                    currentForegroundPackage =
                        eventPackage
                }

                /*
                 * React immediately.
                 */
                serviceScope.launch {

                    try {

                        currentFocusState =
                            focusSessionManager
                                .updateStateFromCurrentTime()

                        checkCurrentApp()

                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private fun updateForegroundFromRootIfUseful() {

        val rootPackage =
            try {
                rootInActiveWindow
                    ?.packageName
                    ?.toString()
            } catch (_: Exception) {
                null
            }

        if (
            rootPackage.isNullOrBlank() ||
            rootPackage == packageName
        ) {
            return
        }

        /*
         * Only use rootInActiveWindow to change our foreground
         * state when it represents either:
         *
         * 1. a currently blocked package, or
         * 2. the package we already believe is foreground.
         *
         * This prevents Launcher/System UI from randomly
         * overwriting the real app.
         */
        val isBlockedPackage =
            currentFocusState.sessions.any { session ->

                session.packageName ==
                        rootPackage &&

                        session.status ==
                        FocusStatus.COOLDOWN
            }

        val isCurrentPackage =
            rootPackage ==
                    currentForegroundPackage

        if (
            isBlockedPackage ||
            isCurrentPackage
        ) {

            currentForegroundPackage =
                rootPackage
        }
    }

    private fun checkCurrentApp() {

        val foregroundPackage =
            currentForegroundPackage
                ?: return

        /*
         * Look for a COOLDOWN session belonging to the
         * currently foreground application.
         */
        val blockedSession =
            currentFocusState.sessions
                .firstOrNull { session ->

                    session.packageName ==
                            foregroundPackage &&

                            session.status ==
                            FocusStatus.COOLDOWN
                }

        if (blockedSession != null) {

            /*
             * TARGET IS BLOCKED.
             *
             * Show the overlay immediately.
             */
            showBlockingView(
                blockedSession
            )

            return
        }

        /*
         * If there is no blocked session for the current
         * foreground package, the user has moved away from
         * the blocked app.
         */
        if (
            blockingView != null
        ) {

            val blockedSession =
                currentFocusState.sessions
                    .firstOrNull { session ->

                        session.id ==
                                blockedSessionId
                    }

            /*
             * Keep the overlay only if its session is still
             * in cooldown AND the same target package is
             * currently foreground.
             */
            val shouldKeepBlocking =
                blockedSession != null &&
                        blockedSession.status ==
                        FocusStatus.COOLDOWN &&
                        blockedSession.packageName ==
                        foregroundPackage

            if (!shouldKeepBlocking) {

                removeBlockingView()
            }
        }
    }

    private fun showBlockingView(
        session: FocusSession
    ) {

        /*
         * Already showing the correct block screen.
         * Do NOT recreate it.
         */
        if (
            blockingView != null &&
            blockedSessionId == session.id
        ) {
            return
        }

        removeBlockingView()

        val container =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(32),
                    dp(32),
                    dp(32),
                    dp(32)
                )

                setBackgroundColor(
                    Color.WHITE
                )

                /*
                 * Consume touches.
                 *
                 * We deliberately do NOT use
                 * FLAG_NOT_TOUCHABLE below.
                 */
                isClickable =
                    true

                isFocusable =
                    true

                setOnClickListener {
                    /*
                     * Intentionally empty.
                     *
                     * This makes the blocking surface consume
                     * the click instead of passing it through.
                     */
                }
            }

        val title =
            TextView(this).apply {

                text =
                    "Focus Mode Active"

                textSize =
                    28f

                setTextColor(
                    Color.BLACK
                )

                gravity =
                    Gravity.CENTER
            }

        val message =
            TextView(this).apply {

                text =
                    "This app is currently restricted.\n\n" +
                            "Your cooldown session is still active."

                textSize =
                    18f

                setTextColor(
                    Color.DKGRAY
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    dp(24),
                    0,
                    0
                )
            }

        val targetApp =
            TextView(this).apply {

                text =
                    session.appName

                textSize =
                    20f

                setTextColor(
                    Color.BLACK
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    dp(24),
                    0,
                    0
                )
            }

        container.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        container.addView(
            message,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        container.addView(
            targetApp,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val layoutParams =
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,

                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,

                /*
                 * IMPORTANT:
                 *
                 * No FLAG_NOT_TOUCHABLE.
                 * Therefore touches do not pass through
                 * the blocking layer.
                 */
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,

                PixelFormat.OPAQUE
            )

        layoutParams.gravity =
            Gravity.CENTER

        try {

            windowManager.addView(
                container,
                layoutParams
            )

            blockingView =
                container

            blockedSessionId =
                session.id

        } catch (_: Exception) {

            blockingView =
                null

            blockedSessionId =
                null
        }
    }

    private fun removeBlockingView() {

        val view =
            blockingView
                ?: return

        try {

            windowManager.removeView(
                view
            )

        } catch (_: Exception) {
        }

        blockingView =
            null

        blockedSessionId =
            null
    }

    private fun dp(
        value: Int
    ): Int {

        return (
                value *
                        resources
                            .displayMetrics
                            .density
                ).toInt()
    }

    override fun onInterrupt() {

        removeBlockingView()
    }

    override fun onDestroy() {

        removeBlockingView()

        serviceScope.cancel()

        super.onDestroy()
    }
}