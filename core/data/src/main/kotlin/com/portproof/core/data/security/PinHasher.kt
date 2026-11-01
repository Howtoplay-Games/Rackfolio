package com.portproof.core.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted PBKDF2-HMAC-SHA256 (available on every supported Android version, API 26+). The iteration
 * count is stored with each hash so it can be raised later without invalidating existing PINs.
 */
internal class PinHasher(val iterations: Int = DEFAULT_ITERATIONS) {

    fun newSalt(): ByteArray = ByteArray(SALT_BYTES).also { random.nextBytes(it) }

    fun hash(pin: String, salt: ByteArray, iterations: Int = this.iterations): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    /** Constant-time comparison against a stored hash. */
    fun matches(pin: String, salt: ByteArray, iterations: Int, expected: ByteArray): Boolean =
        MessageDigest.isEqual(hash(pin, salt, iterations), expected)

    companion object {
        const val DEFAULT_ITERATIONS = 120_000
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val SALT_BYTES = 16
        private const val KEY_BITS = 256
        private val random = SecureRandom()
    }
}
