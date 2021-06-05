package blue.mild.covid.vaxx.dao.model

object DatabaseTypeLength {
    const val SHORT_STRING = 128
    const val DEFAULT_STRING = 2 * SHORT_STRING
    const val PERSONAL_NUMBER = 11
    const val PHONE_NUMBER = 14
    const val INSURANCE_COMPANY = 4
    const val REMOTE_HOST = 45 // for size see https://stackoverflow.com/a/166157/7169288
    const val ROLE = 16
    const val BODY_PART = 17
    const val PATIENT_ISIN_ID = 12
}
