package blue.mild.covid.vaxx.dao

enum class InsuranceCompany(
    val csFullName: String
) {
    CZPZ("Česká průmyslová zdravotní pojišťovna"),
    OZP("Oborová zdravotní pojišťovna"),
    RBP("RBP, zdravotní pojišťovna"),
    VZP("Všeobecná zdravotní pojišťovna"),
    VOZP("Vojenská Zdravotní Pojišťovna"),
    ZPS("Zaměstnanecká pojišťovna Škoda"),
    ZPMV("Zdravotní pojišťovna Ministerstva vnitra ČR")
}
