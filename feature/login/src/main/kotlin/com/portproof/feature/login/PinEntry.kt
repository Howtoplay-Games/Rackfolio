package com.portproof.feature.login

import com.portproof.core.model.PinRules

/** Digits typed so far on a keypad (never longer than a PIN). */
@JvmInline
value class PinEntry(val digits: String = "") {
    val isComplete: Boolean get() = digits.length == PinRules.LENGTH

    fun plus(digit: Char): PinEntry =
        if (digit in '0'..'9' && digits.length < PinRules.LENGTH) PinEntry(digits + digit) else this

    fun backspace(): PinEntry = PinEntry(digits.dropLast(1))
}
