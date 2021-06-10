package blue.mild.covid.vaxx.dto.internal


// Fields according to api documentation https://apidoc.uzis.cz/index.html - VakcinaceDavkaView
data class IsinVaccinationDoseDto (
    val id: String?,
    val vakcinaceId: String,
    val ockovaciLatkaKod: String,
    val datumVakcinace: String,
    val typVykonuKod: String?,
    val sarze: String,
    val aplikacniCestaKod: String?,
    val mistoAplikaceKod: String?,
    val expirace: String,
    val poznamka: String?,
    val ockovaciLatkaNazev: String?,
    val typVykonuNazev: String?,
    val aplikacniCestaNazev: String?,
    val mistoAplikaceNazev: String?,
    val subjekt: IsinSubjectDto,

    // not in documentation
    val odeslaniCertifikatu: String?
)
