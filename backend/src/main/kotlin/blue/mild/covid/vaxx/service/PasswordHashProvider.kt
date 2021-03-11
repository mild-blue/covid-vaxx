package blue.mild.covid.vaxx.service

import com.lambdaworks.crypto.SCryptUtil
import mu.KLogging

/**
 * Provider which hashes passwords.
 */
class PasswordHashProvider {

    private companion object : KLogging() {
        // see https://github.com/wg/scrypt
        const val N = 16384
        const val r = 8
        const val p = 1
    }

    /**
     * Creates hash from given [password] using SCrypt algorithm.
     */
    fun hashPassword(password: String): String = SCryptUtil.scrypt(password, N, r, p)

    /**
     * Verifies that the [password] matches given [passwordHash].
     * Returns true if they match, returns false otherwise.
     */
    fun verifyPassword(password: String, passwordHash: String): Boolean = SCryptUtil.check(password, passwordHash)
}
