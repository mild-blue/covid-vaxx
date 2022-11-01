package blue.mild.covid.vaxx.dto.internal

import blue.mild.covid.vaxx.isin.Pracovnik

// Fields according to api documentation https://apidoc.uzis.cz/index.html - /api/v1/pacienti/AktualizujKontaktniUdajePacienta
data class IsinPostPatientContactInfoDtoIn(
    val zdravotniPojistovnaKod: String?,
    val kontaktniMobilniTelefon: String?,
    val kontaktniEmail: String?,
    val pobytMesto: String?,
    val pobytPsc: String?,
    val notifikovatEmail: Boolean,
    val notifikovatSms: Boolean,
    val poznamka: String?,
    val id: String,
    val pracovnik: Pracovnik? = null
)
