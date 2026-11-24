package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.PdfDocumentInfo
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.ByteArrayInputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfExtractionException(message: String, cause: Throwable? = null) : Exception(message, cause)

object PdfExtractor {
    private const val MAX_PDF_BYTES = 18 * 1024 * 1024 // Keep well under 20MB inline limit
    private const val MAX_TEXT_CHARS = 45_000

    suspend fun extractFromUri(context: Context, uri: Uri): PdfDocumentInfo =
        withContext(Dispatchers.IO) {
            val appContext = context.applicationContext
            PDFBoxResourceLoader.init(appContext)

            val (displayName, reportedSize) = queryFileMetadata(appContext, uri)
            if (reportedSize > MAX_PDF_BYTES) {
                throw PdfExtractionException(
                    "PDF file is too large (${reportedSize / (1024 * 1024)} MB). Please upload a PDF under 18 MB."
                )
            }

            val bytes = try {
                appContext.contentResolver.openInputStream(uri)?.use { stream ->
                    val data = stream.readBytes()
                    if (data.size > MAX_PDF_BYTES) {
                        throw PdfExtractionException(
                            "PDF file exceeds the 18 MB limit. Please upload a smaller document."
                        )
                    }
                    data
                } ?: throw PdfExtractionException("Unable to open the selected PDF file.")
            } catch (e: PdfExtractionException) {
                throw e
            } catch (e: IOException) {
                throw PdfExtractionException("Could not read the selected PDF file.", e)
            }

            if (bytes.isEmpty()) {
                throw PdfExtractionException("The selected PDF file is empty.")
            }

            try {
                PDDocument.load(ByteArrayInputStream(bytes)).use { document ->
                    if (document.isEncrypted) {
                        throw PdfExtractionException(
                            "This PDF is password-protected. Please upload an unencrypted PDF."
                        )
                    }

                    val pageCount = document.numberOfPages
                    if (pageCount <= 0) {
                        throw PdfExtractionException("This PDF has no readable pages.")
                    }

                    val stripper = PDFTextStripper().apply {
                        sortByPosition = true
                    }
                    val rawText = stripper.getText(document) ?: ""
                    val cleanedText = rawText
                        .replace(Regex("[ \\t]+"), " ")
                        .replace(Regex("\\n{3,}"), "\n\n")
                        .trim()
                        .take(MAX_TEXT_CHARS)

                    if (cleanedText.length < 30) {
                        // Scanned or image-based PDF: retain bytes so Gemini can process natively
                        return@withContext PdfDocumentInfo(
                            fileName = displayName,
                            pageCount = pageCount,
                            fileSizeBytes = bytes.size.toLong(),
                            extractedText = cleanedText,
                            pdfBytes = bytes,
                            isScannedFallback = true
                        )
                    }

                    PdfDocumentInfo(
                        fileName = displayName,
                        pageCount = pageCount,
                        fileSizeBytes = bytes.size.toLong(),
                        extractedText = cleanedText,
                        pdfBytes = null,
                        isScannedFallback = false
                    )
                }
            } catch (e: PdfExtractionException) {
                throw e
            } catch (e: InvalidPasswordException) {
                throw PdfExtractionException(
                    "This PDF is password-protected. Please upload an unencrypted PDF.",
                    e
                )
            } catch (e: Exception) {
                throw PdfExtractionException(
                    "Invalid or corrupted PDF file. Please choose another PDF or paste your notes directly.",
                    e
                )
            }
        }

    private fun queryFileMetadata(context: Context, uri: Uri): Pair<String, Long> {
        var name = "study_material.pdf"
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    cursor.getString(nameIndex)?.takeIf { it.isNotBlank() }?.let { name = it }
                }
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
        return name to size
    }
}
