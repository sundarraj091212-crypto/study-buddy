package com.example.model

enum class InputMode {
    PASTE_NOTES,
    UPLOAD_PDF
}

data class PdfDocumentInfo(
    val fileName: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val extractedText: String,
    val pdfBytes: ByteArray? = null,
    val isScannedFallback: Boolean = false
) {
    val formattedSize: String
        get() = when {
            fileSizeBytes < 1024 -> "$fileSizeBytes B"
            fileSizeBytes < 1024 * 1024 -> "${fileSizeBytes / 1024} KB"
            else -> String.format("%.1f MB", fileSizeBytes / (1024f * 1024f))
        }
}

data class SummaryBullet(
    val iconCategory: String,
    val text: String
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class KeyTerm(
    val term: String,
    val meaning: String,
    val example: String
)

data class RevisionPack(
    val sourceLabel: String,
    val summaryBullets: List<SummaryBullet>,
    val quizQuestions: List<QuizQuestion>,
    val keyTerms: List<KeyTerm>
)

sealed interface StudyUiState {
    data object Idle : StudyUiState
    data class Loading(val stageMessage: String) : StudyUiState
    data class Success(val pack: RevisionPack) : StudyUiState
    data class Error(val message: String) : StudyUiState
}

data class QuizSessionState(
    val selectedAnswers: Map<Int, Int> = emptyMap(),
    val isSubmitted: Boolean = false,
    val score: Int = 0
)
