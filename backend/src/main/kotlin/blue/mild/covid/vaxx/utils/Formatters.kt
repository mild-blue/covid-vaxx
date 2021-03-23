package blue.mild.covid.vaxx.utils

import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import com.google.i18n.phonenumbers.PhoneNumberUtil

fun PhoneNumberDtoIn.formatPhoneNumber(): String {
    val phoneUtil = PhoneNumberUtil.getInstance()
    return phoneUtil.format(phoneUtil.parse(this.number, this.countryCode), PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL).removeAllWhitespaces()
}

fun String.removeAllWhitespaces() = this.replace("\\s".toRegex(), "")
