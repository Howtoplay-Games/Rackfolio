package com.portproof.core.database

/**
 * Builds `LIKE` patterns from user input. Queries using these patterns must declare
 * `ESCAPE '\'` so that `%` and `_` typed by the user are matched literally.
 */
object SqlLike {
    const val ESCAPE_CHAR = '\\'

    /** `%query%` with wildcards escaped, or null for blank input (meaning "no text filter"). */
    fun containsPattern(query: String): String? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        val escaped = buildString(trimmed.length + 8) {
            trimmed.forEach { c ->
                if (c == ESCAPE_CHAR || c == '%' || c == '_') append(ESCAPE_CHAR)
                append(c)
            }
        }
        return "%$escaped%"
    }
}
