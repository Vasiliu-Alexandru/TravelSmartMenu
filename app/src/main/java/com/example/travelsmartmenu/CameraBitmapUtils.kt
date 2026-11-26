package com.example.travelsmartmenu

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

fun ImageProxy.toRotatedCroppedBitmap(
    widthFraction: Float = 0.85f,
    heightFraction: Float = 0.45f
): Bitmap {
    val rawBitmap = this.toBitmap()
    val rotationDegrees = this.imageInfo.rotationDegrees

    val rotatedBitmap = if (rotationDegrees != 0) {
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        Bitmap.createBitmap(
            rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true
        )
    } else {
        rawBitmap
    }

    val cropWidth = (rotatedBitmap.width * widthFraction).toInt().coerceAtMost(rotatedBitmap.width)
    val cropHeight = (rotatedBitmap.height * heightFraction).toInt().coerceAtMost(rotatedBitmap.height)
    val startX = ((rotatedBitmap.width - cropWidth) / 2).coerceAtLeast(0)
    val startY = ((rotatedBitmap.height - cropHeight) / 2).coerceAtLeast(0)

    return Bitmap.createBitmap(rotatedBitmap, startX, startY, cropWidth, cropHeight)
}