package blue.mild.covid.vaxx.service.dummy

import blue.mild.covid.vaxx.security.ddos.RequestVerificationService
import mu.KLogging

class DummyRequestVerificationService : RequestVerificationService {
    private companion object : KLogging()

    override suspend fun verify(token: String, host: String?) {
        logger.warn { "NOT VERIFYING request with token $token from host $host." }
    }
}
