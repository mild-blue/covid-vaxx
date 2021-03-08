package blue.mild.covid.vaxx.security.auth

class CaptchaAuthenticationService {

    /**
     * Verify [token] from Google captcha, if the token is valid, returns [PatientPrincipal].
     * Otherwise throws Authent
     */
    suspend fun authenticate(token: String): PatientPrincipal {
        // TODO somehow verify token with Google that given token is correct
        return PatientPrincipal
    }
}
