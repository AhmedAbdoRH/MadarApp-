package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

data class PickedQualificationFile(
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val base64Content: String
)

sealed interface AttachmentReadResult {
    data class Success(val file: PickedQualificationFile) : AttachmentReadResult
    data class Error(val message: String) : AttachmentReadResult
}

object QualificationAttachmentHelper {
    private const val MAX_IMAGE_RAW_BYTES = 420_000
    private const val MAX_PDF_RAW_BYTES = 520_000
    private const val MAX_IMAGE_DIM = 1280
    private val ALLOWED_IMAGE_TYPES = setOf("image/jpeg", "image/png", "image/webp")

    suspend fun readAndPrepareAttachment(
        context: Context,
        uri: Uri
    ): AttachmentReadResult = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val detectedType = (contentResolver.getType(uri) ?: "").lowercase()
            var fileName = "qualification"
            var declaredSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        declaredSize = cursor.getLong(sizeIndex)
                    }
                }
            }

            val resolvedMime = when {
                detectedType in ALLOWED_IMAGE_TYPES -> detectedType
                detectedType.startsWith("image/") -> "image/jpeg"
                detectedType == "application/pdf" || fileName.lowercase().endsWith(".pdf") -> "application/pdf"
                fileName.lowercase().endsWith(".png") -> "image/png"
                fileName.lowercase().endsWith(".webp") -> "image/webp"
                fileName.lowercase().endsWith(".jpg") || fileName.lowercase().endsWith(".jpeg") -> "image/jpeg"
                else -> return@withContext AttachmentReadResult.Error(
                    "صيغة الملف غير مدعومة. يُرجى إرفاق صورة (JPG / PNG) أو ملف PDF."
                )
            }

            if (resolvedMime == "application/pdf") {
                if (declaredSize > 1_500_000L) {
                    return@withContext AttachmentReadResult.Error(
                        "حجم ملف الـ PDF كبير جداً. يُرجى اختيار ملف أقل من 500 كيلوبايت أو إرفاق صورة واضحة للشهادة."
                    )
                }
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext AttachmentReadResult.Error("تعذر قراءة الملف المحدد.")
                if (bytes.isEmpty()) {
                    return@withContext AttachmentReadResult.Error("الملف المحدد فارغ.")
                }
                if (bytes.size > MAX_PDF_RAW_BYTES) {
                    return@withContext AttachmentReadResult.Error(
                        "حجم ملف الـ PDF يتجاوز الحد المسموح (500 كيلوبايت). يُرجى ضغطه أو تصوير الشهادة وإرفاقها كصورة."
                    )
                }
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                AttachmentReadResult.Success(
                    PickedQualificationFile(
                        fileName = fileName.take(180),
                        mimeType = "application/pdf",
                        sizeBytes = bytes.size.toLong(),
                        base64Content = base64
                    )
                )
            } else {
                val rawBytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext AttachmentReadResult.Error("تعذر قراءة الصورة المحددة.")
                if (rawBytes.isEmpty()) {
                    return@withContext AttachmentReadResult.Error("الصورة المحددة فارغة.")
                }

                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOpts)
                if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) {
                    return@withContext AttachmentReadResult.Error("تعذر معالجة ملف الصورة المحدد.")
                }

                var sampleSize = 1
                while (boundsOpts.outWidth / sampleSize > MAX_IMAGE_DIM * 2 ||
                    boundsOpts.outHeight / sampleSize > MAX_IMAGE_DIM * 2
                ) {
                    sampleSize *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts)
                    ?: return@withContext AttachmentReadResult.Error("تعذر فتح الصورة المحددة.")

                val scaled = scaleDownIfNeeded(decoded, MAX_IMAGE_DIM)
                if (scaled !== decoded) decoded.recycle()

                val compressed = compressToLimit(scaled, MAX_IMAGE_RAW_BYTES)
                scaled.recycle()

                if (compressed.size > MAX_IMAGE_RAW_BYTES) {
                    return@withContext AttachmentReadResult.Error(
                        "حجم الصورة لا يزال كبيراً بعد الضغط. يُرجى اختيار صورة بدقة أقل."
                    )
                }

                val base64 = Base64.encodeToString(compressed, Base64.NO_WRAP)
                AttachmentReadResult.Success(
                    PickedQualificationFile(
                        fileName = fileName.take(180),
                        mimeType = "image/jpeg",
                        sizeBytes = compressed.size.toLong(),
                        base64Content = base64
                    )
                )
            }
        } catch (e: Exception) {
            AttachmentReadResult.Error("حدث خطأ أثناء تجهيز المرفق: ${e.localizedMessage ?: "غير معروف"}")
        }
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = minOf(maxDim.toFloat() / w, maxDim.toFloat() / h)
        val newW = (w * ratio).toInt().coerceAtLeast(1)
        val newH = (h * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }

    private fun compressToLimit(bitmap: Bitmap, maxBytes: Int): ByteArray {
        var quality = 82
        val out = ByteArrayOutputStream()
        do {
            out.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            quality -= 12
        } while (out.size() > maxBytes && quality >= 30)
        return out.toByteArray()
    }

    suspend fun decodeBase64ToPreviewBitmaps(
        context: Context,
        mimeType: String,
        base64Content: String,
        maxPdfPages: Int = 5
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        if (base64Content.isBlank()) return@withContext emptyList()
        try {
            val bytes = Base64.decode(base64Content, Base64.DEFAULT)
            if (mimeType == "application/pdf") {
                val tempFile = File.createTempFile("qual_preview_", ".pdf", context.cacheDir)
                try {
                    FileOutputStream(tempFile).use { it.write(bytes) }
                    ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                        PdfRenderer(pfd).use { renderer ->
                            val pageCount = renderer.pageCount.coerceAtMost(maxPdfPages)
                            val pages = mutableListOf<Bitmap>()
                            for (index in 0 until pageCount) {
                                renderer.openPage(index).use { page ->
                                    val targetWidth = 1080
                                    val targetHeight = ((targetWidth.toFloat() / page.width.coerceAtLeast(1)) * page.height)
                                        .toInt()
                                        .coerceIn(200, 2000)
                                    val bitmap = Bitmap.createBitmap(
                                        targetWidth,
                                        targetHeight,
                                        Bitmap.Config.ARGB_8888
                                    )
                                    bitmap.eraseColor(Color.WHITE)
                                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    pages.add(bitmap)
                                }
                            }
                            pages
                        }
                    }
                } finally {
                    tempFile.delete()
                }
            } else {
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) listOf(bitmap) else emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024L -> "$bytes بايت"
            bytes < 1024L * 1024L -> "${(bytes / 1024.0).toInt().coerceAtLeast(1)} كيلوبايت"
            else -> String.format(java.util.Locale.US, "%.1f ميجابايت", bytes / (1024.0 * 1024.0))
        }
    }
}
