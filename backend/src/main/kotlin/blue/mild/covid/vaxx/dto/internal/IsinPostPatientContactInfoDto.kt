package blue.mild.covid.vaxx.dto.internal


// Fields according to api documentation https://apidoc.uzis.cz/index.html - /api/v1/pacienti/AktualizujKontaktniUdajePacienta
data class IsinPostPatientContactInfoDto (
    val zdravotniPojistovnaKod: String?,
    val kontaktniMobilniTelefon: String?,
    val kontaktniEmail: String?,
    val kontaktniPevnaLinka: String?,
    val pobytMesto: String?,
    val pobytPsc: String?,
    val notifikovatEmail: Boolean,
    val notifikovatSms: Boolean,
    val poznamka: String?,
    val id: String?,

    val jmeno: String,
    val prijmeni: String,
    val datumNarozeni: String,
    val cisloPojistence: String,
    val cisloObcanskehoPrukazu: String?,
    val cisloPasu: String?,
    val zemeObcanstviKod: String,
    val datumUmrti: String?,
    val pohlavi: String?,
)
