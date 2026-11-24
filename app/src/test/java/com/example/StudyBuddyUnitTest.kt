package com.example

import com.example.data.StudyAiRepository
import com.example.model.InputMode
import com.example.model.KeyTerm
import com.example.model.QuizQuestion
import com.example.model.RevisionPack
import com.example.model.SummaryBullet
import com.example.viewmodel.StudyBuddyViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudyBuddyUnitTest {

    private val repository = StudyAiRepository()

    @Test
    fun `parseAndValidatePack parses valid structured json`() {
        val json = """
            {
              "summaryBullets": [
                {"iconCategory": "concept", "text": "Photosynthesis converts light into chemical energy."},
                {"iconCategory": "process", "text": "Occurs in chloroplasts using chlorophyll."},
                {"iconCategory": "formula", "text": "Produces glucose and oxygen from CO2 and water."},
                {"iconCategory": "key_point", "text": "Light reactions happen in thylakoid membranes."},
                {"iconCategory": "takeaway", "text": "Calvin cycle fixes carbon in the stroma."}
              ],
              "practiceQuiz": [
                {"question": "Q1?", "options": ["A", "B", "C", "D"], "correctIndex": 0, "explanation": "Exp 1"},
                {"question": "Q2?", "options": ["A", "B", "C", "D"], "correctIndex": 1, "explanation": "Exp 2"},
                {"question": "Q3?", "options": ["A", "B", "C", "D"], "correctIndex": 2, "explanation": "Exp 3"},
                {"question": "Q4?", "options": ["A", "B", "C", "D"], "correctIndex": 3, "explanation": "Exp 4"},
                {"question": "Q5?", "options": ["A", "B", "C", "D"], "correctIndex": 0, "explanation": "Exp 5"}
              ],
              "keyTerms": [
                {"term": "Chlorophyll", "meaning": "Green pigment absorbing light.", "example": "Found in plant leaves."},
                {"term": "Stroma", "meaning": "Fluid inside chloroplast.", "example": "Site of Calvin cycle."},
                {"term": "ATP", "meaning": "Cellular energy molecule.", "example": ""}
              ]
            }
        """.trimIndent()

        val pack = repository.parseAndValidatePack(json, "Biology Chapter 4")
        assertEquals(5, pack.summaryBullets.size)
        assertEquals(5, pack.quizQuestions.size)
        assertEquals(3, pack.keyTerms.size)
        assertEquals("Chlorophyll", pack.keyTerms.first().term)
    }

    @Test
    fun `txt formatters include summary quiz and key terms`() {
        val pack = RevisionPack(
            sourceLabel = "Chapter 1",
            summaryBullets = listOf(SummaryBullet("concept", "Core idea one")),
            quizQuestions = listOf(
                QuizQuestion("What is 2+2?", listOf("3", "4", "5", "6"), 1, "Basic addition.")
            ),
            keyTerms = listOf(KeyTerm("Addition", "Combining numbers", "2+2=4"))
        )

        val summaryTxt = repository.formatSummaryAsTxt(pack)
        assertTrue(summaryTxt.contains("SMART SUMMARY"))
        assertTrue(summaryTxt.contains("Core idea one"))

        val fullTxt = repository.formatCompletePackAsTxt(pack)
        assertTrue(fullTxt.contains("COMPLETE REVISION PACK"))
        assertTrue(fullTxt.contains("PRACTICE QUIZ"))
        assertTrue(fullTxt.contains("Answer: B) 4"))
        assertTrue(fullTxt.contains("IMPORTANT KEY TERMS"))
        assertTrue(fullTxt.contains("Addition"))
    }

    @Test
    fun `viewmodel validates empty and short notes`() {
        val vm = StudyBuddyViewModel(repository)
        vm.generateRevisionPack()
        assertNotNull(vm.state.value.inputValidationError)

        vm.updatePastedNotes("Too short")
        vm.generateRevisionPack()
        assertNotNull(vm.state.value.inputValidationError)

        vm.selectInputMode(InputMode.UPLOAD_PDF)
        assertNull(vm.state.value.inputValidationError)
        vm.generateRevisionPack()
        assertNotNull(vm.state.value.inputValidationError)
    }
}
