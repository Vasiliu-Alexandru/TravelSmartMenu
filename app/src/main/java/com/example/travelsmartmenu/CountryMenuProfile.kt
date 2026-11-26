package com.example.travelsmartmenu

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

enum class ScriptType {
    CHINESE
}

data class CountryMenuProfile(
    val countryCode: String,
    val displayName: String,
    val scriptType: ScriptType,
    val translateSourceLang: String,
    val minDishLength: Int,
    val currencyTokens: List<String>,
    val rateToEur: Double
) {
    fun createRecognizer(): TextRecognizer = when (scriptType) {
        ScriptType.CHINESE -> TextRecognition.getClient(
            ChineseTextRecognizerOptions.Builder().build()
        )
    }

    fun buildTrailingPriceRegex(): Regex {
        val escapedTokens = currencyTokens.joinToString("|") { Regex.escape(it) }
        return Regex(
            """[\s._-]*((?:$escapedTokens)?\s*\d+(?:[.,]\d{1,2})?\s*(?:$escapedTokens)?)\s*$""",
            RegexOption.IGNORE_CASE
        )
    }

    fun buildPureNoiseRegex(): Regex {
        val escapedTokens = currencyTokens.joinToString("|") { Regex.escape(it) }
        return Regex(
            """^[\d\s.,%$-]+(?:$escapedTokens|g|gr|ml|l|kcal)?$""",
            RegexOption.IGNORE_CASE
        )
    }
}

object SupportedCountries {
    val CHINA = CountryMenuProfile(
        countryCode = "CN",
        displayName = "China",
        scriptType = ScriptType.CHINESE,
        translateSourceLang = TranslateLanguage.CHINESE,
        minDishLength = 2,
        currencyTokens = listOf("¥", "￥", "元", "块", "rmb", "cny"),
        rateToEur = 0.13
    )

    val all: List<CountryMenuProfile> = listOf(CHINA)

    fun fromDisplayName(name: String): CountryMenuProfile =
        all.find { it.displayName.equals(name, ignoreCase = true) } ?: CHINA
}