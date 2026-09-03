package com.example.simp_focus_clock.alarm

data class AlarmItem(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String = "Alarm",
    val enabled: Boolean = true,
    val repeatDays: Set<Int> = emptySet()
)