package com.portproof.core.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Local, device-only preferences. No account is required to use PortProof. */
data class UserPreferences(
    val technicianName: String = "",
    val companyName: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

object ProfileRules {
    const val TECHNICIAN_NAME_MAX_LENGTH = 80
    const val COMPANY_NAME_MAX_LENGTH = 120

    /** Returns the sanitised value, or null when it exceeds [maxLength]. Blank input clears the value. */
    fun sanitize(raw: String, maxLength: Int): String? {
        val value = TextSanitizer.singleLine(raw)
        return if (TextSanitizer.length(value) > maxLength) null else value
    }
}
