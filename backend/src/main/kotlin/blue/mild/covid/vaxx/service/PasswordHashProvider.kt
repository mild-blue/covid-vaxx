package blue.mild.covid.vaxx.service

import com.lambdaworks.crypto.SCryptUtil
import mu.KLogging

class PasswordHashProvider {

    private companion object : KLogging() {
        // see https://github.com/wg/scrypt
        const val N = 16384
        const val r = 8
        const val p = 1
    }

    fun hashPassword(password: String): String = SCryptUtil.scrypt(password, N, r, p)
    fun verifyPassword(password: String, passwordHash: String): Boolean = SCryptUtil.check(password, passwordHash)
}
