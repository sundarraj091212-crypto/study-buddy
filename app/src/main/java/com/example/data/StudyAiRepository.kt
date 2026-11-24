package com.example.data

import android.util.Log
import com.example.model.KeyTerm
import com.example.model.PdfDocumentInfo
import com.example.model.QuizQuestion
import com.example.model.RevisionPack
import com.example.model.SummaryBullet
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "StudyAiRepository"

class StudyAiRepository {

    private val revisionPackSchema = Schema.obj(
        properties = mapOf(
            "summaryBullets" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "iconCategory" to Schema.enumeration(
                            values = listOf("concept", "formula", "process", "key_point", "tip", "takeaway"),
                            description = "Category icon matching this summary point"
                        ),
                        "text" to Schema.string(
                            description = "Concise bullet point in simple English highlighting an important concept for quick revision"
                        )
                    )
                ),
                description = "Exactly 5 or 6 important summary bullet points in simple English"
            ),
            "practiceQuiz" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "question" to Schema.string(
                            description = "Clear multiple-choice question testing understanding of the study material"
                        ),
                        "options" to Schema.array(
                            items = Schema.string(),
                            description = "Exactly 4 answer choices corresponding to A, B, C, D"
                        ),
                        "correctIndex" to Schema.integer(
                            description = "0-based index (0, 1, 2, or 3) of the single correct option"
                        ),
                        "explanation" to Schema.string(
                            description = "Short, clear explanation of why the correct answer is right"
                        )
                    )
                ),
                description = "Exactly 5 multiple-choice questions"
            ),
            "keyTerms" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "term" to Schema.string(
                            description = "Important key term name from the notes"
                        ),
                        "meaning" to Schema.string(
                            description = "Simple, easy-to-understand definition"
                        ),
                        "example" to Schema.string(
                            description = "One short practical example if relevant, or empty string if none"
                        )
                    ),
                    optionalProperties = listOf("example")
                ),
                description = "Exactly 3 important key terms from the material"
            )
        )
    )

    private val primaryModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.3f
                responseMimeType = "application/json"
                responseSchema = revisionPackSchema
            }
        )
    }

    private val fallbackModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.3f
                responseMimeType = "application/json"
                responseSchema = revisionPackSchema
            }
        )
    }

    suspend fun generateRevisionPack(
        pastedNotes: String?,
        pdfInfo: PdfDocumentInfo?
    ): RevisionPack {
        val sourceLabel: String
        val promptInstructions = buildString {
            appendLine("You are AI Study Buddy, an expert exam and placement test revision assistant.")
            appendLine("Convert the provided study material into a structured quick revision pack.")
            appendLine()
            appendLine("STRICT REQUIREMENTS:")
            appendLine("1. summaryBullets: Generate 5 to 6 bullet points in simple, crisp English. Highlight core concepts and avoid fluff.")
            appendLine("2. practiceQuiz: Generate EXACTLY 5 multiple-choice questions. Each question MUST have exactly 4 options (no 'A.' prefix inside the option text), a valid 0-based correctIndex (0..3), and a short explanation.")
            appendLine("3. keyTerms: Generate EXACTLY 3 important key terms with a simple meaning and one short example if relevant.")
            appendLine("4. Base everything strictly on the provided study material.")
        }

        val requestContent = if (pdfInfo != null && pdfInfo.isScannedFallback && pdfInfo.pdfBytes != null) {
            sourceLabel = pdfInfo.fileName
            content {
                inlineData(pdfInfo.pdfBytes, "application/pdf")
                text(promptInstructions)
            }
        } else {
            val studyText = when {
                pdfInfo != null && pdfInfo.extractedText.isNotBlank() -> {
                    sourceLabel = pdfInfo.fileName
                    pdfInfo.extractedText
                }
                !pastedNotes.isNullOrBlank() -> {
                    sourceLabel = "Pasted Study Notes"
                    pastedNotes.trim()
                }
                else -> throw IllegalArgumentException("Please paste study notes or upload a PDF first.")
            }
            content {
                text("$promptInstructions\nSTUDY MATERIAL:\n\"\"\"\n$studyText\n\"\"\"")
            }
        }

        val response = withTimeout(60_000L) {
            generateWithFallback(requestContent)
        }

        val rawJson = response.text
        if (rawJson.isNullOrBlank()) {
            throw IllegalStateException("Gemini returned an empty response. Please try again.")
        }

        return parseAndValidatePack(rawJson, sourceLabel)
    }

    private suspend fun generateWithFallback(requestContent: com.google.firebase.ai.type.Content) =
        try {
            primaryModel.generateContent(requestContent)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!isTransientDemandError(e)) throw e
            Log.w(TAG, "Primary model busy (${e.message}); falling back to gemini-3.5-flash-lite")
            kotlinx.coroutines.delay(1_000L)
            try {
                fallbackModel.generateContent(requestContent)
            } catch (e2: kotlinx.coroutines.CancellationException) {
                throw e2
            } catch (e2: Exception) {
                if (!isTransientDemandError(e2)) throw e2
                Log.w(TAG, "Fallback model busy (${e2.message}); retrying once after brief pause")
                kotlinx.coroutines.delay(1_500L)
                fallbackModel.generateContent(requestContent)
            }
        }

    private fun isTransientDemandError(e: Exception): Boolean {
        val msg = (e.message ?: "") + " " + (e.localizedMessage ?: "")
        return msg.contains("high demand", ignoreCase = true) ||
            msg.contains("overloaded", ignoreCase = true) ||
            msg.contains("unavailable", ignoreCase = true) ||
            msg.contains("503", ignoreCase = true) ||
            msg.contains("429", ignoreCase = true) ||
            msg.contains("try again", ignoreCase = true) ||
            msg.contains("quota", ignoreCase = true)
    }

    internal fun parseAndValidatePack(rawJson: String, sourceLabel: String): RevisionPack {
        val root = try {
            JSONObject(rawJson)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid JSON from model: $rawJson", e)
            throw IllegalStateException("Received an invalid revision format from AI. Please try again.", e)
        }

        val summaryArray = root.optJSONArray("summaryBullets")
            ?: throw IllegalStateException("AI response missed the Smart Summary section.")
        val summaryBullets = buildList {
            for (i in 0 until summaryArray.length()) {
                val item = summaryArray.getJSONObject(i)
                val text = item.optString("text").trim()
                if (text.isNotEmpty()) {
                    add(
                        SummaryBullet(
                            iconCategory = item.optString("iconCategory", "concept"),
                            text = text
                        )
                    )
                }
            }
        }.take(6)

        if (summaryBullets.size < 3) {
            throw IllegalStateException("AI returned too few summary points. Please try again with more detailed notes.")
        }

        val quizArray = root.optJSONArray("practiceQuiz")
            ?: throw IllegalStateException("AI response missed the Practice Quiz section.")
        val quizQuestions = buildList {
            for (i in 0 until quizArray.length()) {
                val qObj = quizArray.getJSONObject(i)
                val questionText = qObj.optString("question").trim()
                val optionsJson = qObj.optJSONArray("options") ?: continue
                val options = (0 until optionsJson.length())
                    .map { idx -> optionsJson.optString(idx).trim() }
                    .filter { it.isNotEmpty() }
                    .take(4)

                val correctIdx = qObj.optInt("correctIndex", -1)
                val explanation = qObj.optString("explanation").trim()
                    .ifEmpty { "Review the key concept in the summary above." }

                if (questionText.isNotEmpty() && options.size == 4 && correctIdx in 0..3) {
                    add(
                        QuizQuestion(
                            question = questionText,
                            options = options,
                            correctIndex = correctIdx,
                            explanation = explanation
                        )
                    )
                }
            }
        }.take(5)

        if (quizQuestions.size != 5) {
            throw IllegalStateException(
                "AI generated ${quizQuestions.size} valid quiz questions instead of 5. Please tap Generate again."
            )
        }

        val termsArray = root.optJSONArray("keyTerms")
            ?: throw IllegalStateException("AI response missed the Key Terms section.")
        val keyTerms = buildList {
            for (i in 0 until termsArray.length()) {
                val tObj = termsArray.getJSONObject(i)
                val term = tObj.optString("term").trim()
                val meaning = tObj.optString("meaning").trim()
                val example = tObj.optString("example", "").trim()
                if (term.isNotEmpty() && meaning.isNotEmpty()) {
                    add(KeyTerm(term = term, meaning = meaning, example = example))
                }
            }
        }.take(3)

        if (keyTerms.size != 3) {
            throw IllegalStateException("AI returned ${keyTerms.size} key terms instead of 3. Please tap Generate again.")
        }

        return RevisionPack(
            sourceLabel = sourceLabel,
            summaryBullets = summaryBullets,
            quizQuestions = quizQuestions,
            keyTerms = keyTerms
        )
    }

    fun formatSummaryAsTxt(pack: RevisionPack): String = buildString {
        appendLine("AI STUDY BUDDY - SMART SUMMARY")
        appendLine("Source: ${pack.sourceLabel}")
        appendLine("==================================================")
        appendLine()
        pack.summaryBullets.forEachIndexed { index, bullet ->
            appendLine("${index + 1}. ${bullet.text}")
        }
    }

    fun formatCompletePackAsTxt(pack: RevisionPack): String = buildString {
        val optionLetters = listOf("A", "B", "C", "D")
        appendLine("AI STUDY BUDDY - COMPLETE REVISION PACK")
        appendLine("Source: ${pack.sourceLabel}")
        appendLine("==================================================")
        appendLine()
        appendLine("1. SMART SUMMARY")
        appendLine("--------------------------------------------------")
        pack.summaryBullets.forEachIndexed { index, bullet ->
            appendLine("${index + 1}. ${bullet.text}")
        }
        appendLine()
        appendLine("2. PRACTICE QUIZ (5 MCQs)")
        appendLine("--------------------------------------------------")
        pack.quizQuestions.forEachIndexed { index, q ->
            appendLine("Q${index + 1}. ${q.question}")
            q.options.forEachIndexed { optIdx, optText ->
                appendLine("   ${optionLetters[optIdx]}) $optText")
            }
            appendLine("   Answer: ${optionLetters[q.correctIndex]}) ${q.options[q.correctIndex]}")
            appendLine("   Explanation: ${q.explanation}")
            appendLine()
        }
        appendLine("3. IMPORTANT KEY TERMS")
        appendLine("--------------------------------------------------")
        pack.keyTerms.forEachIndexed { index, term ->
            appendLine("${index + 1}. ${term.term}")
            appendLine("   Meaning: ${term.meaning}")
            if (term.example.isNotBlank()) {
                appendLine("   Example: ${term.example}")
            }
            appendLine()
        }
    }
}
