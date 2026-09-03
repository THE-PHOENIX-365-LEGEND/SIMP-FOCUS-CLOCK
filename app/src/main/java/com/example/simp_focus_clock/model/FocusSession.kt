package com.example.simp_focus_clock.model

data class FocusSession(

    val id: String,

    val packageName: String,

    val appName: String,

    val focusStartTime: Long,

    val focusEndTime: Long,

    val cooldownStartTime: Long,

    val cooldownEndTime: Long,

    val status: FocusStatus,

    val emergencyCodeHash: String = ""
)