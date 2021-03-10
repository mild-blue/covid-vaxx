package blue.mild.covid.vaxx.security.ddos

interface RequestVerificationService {
    /**
     * Verifies that the request coming to API is from the real user.
     */
    suspend fun verify(token: String, host: String? = null)
}
