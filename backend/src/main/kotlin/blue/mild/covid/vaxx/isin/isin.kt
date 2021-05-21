@file:Suppress("TooManyFunctions")
package blue.mild.covid.vaxx.isin

import blue.mild.covid.vaxx.utils.createLogger
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.receive
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.ServerResponseException
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
import pw.forst.katlib.getEnv
import java.io.File
import java.security.KeyStore

enum class IsinEnvironment {
    PUBLIC,
    TEST,
    PRODUCTION,
}

// configure which environment should be used
private val useEnvironment = IsinEnvironment.TEST

private val logger = createLogger("HttpClientConfiguration")
private const val PUBLIC_ROOT = "https://apidoc.uzis.cz/api/v1"
private const val TEST_ROOT = "https://apitest.uzis.cz/api/v1"
private const val PRODUCTION_ROOT = "https://api.uzis.cz/api/v1"

// 000 je pro polikliniky - neni to placeholder
// https://nrpzs.uzis.cz/detail-66375-clinicum-a-s.html#fndtn-detail_uzis
private const val PCZ = "000"
private const val NRZP_CISLO = "184070832"
// rodne cislo pracovnika je z PDFka
private val pracovnikO = Pracovnik(pcz = PCZ, nrzpCislo = NRZP_CISLO, rodneCislo = "9910190015")
private const val URL_VYTVOR_NEBO_ZMEN_VAKCINACI = "vakcinace/VytvorNeboZmenVakcinaci"
private const val URL_VYTVOR_NEBO_ZMEN_DAVKU = "vakcinace/VytvorNeboZmenDavku"
private const val URL_ZMEN_STAV_VAKCINACE = "vakcinace/ZmenStavVakcinace"
private const val URL_VAKCINACE_DLE_ID = "vakcinace/NacistVakcinaciDleId"
private const val URL_VAKCINACE_PACIENTA = "vakcinace/NacistVakcinacePacienta"
private const val URL_VAKCINACE_SEZNAM_REGISTRACI_A_REZERVACI = "vakcinace/SeznamRegistraciARezervaci"
private const val URL_NACTI_PRACOVNIKY = "nrzp/NactiPracovniky"
private const val URL_NACTI_PRACOVNIKA = "nrzp/NactiPracovnika"
private const val URL_AKTUALIZUJ_KONTAKTNI_UDAJE_PACIENTA = "pacienti/AktualizujKontaktniUdajePacienta"
private const val URL_NAJDI_PACIENTA = "pacienti/VyhledatDleJmenoPrijmeniRc"


private val roots = mapOf(
    IsinEnvironment.PUBLIC to PUBLIC_ROOT,
    IsinEnvironment.TEST to TEST_ROOT,
    IsinEnvironment.PRODUCTION to PRODUCTION_ROOT,
)

// Dummy class to wrap data around pacient
data class InputPacient(
    val jmeno: String,
    val prijmeni: String,
    val rodneCislo: String
)
private val patients = mapOf(
    IsinEnvironment.PUBLIC to InputPacient("Jan", "Kubant", "9002030015"),
    IsinEnvironment.TEST to InputPacient("VICTOR", "BUDIUC", "8208258201"),
    IsinEnvironment.PRODUCTION to InputPacient("Jan", "Kubant", "9002030015"),
)

// Dummy class to wrap data around paracovnik
data class InputPracovnik(
    val cislo: String,
    val jmeno: String,
    val prijmeni: String,
    val datumNarozeni: String,
    val rodneCislo: String,
    val pcz: String,
)
private val workers = mapOf(
    // Public - hardcoded one
    IsinEnvironment.PUBLIC to InputPracovnik(pracovnikO.nrzpCislo, "", "", datumNarozeni = "", rodneCislo = pracovnikO.rodneCislo, pcz=PCZ),
    // Test is one worker received by nactiPracovniky
    IsinEnvironment.TEST to InputPracovnik("172319367", "Jmeno2", "Prijmeni26", "1924-05-10T00:00:00", "245510064", PCZ),
    // Prod - hardcoded one
    IsinEnvironment.PRODUCTION to InputPracovnik(pracovnikO.nrzpCislo, "", "", datumNarozeni = "", rodneCislo = pracovnikO.rodneCislo, PCZ),
)

private val root = roots.getValue(useEnvironment)
private val pacient = patients.getValue(useEnvironment)
private val iPracovnik = workers.getValue(useEnvironment)
private val pracovnik = Pracovnik(nrzpCislo= iPracovnik.cislo, rodneCislo = iPracovnik.rodneCislo, pcz = iPracovnik.pcz)


private val userIdentification = "?pcz=${pracovnik.pcz}&pracovnikNrzpCislo=${pracovnik.nrzpCislo}"

private val configuration = KeyStoreConfiguration(
    storePass = getEnv("ISIN_STORE_PASS") ?: "",
    storePath = getEnv("ISIN_STORE_PATH") ?: "/home/honza/Desktop/rgu_ws_44797362.pfx",
    storeType = getEnv("ISIN_STORE_TYPE") ?: "JKS",
    keyPass = getEnv("ISIN_KEY_PASS") ?: ""
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

        if (useEnvironment != IsinEnvironment.PUBLIC) {
            configureCertificates(config)
        }
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
//
///**
// * Debug logger for HTTP requests.
// */
//private val Logger.Companion.DEBUG: Logger
//    get() = object : Logger, org.slf4j.Logger by createLogger("HttpCallsLogging") {
//        override fun log(message: String) {
//            debug(message)
//        }
//    }

/**
 * Trace logger for HTTP Requests.
 */
private val Logger.Companion.TRACE: Logger
    get() = object : Logger, org.slf4j.Logger by createLogger("HttpCallsLogging") {
        override fun log(message: String) {
            trace(message)
        }
    }

private fun createIsinURL(requestUrl: String, baseUrl: String = root, parameters: List<Any> = listOf(), includeIdentification: Boolean = true): String {
    val parametersUrl = parameters.map { it.toString() }.joinToString(separator = "/")
    return "$baseUrl/$requestUrl/$parametersUrl${if (includeIdentification) userIdentification else ""}"
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
    val url = createIsinURL(URL_NAJDI_PACIENTA, parameters = listOf(jmeno, prijmeni, rodneCislo))
    val response = getUrl(isinClient, url)
    return response.get("pacient").get("id").textValue()
}

suspend fun aktualizujKontaktniUdajePacienta(isinClient: HttpClient, data: AktualizujPacienta): JsonNode {
    val url = createIsinURL(URL_AKTUALIZUJ_KONTAKTNI_UDAJE_PACIENTA)
    return postUrlData(isinClient, url, data)
}

suspend fun vytvorVakcinaci(isinClient: HttpClient, data: VytvorNeboZmenVakcinaci): String? {
    val url = createIsinURL(URL_VYTVOR_NEBO_ZMEN_VAKCINACI)
    val response = postUrlData(isinClient, url, data)
    return response.get("id").textValue()
}


suspend fun vytvorDavku(isinClient: HttpClient, data: VytvorNeboZmenDavku): JsonNode {
    val url = createIsinURL(URL_VYTVOR_NEBO_ZMEN_DAVKU)
    return postUrlData(isinClient, url, data)
}


suspend fun uzavriVakcinaci(isinClient: HttpClient, idVakcinace: String): JsonNode {
    val url = createIsinURL(URL_ZMEN_STAV_VAKCINACE, parameters = listOf(idVakcinace, "Ukoncene"))
    return postUrlData(isinClient, url)
}

suspend fun getVakcinaceDleId(isinClient: HttpClient, idVakcinace: String): JsonNode {
    val url = createIsinURL(URL_VAKCINACE_DLE_ID, parameters = listOf(idVakcinace))
    val response = getUrl(isinClient, url)
    return response.get("davky")
}

suspend fun getVakcinaceDlePacienta(isinClient: HttpClient, idPacient: String): JsonNode {
    val url = createIsinURL(URL_VAKCINACE_PACIENTA, parameters = listOf(idPacient))
    return getUrl(isinClient, url)
}

suspend fun getVakcinaceSeznamRegistraciARezervaci(isinClient: HttpClient, idPacient: String): JsonNode {
    val url = createIsinURL(URL_VAKCINACE_SEZNAM_REGISTRACI_A_REZERVACI, parameters = listOf(idPacient))
    return getUrl(isinClient, url)
}

suspend fun nactiPracovnika(isinClient: HttpClient, pracovnikCislo: String): JsonNode {
    val url = "${createIsinURL(URL_NACTI_PRACOVNIKA, includeIdentification = false)}?pracovnikCislo=${pracovnikCislo}"
    return getUrl(isinClient, url)
}

suspend fun nactiPracovniky(isinClient: HttpClient): JsonNode {
    val url = createIsinURL(URL_NACTI_PRACOVNIKY)
    return getUrl(isinClient, url)
}

@Suppress("LongMethod")
fun main() {
    println("Using environment: ${useEnvironment} => ${root}; ${pacient}; ${pracovnik}")

    runBlocking {

        val isinClient = client(configuration)

        val idPacienta = getPatientId(isinClient, pacient.jmeno, pacient.prijmeni, pacient.rodneCislo)!!

        // I have hardcoded one pracovnik in configuration
        // nactiPracovniky(isinClient)

        val loadedPracovnik = nactiPracovnika(isinClient, pracovnik.nrzpCislo)
        println(loadedPracovnik.toPrettyString())

        // pracovnik bez rodneho cisla se vymlel, pracovnik s RC od pacienta vraci 404

        try {
            val aktualizace = aktualizujKontaktniUdajePacienta(
                isinClient, AktualizujPacienta(
                    idPacienta = idPacienta,
                    kontaktniEmail = "test@test.cz",
                    kontaktniMobilniTelefon = "123456789",
                    pracovnik = pracovnik
                )
            )
            println(aktualizace.toPrettyString())
        } catch (e: ServerResponseException) {
            println(e)
        } catch (e: ClientRequestException) {
            println(e)
        }

        try {
            val vakcinace = getVakcinaceDlePacienta(isinClient, idPacienta)
            println("Pocet vakcinaci: ${vakcinace.size()}")
            println(vakcinace.toPrettyString())
        } catch (e: ServerResponseException) {
            println(e)
        } catch (e: ClientRequestException) {
            println(e)
        }

        try {
            val seznam = getVakcinaceSeznamRegistraciARezervaci(isinClient, idPacienta)
            println("SeznamRegistraciARezervaci: ${seznam.size()}")
            println(seznam.toPrettyString())
        } catch (e: ServerResponseException) {
            println(e)
        } catch (e: ClientRequestException) {
            println(e)
        }

        // typOckovaniKod a indikace - copy-paste z prikladu
        val vakcinaceId = (
                vytvorVakcinaci(
                    isinClient,
                    VytvorNeboZmenVakcinaci(
                        pacientId = idPacienta,
                        typOckovaniKod = "CO19",
                        indikace=listOf("C03", "J01"),
                        pracovnik = pracovnik,
                        // {"errors":[{"fieldName":"IndikaceJina","message":"Jiná popis je povinné pole"}]}
                        // podle dokumentace to neni povinny parametr
                        indikaceJina = "Nejaky nahodny duvod - ma to byt"
                    )
                ) ?: "fix_for_test")
            ?: throw IllegalArgumentException("Vaccination creation failed, no vaccitaion id was returned")

        println("vakcinaceId: ${vakcinaceId}")

        // datum vakcinace musi byt v minulosti
        val vytvor1 = vytvorDavku(
            isinClient, VytvorNeboZmenDavku(
                datumVakcinace = "2021-05-10T00:00:00",
                vakcinaceId = vakcinaceId,
                ockovaciLatkaKod = "CO01",
                sarze = "J1234",
                expirace = "2021-05-12T00:00:00",
                pracovnik = pracovnik,
                mistoAplikaceKod = "NP",
                stav = "Probihajici"
            )
        )
        println(vytvor1.toPrettyString())
        val vysledneDavky1 = getVakcinaceDleId(isinClient, vakcinaceId)
        println("Pocet davek ${vysledneDavky1.size()}")
        println(vysledneDavky1)

        // datum vakcinace musi byt v minulosti
        val vytvor2 = vytvorDavku(
            isinClient, VytvorNeboZmenDavku(
                datumVakcinace = "2021-05-11T00:00:00",
                vakcinaceId = vakcinaceId,
                ockovaciLatkaKod = "CO01",
                sarze = "J1234",
                expirace = "2021-05-13T00:00:00",
                pracovnik = pracovnik,
                mistoAplikaceKod = "NP",
                stav = "Ukoncene"
            )
        )
        println(vytvor2.toPrettyString())

        val vysledneDavky2 = getVakcinaceDleId(isinClient, vakcinaceId)
        println("Pocet davek ${vysledneDavky2.size()}")
        println(vysledneDavky2)
    }

}
