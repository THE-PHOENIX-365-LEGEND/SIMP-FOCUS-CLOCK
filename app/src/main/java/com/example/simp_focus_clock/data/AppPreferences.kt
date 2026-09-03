package com.example.simp_focus_clock.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.simp_focus_clock.model.FocusSession
import com.example.simp_focus_clock.model.FocusState
import com.example.simp_focus_clock.model.FocusStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.focusDataStore by preferencesDataStore(
    name = "focus_clock_preferences"
)

class AppPreferences(
    private val context: Context
) {

    private object Keys {

        val sessions =
            stringPreferencesKey("focus_sessions")
    }

    val focusState: Flow<FocusState> =
        context.focusDataStore.data.map { preferences ->

            val encodedSessions =
                preferences[Keys.sessions]
                    ?: ""

            val sessions =
                decodeSessions(
                    encodedSessions
                )

            FocusState(
                sessions = sessions
            )
        }

    suspend fun saveFocusState(
        state: FocusState
    ) {

        context.focusDataStore.edit { preferences ->

            preferences[Keys.sessions] =
                encodeSessions(
                    state.sessions
                )
        }
    }

    suspend fun saveSession(
        session: FocusSession
    ) {

        context.focusDataStore.edit { preferences ->

            val currentSessions =
                decodeSessions(
                    preferences[Keys.sessions]
                        ?: ""
                )

            val updatedSessions =
                currentSessions
                    .filterNot {
                        it.id == session.id
                    }
                    .toMutableList()

            updatedSessions.add(
                session
            )

            preferences[Keys.sessions] =
                encodeSessions(
                    updatedSessions
                )
        }
    }

    suspend fun removeSession(
        sessionId: String
    ) {

        context.focusDataStore.edit { preferences ->

            val currentSessions =
                decodeSessions(
                    preferences[Keys.sessions]
                        ?: ""
                )

            val updatedSessions =
                currentSessions.filterNot {
                    it.id == sessionId
                }

            preferences[Keys.sessions] =
                encodeSessions(
                    updatedSessions
                )
        }
    }

    suspend fun clearFocusState() {

        context.focusDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun encodeSessions(
        sessions: List<FocusSession>
    ): String {

        return sessions
            .take(10)
            .joinToString(
                separator = "||"
            ) { session ->

                listOf(
                    session.id,
                    session.packageName,
                    session.appName,
                    session.focusStartTime.toString(),
                    session.focusEndTime.toString(),
                    session.cooldownStartTime.toString(),
                    session.cooldownEndTime.toString(),
                    session.status.name,
                    session.emergencyCodeHash
                ).joinToString(
                    separator = "|"
                ) {
                    escape(it)
                }
            }
    }

    private fun decodeSessions(
        encoded: String
    ): List<FocusSession> {

        if (encoded.isBlank()) {
            return emptyList()
        }

        return encoded
            .split("||")
            .mapNotNull { item ->

                try {

                    val values =
                        item
                            .split("|")
                            .map {
                                unescape(it)
                            }

                    if (values.size != 9) {
                        return@mapNotNull null
                    }

                    FocusSession(

                        id =
                            values[0],

                        packageName =
                            values[1],

                        appName =
                            values[2],

                        focusStartTime =
                            values[3].toLong(),

                        focusEndTime =
                            values[4].toLong(),

                        cooldownStartTime =
                            values[5].toLong(),

                        cooldownEndTime =
                            values[6].toLong(),

                        status =
                            FocusStatus.valueOf(
                                values[7]
                            ),

                        emergencyCodeHash =
                            values[8]
                    )

                } catch (
                    _: Exception
                ) {

                    null
                }
            }
            .take(10)
    }

    private fun escape(
        value: String
    ): String {

        return value
            .replace(
                "\\",
                "\\\\"
            )
            .replace(
                "|",
                "\\p"
            )
            .replace(
                "\n",
                "\\n"
            )
    }

    private fun unescape(
        value: String
    ): String {

        return value
            .replace(
                "\\n",
                "\n"
            )
            .replace(
                "\\p",
                "|"
            )
            .replace(
                "\\\\",
                "\\"
            )
    }
}