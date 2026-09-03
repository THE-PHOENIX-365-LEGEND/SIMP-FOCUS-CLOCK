package com.example.simp_focus_clock.util

import java.security.MessageDigest
import java.security.SecureRandom

object EmergencyCodeManager {

    private val secureRandom = SecureRandom()

    fun generateCode(): String {
        return secureRandom
            .nextInt(1_000_000)
            .toString()
            .padStart(6, '0')
    }

    fun hashCode(code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")

        val hash = digest.digest(
            code.toByteArray(Charsets.UTF_8)
        )

        return hash.joinToString("") { byte ->
            "%02x".format(byte)
        }
    }

    fun verifyCode(
        enteredCode: String,
        storedHash: String
    ): Boolean {
        if (enteredCode.length != 6) {
            return false
        }

        val enteredHash = hashCode(enteredCode)

        return MessageDigest.isEqual(
            enteredHash.toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )
    }
}