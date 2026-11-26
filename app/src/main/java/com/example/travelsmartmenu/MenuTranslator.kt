package com.example.travelsmartmenu

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

data class TranslatedMenuItem(
    val originalName: String,
    val translatedName: String,
    val extractedPrice: String?,
    val convertedPriceText: String?
)

class MenuTranslator {

    suspend fun translateItems(
        items: List<ScannedMenuItem>,
        targetLanguageName: String,
        countryProfile: CountryMenuProfile
    ): List<TranslatedMenuItem> {
        if (items.isEmpty()) return emptyList()

        val targetLangCode = if (targetLanguageName == "Română") {
            TranslateLanguage.ROMANIAN
        } else {
            TranslateLanguage.ENGLISH
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(countryProfile.translateSourceLang)
            .setTargetLanguage(targetLangCode)
            .build()

        val translator = Translation.getClient(options)

        return try {
            val isModelReady = ensureModelDownloaded(translator)
            items.map { item ->
                val translated = if (isModelReady) {
                    translateSingle(translator, item.dishName)
                } else {
                    item.dishName
                }
                val converted = convertPrice(item.extractedPrice, targetLanguageName, countryProfile)
                TranslatedMenuItem(
                    originalName = item.dishName,
                    translatedName = translated,
                    extractedPrice = item.extractedPrice,
                    convertedPriceText = converted
                )
            }
        } finally {
            translator.close()
        }
    }

    private suspend fun ensureModelDownloaded(translator: Translator): Boolean =
        suspendCancellableCoroutine { cont ->
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }

    private suspend fun translateSingle(translator: Translator, text: String): String =
        suspendCancellableCoroutine { cont ->
            translator.translate(text)
                .addOnSuccessListener { result -> cont.resume(result) }
                .addOnFailureListener { cont.resume(text) }
        }

    private fun convertPrice(
        rawPrice: String?,
        targetLanguageName: String,
        profile: CountryMenuProfile
    ): String? {
        if (rawPrice.isNullOrBlank()) return null
        val numeric = Regex("""\d+(?:[.,]\d{1,2})?""")
            .find(rawPrice)
            ?.value
            ?.replace(',', '.')
            ?.toDoubleOrNull() ?: return null

        val eurAmount = numeric * profile.rateToEur
        return if (targetLanguageName == "Română") {
            String.format(Locale.US, "~%.2f RON", eurAmount * 4.97)
        } else {
            String.format(Locale.US, "~€%.2f", eurAmount)
        }
    }
}