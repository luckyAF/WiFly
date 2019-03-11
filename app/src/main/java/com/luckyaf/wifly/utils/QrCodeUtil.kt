package com.luckyaf.wifly.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeUtil {
    fun createBarcode(str: String, width: Int, height: Int, isKeepWhiteSpace: Boolean): Bitmap? {
        var width = width
        var height = height
        try {
            var matrix = QRCodeWriter().encode(str, BarcodeFormat.QR_CODE, width, height)
            width = matrix.width
            height = matrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = Color.BLACK
                    } else {
                        pixels[y * width + x] = Color.WHITE
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        } catch (e: Exception) {
            return null
        }

    }
}