package blue.mild.covid.vaxx.isin

import blue.mild.covid.vaxx.utils.createLogger
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.receive
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.apache.http.ssl.SSLContextBuilder
import java.io.File
import java.security.KeyStore


private val logger = createLogger("HttpClientConfiguration")
private val publicRoot = "https://apidoc.uzis.cz/api/v1"
private val testRoot = "https://apitest.uzis.cz/api/v1"
private val productionRoot = "https://api.uzis.cz/api/v1"
private val pcz = "000"
private val NrzpCislo = "184070832"
private val userIdentification = "?pcz=$pcz&pracovnikNrzpCislo=$NrzpCislo"
private val pracovnik = Pracovnik(pcz = pcz, pracovnikNrzpCislo = NrzpCislo)
private val root = publicRoot

private val urlVytvorNeboZmenVakcinaci = "vakcinace/VytvorNeboZmenVakcinaci"
private val urlVytvorNeboZmenDavku = "vakcinace/VytvorNeboZmenDavku"
private val urlZmenStavVakcinace = "vakcinace/ZmenStavVakcinace"
private val urlVakcinace = "vakcinace/NacistVakcinaciDleId"
private val urlNactiPracovniky = "nrzp/NactiPracovniky"
private val urlAktualizujKontaktniUdajePacienta = "pacienti/AktualizujKontaktniUdajePacienta"
private val urlNajdiPacienta = "pacienti/VyhledatDleJmenoPrijmeniRc"

private val vytvorVakcinaci = VytvorNeboZmenVakcinaci(
    pacientId = "1",
    typOckovaniKod = "1",
    indikace = listOf("1"),
    pracovnik = pracovnik
)

private val configuration = KeyStoreConfiguration(
    storePass = "",
    storePath = "/home/honza/Desktop/rgu_ws_44797362.pfx",
    storeType = "JKS",
    keyPass = ""
)

/**
 * Prepares HTTP Client with given keystore.
 */
fun client(
    config: KeyStoreConfiguration
) =
    HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }

        install(Logging) {
            logger = Logger.TRACE
            level = LogLevel.ALL
        }

//            configureCertificates(config)
    }


/**
 * Tries to read and create key store.
 */
private fun readStore(config: KeyStoreConfiguration): KeyStore? =
    runCatching {
        File(config.storePath).inputStream().use {
            KeyStore.getInstance(config.storeType).apply {
                load(it, config.storePass.toCharArray())
            }
        }
    }.onFailure {
        logger.error(it) { "It was not possible to load key store!" }
    }.onSuccess {
        logger.debug { "KeyStore loaded." }
    }.getOrNull()


/**
 * Prepares client engine and sets certificates from [config].
 */
private fun HttpClientConfig<ApacheEngineConfig>.configureCertificates(config: KeyStoreConfiguration) {
    engine {
        customizeClient {
            setSSLContext(
                SSLContextBuilder
                    .create()
                    .loadKeyMaterial(readStore(config), config.keyPass.toCharArray())
                    .build()
            )
        }
    }
}

/**
 * Debug logger for HTTP requests.
 */
private val Logger.Companion.DEBUG: Logger
    get() = object : Logger, org.slf4j.Logger by createLogger("HttpCallsLogging") {
        override fun log(message: String) {
            debug(message)
        }
    }

/**
 * Trace logger for HTTP Requests.
 */
private val Logger.Companion.TRACE: Logger
    get() = object : Logger, org.slf4j.Logger by createLogger("HttpCallsLogging") {
        override fun log(message: String) {
            trace(message)
        }
    }

private fun createIsinURL(requestUrl: String, baseUrl: String = root, parameters: List<Any> = listOf()): String {
    val parametersUrl = parameters.map { it.toString() }.joinToString(separator = "/")
    return "$baseUrl/$requestUrl/$parametersUrl$userIdentification"
}

suspend fun getUrl(isinClient: HttpClient, url: String): JsonNode {
    return isinClient.get<HttpResponse>(url).receive<JsonNode>()
}

suspend fun postUrlData(
    isinClient: HttpClient,
    url: String,
    data: Any = {}
): JsonNode {

    return isinClient.post<HttpResponse>(url) {
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
        body = data
    }.receive<JsonNode>()
}

suspend fun getPatientId(isinClient: HttpClient, jmeno: String, prijmeni: String, rodneCislo: String): String? {
    val url = createIsinURL(urlNajdiPacienta, parameters = listOf(jmeno, prijmeni, rodneCislo))
    val response = getUrl(isinClient, url)
    return response.get("pacient").get("id").textValue()
}

suspend fun aktualizujKontaktniUdajePacienta(isinClient: HttpClient, data: AktualizujPacienta): JsonNode {
    val url = createIsinURL(urlAktualizujKontaktniUdajePacienta)
    return postUrlData(isinClient, url, data)
}

suspend fun vytvorVakcinaci(isinClient: HttpClient, data: VytvorNeboZmenVakcinaci): String? {
    val url = createIsinURL(urlVytvorNeboZmenVakcinaci)
    val response = postUrlData(isinClient, url, data)
    return response.get("id").textValue()
}


suspend fun vytvorDavku(isinClient: HttpClient, data: VytvorNeboZmenDavku): JsonNode {
    val url = createIsinURL(urlVytvorNeboZmenDavku)
    return postUrlData(isinClient, url, data)
}


suspend fun uzavriVakcinaci(isinClient: HttpClient, idVakcinace: String): JsonNode {
    val url = createIsinURL(urlZmenStavVakcinace, parameters = listOf(idVakcinace, "Ukoncene"))
    return postUrlData(isinClient, url)
}

suspend fun getDavkyVakcinace(isinClient: HttpClient, id: String): JsonNode {
    val url = createIsinURL(urlVakcinace, parameters = listOf(id))
    val response = getUrl(isinClient, url)
    return response.get("davky")
}

fun main() {
    runBlocking {

        val isinClient = client(configuration)

        val id = getPatientId(isinClient, "Jan", "Kubant", "9002030015")!!

        aktualizujKontaktniUdajePacienta(
            isinClient, AktualizujPacienta(
                idPacienta = id,
                kontaktniEmail = "test@test.cz",
                kontaktniMobilniTelefon = "123456789",
                pracovnik = Pracovnik(pcz = pcz, pracovnikNrzpCislo = NrzpCislo)
            )
        )
        val vakcinaceId = (vytvorVakcinaci(isinClient, vytvorVakcinaci) ?: "fix_for_test")
            ?: throw IllegalArgumentException("Vaccination creation failed, no vaccitaion id was returned")
        vytvorDavku(
            isinClient, VytvorNeboZmenDavku(
                datumVakcinace = "2020-12-10T00:00:00",
                vakcinaceId = vakcinaceId,
                ockovaciLatkaKod = "kod",
                sarze = "sarze",
                expirace = "2020-12-10T00:00:00",
                pracovnik = pracovnik,
                mistoAplikaceKod = "dr",
                stav = "Ukoncene"
            )
        )
        val vysledneDavky = getDavkyVakcinace(isinClient, vakcinaceId)
        println("Pocet davek ${vysledneDavky.size()}")
        println(vysledneDavky)
    }

}
