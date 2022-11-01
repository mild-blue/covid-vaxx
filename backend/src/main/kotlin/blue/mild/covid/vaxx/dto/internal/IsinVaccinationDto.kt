package blue.mild.covid.vaxx.dto.internal

// Fields according to api documentation https://apidoc.uzis.cz/index.html - VakcinaceView
data class IsinVaccinationDto(
    val id: String?,
    val pacientId: String,
    val typOckovaniKod: String,
    val indikace: List<String>,
    val indikaceJina: String?,
    val stav: String, // Indikovano, Probihajici, Ukoncene, Zruseno
    val typOckovaniNazev: String?,
    val stavVakcinaceNazev: String?,
    val subjekt: IsinSubjectDto,
    val davky: List<IsinVaccinationDoseDto>?
)
