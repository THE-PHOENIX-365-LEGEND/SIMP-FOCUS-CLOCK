package com.example.simp_focus_clock.ui.focus

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simp_focus_clock.data.AppPreferences
import com.example.simp_focus_clock.manager.FocusSessionManager
import com.example.simp_focus_clock.model.FocusSession
import com.example.simp_focus_clock.model.FocusState
import com.example.simp_focus_clock.model.FocusStatus
import com.example.simp_focus_clock.model.InstalledApp
import com.example.simp_focus_clock.notification.FocusNotificationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FocusViewModel : ViewModel() {

    var selectedApp by mutableStateOf<InstalledApp?>(null)
        private set

    var focusDurationMinutes by mutableStateOf(30)
        private set

    var cooldownDurationMinutes by mutableStateOf(120)
        private set

    private val _focusState =
        MutableStateFlow(FocusState())

    val focusState: StateFlow<FocusState> =
        _focusState.asStateFlow()

    private val _remainingMillis =
        MutableStateFlow(0L)

    val remainingMillis: StateFlow<Long> =
        _remainingMillis.asStateFlow()

    private val _emergencyCode =
        MutableStateFlow<String?>(null)

    val emergencyCode: StateFlow<String?> =
        _emergencyCode.asStateFlow()

    private var focusSessionManager:
            FocusSessionManager? = null

    private var notificationManager:
            FocusNotificationManager? = null

    private var stateUpdateJob: Job? = null

    /*
     * Keeps track of sessions that have already
     * triggered their cooldown notification.
     */
    private val cooldownNotificationSent =
        mutableSetOf<String>()

    /*
     * Keeps track of sessions whose completion
     * notification has already been sent.
     */
    private val completionNotificationSent =
        mutableSetOf<String>()

    fun initialize(
        appPreferences: AppPreferences,
        context: Context? = null
    ) {

        if (focusSessionManager == null) {

            focusSessionManager =
                FocusSessionManager(
                    appPreferences
                )

            if (context != null) {

                notificationManager =
                    FocusNotificationManager(
                        context.applicationContext
                    )
            }

            observeFocusState()

            refreshState()
        }
    }

    private fun observeFocusState() {

        val manager =
            focusSessionManager
                ?: return

        viewModelScope.launch {

            manager.focusState.collect { state ->

                _focusState.value =
                    state

                updateRemainingTime(
                    state
                )

                processNotifications(
                    state
                )
            }
        }
    }

    private fun refreshState() {

        val manager =
            focusSessionManager
                ?: return

        stateUpdateJob?.cancel()

        stateUpdateJob =
            viewModelScope.launch {

                while (true) {

                    val state =
                        manager
                            .updateStateFromCurrentTime()

                    _focusState.value =
                        state

                    updateRemainingTime(
                        state
                    )

                    processNotifications(
                        state
                    )

                    if (
                        state.sessions.isEmpty()
                    ) {
                        break
                    }

                    delay(1000L)
                }
            }
    }

    /*
     * Notification processing
     */
    private fun processNotifications(
        state: FocusState
    ) {

        val notifier =
            notificationManager
                ?: return

        val currentSessionIds =
            state.sessions
                .map { it.id }
                .toSet()

        cooldownNotificationSent
            .retainAll(currentSessionIds)

        completionNotificationSent
            .retainAll(currentSessionIds)

        state.sessions.forEach { session ->

            when (session.status) {

                FocusStatus.FOCUSING -> {
                    // Focus notification is sent when
                    // the session starts.
                }

                FocusStatus.COOLDOWN -> {

                    if (
                        cooldownNotificationSent
                            .add(session.id)
                    ) {

                        val cooldownMinutes =
                            (
                                    session.cooldownEndTime -
                                            session.cooldownStartTime
                                    ) / 60_000L

                        notifier.showCooldownStarted(
                            appName =
                                session.appName,

                            cooldownMinutes =
                                cooldownMinutes
                                    .toInt()
                                    .coerceAtLeast(1)
                        )
                    }
                }

                FocusStatus.IDLE -> {
                    // No notification required.
                }
            }
        }
    }

    private fun updateRemainingTime(
        state: FocusState
    ) {

        val now =
            System.currentTimeMillis()

        val session =
            state.sessions
                .maxByOrNull {
                    it.focusStartTime
                }

        if (session == null) {

            _remainingMillis.value =
                0L

            return
        }

        val remaining =
            when (session.status) {

                FocusStatus.IDLE ->
                    0L

                FocusStatus.FOCUSING ->
                    (
                            session.focusEndTime -
                                    now
                            ).coerceAtLeast(0L)

                FocusStatus.COOLDOWN ->
                    (
                            session.cooldownEndTime -
                                    now
                            ).coerceAtLeast(0L)
            }

        _remainingMillis.value =
            remaining
    }

    fun selectApp(
        app: InstalledApp
    ) {
        selectedApp = app
    }

    fun setFocusDuration(
        minutes: Int
    ) {

        if (minutes > 0) {

            focusDurationMinutes =
                minutes
        }
    }

    fun setCooldownDuration(
        minutes: Int
    ) {

        if (minutes > 0) {

            cooldownDurationMinutes =
                minutes
        }
    }

    fun startFocus(
        onStarted: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {

        val app =
            selectedApp

        if (app == null) {

            onError(
                "Please select an app first."
            )

            return
        }

        val manager =
            focusSessionManager

        if (manager == null) {

            onError(
                "Focus system is not initialized."
            )

            return
        }

        viewModelScope.launch {

            try {

                val emergencyCode =
                    manager.startFocus(

                        packageName =
                            app.packageName,

                        appName =
                            app.appName,

                        focusDurationMillis =
                            focusDurationMinutes
                                .toLong()
                                .times(60_000L),

                        cooldownDurationMillis =
                            cooldownDurationMinutes
                                .toLong()
                                .times(60_000L)
                    )

                _emergencyCode.value =
                    emergencyCode

                /*
                 * Show Focus Started notification.
                 */
                notificationManager
                    ?.showFocusStarted(

                        appName =
                            app.appName,

                        durationMinutes =
                            focusDurationMinutes
                    )

                refreshState()

                onStarted(
                    emergencyCode
                )

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to start Focus."
                )
            }
        }
    }

    fun cancelCooldown(
        sessionId: String,
        enteredCode: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {

        val manager =
            focusSessionManager
                ?: run {
                    onFailure()
                    return
                }

        viewModelScope.launch {

            try {

                val currentSession =
                    _focusState.value
                        .sessions
                        .firstOrNull {
                            it.id == sessionId
                        }

                val cancelled =
                    manager.cancelCooldownWithCode(

                        sessionId =
                            sessionId,

                        enteredCode =
                            enteredCode
                    )

                if (cancelled) {

                    _emergencyCode.value =
                        null

                    if (
                        currentSession != null
                    ) {

                        notificationManager
                            ?.cancelCooldownNotification()
                    }

                    refreshState()

                    onSuccess()

                } else {

                    onFailure()
                }

            } catch (_: Exception) {

                onFailure()
            }
        }
    }

    fun cancelCooldown(
        enteredCode: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {

        val session =
            _focusState.value
                .sessions
                .filter {
                    it.status ==
                            FocusStatus.COOLDOWN
                }
                .maxByOrNull {
                    it.cooldownStartTime
                }

        if (session == null) {

            onFailure()

            return
        }

        cancelCooldown(

            sessionId =
                session.id,

            enteredCode =
                enteredCode,

            onSuccess =
                onSuccess,

            onFailure =
                onFailure
        )
    }

    fun getActiveSessions():
            List<FocusSession> =
        _focusState.value.sessions

    fun getCooldownSessions():
            List<FocusSession> =
        _focusState.value.sessions
            .filter {
                it.status ==
                        FocusStatus.COOLDOWN
            }

    fun getFocusingSessions():
            List<FocusSession> =
        _focusState.value.sessions
            .filter {
                it.status ==
                        FocusStatus.FOCUSING
            }

    fun clearEmergencyCode() {

        _emergencyCode.value =
            null
    }

    override fun onCleared() {

        stateUpdateJob?.cancel()

        super.onCleared()
    }
}