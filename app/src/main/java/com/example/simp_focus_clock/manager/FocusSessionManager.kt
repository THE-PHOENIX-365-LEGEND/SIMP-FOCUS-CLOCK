package com.example.simp_focus_clock.manager

import com.example.simp_focus_clock.data.AppPreferences
import com.example.simp_focus_clock.model.FocusSession
import com.example.simp_focus_clock.model.FocusState
import com.example.simp_focus_clock.model.FocusStatus
import com.example.simp_focus_clock.util.EmergencyCodeManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class FocusSessionManager(
    private val appPreferences: AppPreferences
) {

    val focusState: Flow<FocusState> =
        appPreferences.focusState

    companion object {
        const val MAX_SESSIONS = 10
    }

    suspend fun startFocus(
        packageName: String,
        appName: String,
        focusDurationMillis: Long,
        cooldownDurationMillis: Long
    ): String {

        require(packageName.isNotBlank()) {
            "Target application is required."
        }

        require(appName.isNotBlank()) {
            "Application name is required."
        }

        require(focusDurationMillis > 0L) {
            "Focus duration must be greater than zero."
        }

        require(cooldownDurationMillis > 0L) {
            "Cooldown duration must be greater than zero."
        }

        val currentState =
            appPreferences.focusState.first()

        val currentSessions =
            currentState.sessions

        require(
            currentSessions.size < MAX_SESSIONS
        ) {
            "Maximum of 10 active app sessions reached."
        }

        require(
            currentSessions.none {
                it.packageName == packageName
            }
        ) {
            "This app already has an active Focus session."
        }

        val now =
            System.currentTimeMillis()

        val focusEndTime =
            now + focusDurationMillis

        val cooldownStartTime =
            focusEndTime

        val cooldownEndTime =
            cooldownStartTime +
                    cooldownDurationMillis

        val emergencyCode =
            EmergencyCodeManager.generateCode()

        val emergencyCodeHash =
            EmergencyCodeManager.hashCode(
                emergencyCode
            )

        val session =
            FocusSession(

                id =
                    UUID.randomUUID()
                        .toString(),

                packageName =
                    packageName,

                appName =
                    appName,

                focusStartTime =
                    now,

                focusEndTime =
                    focusEndTime,

                cooldownStartTime =
                    cooldownStartTime,

                cooldownEndTime =
                    cooldownEndTime,

                status =
                    FocusStatus.FOCUSING,

                emergencyCodeHash =
                    emergencyCodeHash
            )

        val updatedSessions =
            currentSessions +
                    session

        appPreferences.saveFocusState(
            FocusState(
                sessions =
                    updatedSessions
            )
        )

        return emergencyCode
    }

    suspend fun updateStateFromCurrentTime():
            FocusState {

        val currentState =
            appPreferences.focusState.first()

        if (
            currentState.sessions.isEmpty()
        ) {
            return FocusState()
        }

        val now =
            System.currentTimeMillis()

        val updatedSessions =
            currentState.sessions
                .map { session ->

                    when (session.status) {

                        FocusStatus.IDLE -> {
                            session
                        }

                        FocusStatus.FOCUSING -> {

                            if (
                                now <
                                session.focusEndTime
                            ) {

                                session

                            } else {

                                session.copy(
                                    status =
                                        FocusStatus.COOLDOWN
                                )
                            }
                        }

                        FocusStatus.COOLDOWN -> {

                            if (
                                now <
                                session.cooldownEndTime
                            ) {

                                session

                            } else {

                                null
                            }
                        }
                    }
                }
                .filterNotNull()

        val updatedState =
            FocusState(
                sessions =
                    updatedSessions
            )

        if (
            updatedSessions !=
            currentState.sessions
        ) {

            appPreferences.saveFocusState(
                updatedState
            )
        }

        return updatedState
    }

    suspend fun cancelCooldownWithCode(
        sessionId: String,
        enteredCode: String
    ): Boolean {

        val currentState =
            appPreferences.focusState.first()

        val session =
            currentState.sessions
                .firstOrNull {
                    it.id == sessionId
                }
                ?: return false

        if (
            session.status !=
            FocusStatus.COOLDOWN
        ) {
            return false
        }

        val isCorrect =
            EmergencyCodeManager.verifyCode(
                enteredCode =
                    enteredCode,
                storedHash =
                    session.emergencyCodeHash
            )

        if (!isCorrect) {
            return false
        }

        val updatedSessions =
            currentState.sessions
                .filterNot {
                    it.id == sessionId
                }

        appPreferences.saveFocusState(
            FocusState(
                sessions =
                    updatedSessions
            )
        )

        return true
    }

    suspend fun stopFocusBeforeCooldown(
        sessionId: String
    ): Boolean {

        val currentState =
            appPreferences.focusState.first()

        val session =
            currentState.sessions
                .firstOrNull {
                    it.id == sessionId
                }
                ?: return false

        if (
            session.status !=
            FocusStatus.FOCUSING
        ) {
            return false
        }

        val updatedSessions =
            currentState.sessions
                .filterNot {
                    it.id == sessionId
                }

        appPreferences.saveFocusState(
            FocusState(
                sessions =
                    updatedSessions
            )
        )

        return true
    }

    suspend fun getActiveSessions():
            List<FocusSession> {

        return appPreferences
            .focusState
            .first()
            .sessions
            .filter {
                it.status !=
                        FocusStatus.IDLE
            }
    }
}