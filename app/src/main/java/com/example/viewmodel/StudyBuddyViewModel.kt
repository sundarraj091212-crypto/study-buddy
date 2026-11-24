package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PdfExtractionException
import com.example.data.PdfExtractor
import com.example.data.StudyAiRepository
import com.example.model.InputMode
import com.example.model.PdfDocumentInfo
import com.example.model.QuizSessionState
import com.example.model.RevisionPack
import com.example.model.StudyUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "StudyBuddyViewModel"
private const val MIN_NOTES_CHARS = 20

data class StudyBuddyScreenState(
    val inputMode: InputMode = InputMode.PASTE_NOTES,
    val pastedNotes: String = "",
    val pdfInfo: PdfDocumentInfo? = null,
    val isExtractingPdf: Boolean = false,
    val pdfError: String? = null,
    val inputValidationError: String? = null,
    val studyUiState: StudyUiState = StudyUiState.Idle,
    val quizSession: QuizSessionState = QuizSessionState()
)

class StudyBuddyViewModel(
    private val aiRepository: StudyAiRepository = StudyAiRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(StudyBuddyScreenState())
    val state: StateFlow<StudyBuddyScreenState> = _state.asStateFlow()

    fun selectInputMode(mode: InputMode) {
        _state.update {
            it.copy(
                inputMode = mode,
                inputValidationError = null
            )
        }
    }

    fun updatePastedNotes(text: String) {
        _state.update {
            it.copy(
                pastedNotes = text,
                inputValidationError = null
            )
        }
    }

    fun loadPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isExtractingPdf = true,
                    pdfError = null,
                    inputValidationError = null
                )
            }
            try {
                val info = PdfExtractor.extractFromUri(context, uri)
                _state.update {
                    it.copy(
                        inputMode = InputMode.UPLOAD_PDF,
                        pdfInfo = info,
                        isExtractingPdf = false,
                        pdfError = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: PdfExtractionException) {
                Log.w(TAG, "PDF extraction warning: ${e.message}")
                _state.update {
                    it.copy(
                        pdfInfo = null,
                        isExtractingPdf = false,
                        pdfError = e.message ?: "Could not read the selected PDF."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected PDF error", e)
                _state.update {
                    it.copy(
                        pdfInfo = null,
                        isExtractingPdf = false,
                        pdfError = "Failed to process PDF file. Please try another document."
                    )
                }
            }
        }
    }

    fun clearAll() {
        _state.update {
            StudyBuddyScreenState(inputMode = it.inputMode)
        }
    }

    fun generateRevisionPack() {
        val current = _state.value
        val activePdf = if (current.inputMode == InputMode.UPLOAD_PDF) current.pdfInfo else null
        val activeNotes = if (current.inputMode == InputMode.PASTE_NOTES) current.pastedNotes else ""

        if (current.inputMode == InputMode.PASTE_NOTES) {
            if (activeNotes.isBlank()) {
                _state.update {
                    it.copy(inputValidationError = "Please paste your study notes before generating a revision pack.")
                }
                return
            }
            if (activeNotes.trim().length < MIN_NOTES_CHARS) {
                _state.update {
                    it.copy(inputValidationError = "Please enter at least $MIN_NOTES_CHARS characters so Gemini has enough context.")
                }
                return
            }
        } else {
            if (current.isExtractingPdf) {
                _state.update {
                    it.copy(inputValidationError = "Please wait while we finish reading your PDF.")
                }
                return
            }
            if (activePdf == null) {
                _state.update {
                    it.copy(inputValidationError = "Please select a valid PDF document first.")
                }
                return
            }
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    inputValidationError = null,
                    quizSession = QuizSessionState(),
                    studyUiState = StudyUiState.Loading("Analyzing notes & crafting your quiz...")
                )
            }

            try {
                val pack = aiRepository.generateRevisionPack(
                    pastedNotes = activeNotes.takeIf { current.inputMode == InputMode.PASTE_NOTES },
                    pdfInfo = activePdf
                )
                _state.update {
                    it.copy(
                        studyUiState = StudyUiState.Success(pack),
                        quizSession = QuizSessionState()
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini generation timed out", e)
                _state.update {
                    it.copy(
                        studyUiState = StudyUiState.Error(
                            "Request timed out after 60 seconds. Please check your connection and try again."
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Revision pack generation failed", e)
                val friendlyMessage = mapFriendlyApiError(e)
                _state.update {
                    it.copy(studyUiState = StudyUiState.Error(friendlyMessage))
                }
            }
        }
    }

    fun selectQuizOption(questionIndex: Int, optionIndex: Int) {
        val current = _state.value
        if (current.quizSession.isSubmitted) return
        _state.update { state ->
            val updated = state.quizSession.selectedAnswers.toMutableMap().apply {
                put(questionIndex, optionIndex)
            }
            state.copy(
                quizSession = state.quizSession.copy(selectedAnswers = updated)
            )
        }
    }

    fun submitQuiz() {
        val current = _state.value
        val pack = (current.studyUiState as? StudyUiState.Success)?.pack ?: return
        val score = pack.quizQuestions.indices.count { index ->
            current.quizSession.selectedAnswers[index] == pack.quizQuestions[index].correctIndex
        }
        _state.update {
            it.copy(
                quizSession = it.quizSession.copy(
                    isSubmitted = true,
                    score = score
                )
            )
        }
    }

    fun retakeQuiz() {
        _state.update {
            it.copy(quizSession = QuizSessionState())
        }
    }

    fun generateNewPack() {
        _state.update {
            it.copy(
                studyUiState = StudyUiState.Idle,
                quizSession = QuizSessionState(),
                inputValidationError = null
            )
        }
    }

    fun dismissError() {
        _state.update {
            it.copy(studyUiState = StudyUiState.Idle)
        }
    }

    fun getSummaryTxt(pack: RevisionPack): String = aiRepository.formatSummaryAsTxt(pack)

    fun getCompletePackTxt(pack: RevisionPack): String = aiRepository.formatCompletePackAsTxt(pack)

    private fun mapFriendlyApiError(e: Exception): String {
        val raw = e.localizedMessage.orEmpty()
        return when {
            raw.contains("403", ignoreCase = true) || raw.contains("AppCheck", ignoreCase = true) ->
                "AI service authorization error (App Check). Please rebuild or verify network access. ($raw)"
            raw.contains("429", ignoreCase = true) || raw.contains("quota", ignoreCase = true) ->
                "AI service is temporarily busy (rate limit reached). Please wait a moment and try again."
            raw.contains("network", ignoreCase = true) || raw.contains("Unable to resolve host", ignoreCase = true) ->
                "No internet connection detected. Please connect to the internet and try again."
            raw.isNotBlank() -> raw
            else -> "Could not generate revision pack. Please try again."
        }
    }
}
