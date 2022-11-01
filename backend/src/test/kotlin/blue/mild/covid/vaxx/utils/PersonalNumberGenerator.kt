package blue.mild.covid.vaxx.utils

/**
 * Generates valid personal number.
 */
fun generatePersonalNumber(): String {
    val numberInDay = generateWithLeadingZero(0, 999)
    val year = generateWithLeadingZero(65, 99)
    val month = generateWithLeadingZero(1, 12)
    val day = generateWithLeadingZero(1, 28)

    val num = "$year$month$day$numberInDay"
    val rem = num.toBigInteger().rem("11".toBigInteger())
    return if (rem != "10".toBigInteger()) {
        "$num$rem"
    } else {
        "${num}0"
    }
}

private fun generateWithLeadingZero(from: Int, to: Int): String {
    val charCount = to.toString().length
    val num = (from..to).random().toString()
    val missingZeroes = charCount - num.length
    return "0".repeat(missingZeroes) + num
}
