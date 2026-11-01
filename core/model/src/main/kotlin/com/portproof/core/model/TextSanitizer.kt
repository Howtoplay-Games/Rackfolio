package com.portproof.core.model

/**
 * Normalises free text before it is validated and stored.
 *
 * - Removes control characters, bidirectional overrides and byte-order marks, which can be used to
 *   disguise text in exported reports.
 * - Single-line values: line breaks and tabs become spaces, runs of whitespace collapse to one.
 * - Multi-line values: line endings are normalised to `\n`, tabs are kept.
 */
object TextSanitizer {
    private val bidiControls = setOf(
        '\u202A', '\u202B', '\u202C', '\u202D', '\u202E',
        '\u2066', '\u2067', '\u2068', '\u2069', '\uFEFF',
    )
    private val whitespaceRun = Regex("\\s+")

    fun singleLine(raw: String): String {
        val cleaned = buildString(raw.length) {
            raw.forEach { c ->
                when {
                    c == '\n' || c == '\r' || c == '\t' -> append(' ')
                    c.isUnsafe() -> Unit
                    else -> append(c)
                }
            }
        }
        return cleaned.replace(whitespaceRun, " ").trim()
    }

    fun multiLine(raw: String): String {
        val normalised = raw.replace("\r\n", "\n").replace('\r', '\n')
        val cleaned = buildString(normalised.length) {
            normalised.forEach { c -> if (c == '\n' || c == '\t' || !c.isUnsafe()) append(c) }
        }
        return cleaned.lines().joinToString("\n") { it.trimEnd() }.trim()
    }

    /** Length in Unicode code points, so emoji and other supplementary characters count once. */
    fun length(value: String): Int = value.codePointCount(0, value.length)

    private fun Char.isUnsafe(): Boolean = Character.isISOControl(this) || this in bidiControls
}
