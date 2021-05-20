package blue.mild.covid.vaxx.isin

/**
 * Configuration for /api/v1/vakcinace/VytvorNeboZmenVakcinaci
 */
data class VytvorNeboZmenVakcinaci(

        val pacientId: String,

        val typOckovaniKod: String,

        val indikace: List<String>,

        val pracovnik: Pracovnik,

        val indikaceJina: String? = null,
)


data class Pracovnik(
        // Todle bylo pracovnikNrzpCislo,
        // zmenil jsem to podle /api/v1/pacienti/AktualizujKontaktniUdajePacienta
        val nrzpCislo: String,

        // todle jsem pridal
        // /api/v1/pacienti/AktualizujKontaktniUdajePacienta
        val rodneCislo: String,

        val pcz: String,


)


/**
 * Configuration for /api/v1/vakcinace/VytvorNeboZmenVakcinaci
 */
data class VytvorNeboZmenDavku(

        val vakcinaceId: String,

        val ockovaciLatkaKod: String,

        val sarze: String,

        val datumVakcinace: String,

        val expirace: String,

        val pracovnik: Pracovnik,

        val stav: String,

        val mistoAplikaceKod: String
)


/**
 * Configuration for /api/v1/vakcinace/VytvorNeboZmenVakcinaci
 */
data class AktualizujPacienta(

        val idPacienta: String,

        val kontaktniMobilniTelefon: String,

        val kontaktniEmail: String,

        val pracovnik: Pracovnik
)