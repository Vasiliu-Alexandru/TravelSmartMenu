package com.example.travelsmartmenu

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

data class ScannedMenuItem(
    val dishName: String,
    val extractedPrice: String? = null
)

class MenuOcrProcessor(
    private val profile: CountryMenuProfile = SupportedCountries.CHINA
) {
    private val recognizer = profile.createRecognizer()
    private val pureNoiseRegex = profile.buildPureNoiseRegex()
    private val trailingPriceRegex = profile.buildTrailingPriceRegex()
    private val validLetterRegex = Regex("""\p{L}{${profile.minDishLength},}""")

    // Used for Camera capture (cropped Bitmap)
    suspend fun extractFromBitmap(bitmap: Bitmap): List<ScannedMenuItem> =
        processInputImage(InputImage.fromBitmap(bitmap, 0))

    // Used for Gallery upload (full selected image Uri)
    suspend fun extractFromUri(context: Context, uri: Uri): List<ScannedMenuItem> =
        processInputImage(InputImage.fromFilePath(context, uri))

    private suspend fun processInputImage(image: InputImage): List<ScannedMenuItem> =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(parseLines(visionText))
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }

    private fun parseLines(visionText: Text): List<ScannedMenuItem> {
        val allLines = visionText.textBlocks
            .flatMap { it.lines }
            .sortedBy { it.boundingBox?.top ?: 0 }

        return allLines.mapNotNull { line ->
            val raw = line.text.trim()

            if (raw.length < profile.minDishLength || pureNoiseRegex.matches(raw)) {
                return@mapNotNull null
            }
            if (!raw.contains(validLetterRegex)) {
                return@mapNotNull null
            }

            val priceMatch = trailingPriceRegex.find(raw)
            val price = priceMatch?.groupValues?.getOrNull(1)?.trim()
            val dishOnly = if (priceMatch != null) {
                raw.removeRange(priceMatch.range).trim(' ', '.', '-', '_')
            } else {
                raw
            }

            if (dishOnly.length >= profile.minDishLength) {
                ScannedMenuItem(dishName = dishOnly, extractedPrice = price)
            } else {
                null
            }
        }
    }

    fun close() {
        recognizer.close()
    }
}