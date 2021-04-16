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
private val publicRoot = "https://apidoc.uzis.cz/api/v1/"
private val testRoot = "https://apitest.uzis.cz/api/v1/"
private val productionRoot = "https://api.uzis.cz/api/v1/"
private val root = productionRoot
private val urlCiselnik = "ciselniky/Nacti/AplikacniCesty"


private val urlVytvorNeboZmenVakcinaci = "vakcinace/VytvorNeboZmenVakcinaci"
private val urlZmenStavVakcinace = "vakcinace/ZmenStavVakcinace"
private val urlVakcinace = "vakcinace/NacistVakcinaciDleId"
private val urlNactiPracovniky = "nrzp/NactiPracovniky"

private val vytvorVakcinaci = VytvorNeboZmenVakcinaci(
        pacientId = "1",
        typOckovaniKod = "1",
        indikace = listOf("1"),
        pracovnik = Pracovnik("1")
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

suspend fun vytvorVakcinaci(isinClient: HttpClient, data: VytvorNeboZmenVakcinaci): HttpResponse {
    val requestUrl = "$root$urlVytvorNeboZmenVakcinaci"
    return isinClient.post<HttpResponse>(requestUrl) { // should be disabled
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
        body = data
    }
}

suspend fun uzavriVakcinaci(isinClient: HttpClient,
                            idVakcinace: Int,
                            pcz: String): HttpResponse {
    val requestUrl = "$root$urlZmenStavVakcinace/$idVakcinace/Indikovano/?pcz=$pcz&pracovnikNrzpCislo=184070832"
    return isinClient.post<HttpResponse>(requestUrl) {
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.
        Json)
        body = {}
    }
}

suspend fun getUrl(isinClient: HttpClient, url: String = urlNactiPracovniky): HttpResponse {
    val requestUrl = "$root$url"
    return isinClient.get<HttpResponse>(requestUrl)
}

suspend fun getVakcinace(isinClient: HttpClient, id: Int, pcz: String): HttpResponse {
    val requestUrl = "$root$urlVakcinace/$id?pcz=$pcz&pracovnikRodneCislo=415915954"
    return isinClient.get<HttpResponse>(requestUrl)
}

fun main() {
    runBlocking {

        val isinClient = client(configuration)

        val response = getUrl(isinClient)
        print(response.receive<JsonNode>())

//        val response = getUrl(isinClient,"pacienti/VyhledatMePacienty?cisloPojistence=9002030015&pracovnikRodneCislo=415915954&pcz=000")
//        print(response.receive<JsonNode>())


//        val response = getUrl(isinClient,"vakcinace/NacistMojeOckovani/4-1-2021/12-31-2021?pcz=000&pracovnikNrzpCislo=1")
//        print(response.receive<JsonNode>())

//        val responseCiselnik = getVakcinace(isinClient, 1000, "000")
//        print(responseCiselnik.receive<JsonNode>())

//        val responseVytvor = vytvorVakcinaci(isinClient, vytvorVakcinaci)
//        print(responseVytvor.receive<JsonNode>())
//
//        val responseUzavri = uzavriVakcinaci(isinClient, 1, "000")
//        print(responseUzavri.receive<JsonNode>())
    }

}
