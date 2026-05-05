package com.goodwy.commons.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.EnumMap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.goodwy.commons.extensions.toast

object QrCodeHelper {
    fun generateQrCode(context: Context, content: String, size: Int): Bitmap? {
        return try {
            val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            bitmap
        } catch (e: Exception) {
            context.toast(e.message ?: e.toString())
            null
        }
    }

    /**
     * Checks whether the vCard will fit in the QR code
     * @param content data for the QR code
     * @param qrVersion QR code version (default is 40—the maximum)
     * @return true if it fits
     */
    fun canFitInQrCode(content: String, qrVersion: Int = 40): Boolean {
        // Maximum QR code capacity in bytes for different versions and error correction levels
        // Version 40, Level L (Low) - 2953 байт
        // Version 40, Level M (Medium) - 2331 байт
        // Version 40, Level Q (Quartile) - 1663 байта
        // Version 40, Level H (High) - 1273 байта

        val bytes = content.toByteArray(Charsets.UTF_8)
        val maxBytes = when (qrVersion) {
            1 -> 17
            5 -> 134
            10 -> 652
            20 -> 1651
            30 -> 2459
            40 -> 2953
            else -> 2953
        }

        return bytes.size <= maxBytes
    }
}
