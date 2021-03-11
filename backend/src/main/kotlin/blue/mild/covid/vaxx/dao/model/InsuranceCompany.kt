package blue.mild.covid.vaxx.dao.model

enum class InsuranceCompany(
    val csFullName: String,
    val code: Int
) {
    CZPZ("Česká průmyslová zdravotní pojišťovna", 205),
    OZP("Oborová zdravotní pojišťovna zaměstnanců bank, pojišťoven a stavebnictví", 207),
    RBP("RBP, zdravotní pojišťovna", 213),
    VZP("Všeobecná zdravotní pojišťovna České republiky", 111),
    VOZP("Vojenská zdravotní pojišťovna ČR", 201),
    ZPS("Zaměstnanecká pojišťovna Škoda", 209),
    ZPMV("Zdravotní pojišťovna ministerstva vnitra", 211)
}
