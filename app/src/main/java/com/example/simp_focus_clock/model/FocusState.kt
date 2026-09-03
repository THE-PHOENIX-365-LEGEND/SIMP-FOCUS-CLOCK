package com.example.simp_focus_clock.model

data class FocusState(

    /*
     * New system:
     * Each app gets its own independent FocusSession.
     *
     * Maximum active sessions will be limited to 10
     * by the session manager in the next steps.
     */
    val sessions: List<FocusSession> = emptyList(),

    /*
     * Legacy fields are temporarily kept so the existing
     * AppPreferences and FocusSessionManager continue to
     * compile during the migration.
     *
     * These will be removed after the new multi-session
     * system is fully connected.
     */
    val selectedPackageName: String = "",

    val selectedAppName: String = "",

    val focusStartTime: Long = 0L,

    val focusEndTime: Long = 0L,

    val cooldownStartTime: Long = 0L,

    val cooldownEndTime: Long = 0L,

    val status: FocusStatus = FocusStatus.IDLE,

    val emergencyCodeHash: String = ""
)