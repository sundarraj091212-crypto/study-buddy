package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.KeyTerm
import com.example.model.QuizQuestion
import com.example.model.QuizSessionState
import com.example.model.RevisionPack
import com.example.model.SummaryBullet
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentBlueSoft
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleSoft
import com.example.ui.theme.CardSoftGrey
import com.example.ui.theme.CardSubtleBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ErrorRedBorder
import com.example.ui.theme.ErrorRedSoft
import com.example.ui.theme.GlassHeaderBorder
import com.example.ui.theme.GlassHeaderSurface
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkOnDark
import com.example.ui.theme.InkOnDarkMuted
import com.example.ui.theme.InkSubtle
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenBorder
import com.example.ui.theme.SuccessGreenSoft
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberSoft

@Composable
fun ResultsDashboard(
    pack: RevisionPack,
    quizSession: QuizSessionState,
    isWideLayout: Boolean,
    onSelectQuizOption: (Int, Int) -> Unit,
    onSubmitQuiz: () -> Unit,
    onRetakeQuiz: () -> Unit,
    onDownloadSummary: () -> Unit,
    onDownloadCompletePack: () -> Unit,
    onSharePack: () -> Unit,
    onGenerateNewPack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Source Pill Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GlassHeaderSurface,
            border = BorderStroke(1.dp, GlassHeaderBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoStories,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Revision Pack Ready",
                            style = MaterialTheme.typography.titleSmall,
                            color = InkOnDark
                        )
                        Text(
                            text = "Source: ${pack.sourceLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkOnDarkMuted
                        )
                    }
                }
            }
        }

        if (isWideLayout) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.Top
            ) {
                SmartSummaryCard(
                    bullets = pack.summaryBullets,
                    onDownloadSummary = onDownloadSummary,
                    modifier = Modifier.weight(1.1f)
                )
                KeyTermsCard(
                    terms = pack.keyTerms,
                    modifier = Modifier.weight(0.9f)
                )
            }
            PracticeQuizCard(
                questions = pack.quizQuestions,
                quizSession = quizSession,
                onSelectOption = onSelectQuizOption,
                onSubmitQuiz = onSubmitQuiz,
                onRetakeQuiz = onRetakeQuiz
            )
        } else {
            SmartSummaryCard(
                bullets = pack.summaryBullets,
                onDownloadSummary = onDownloadSummary
            )
            PracticeQuizCard(
                questions = pack.quizQuestions,
                quizSession = quizSession,
                onSelectOption = onSelectQuizOption,
                onSubmitQuiz = onSubmitQuiz,
                onRetakeQuiz = onRetakeQuiz
            )
            KeyTermsCard(
                terms = pack.keyTerms
            )
        }

        // Export & New Revision Pack Footer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Save & Revise Anywhere",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkDark
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDownloadCompletePack,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("download_full_pack_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Pack (.txt)",
                            style = MaterialTheme.typography.labelLarge,
                            color = AccentPurple
                        )
                    }

                    OutlinedButton(
                        onClick = onSharePack,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("share_pack_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.share_pack),
                            style = MaterialTheme.typography.labelLarge,
                            color = AccentBlue
                        )
                    }
                }

                Button(
                    onClick = onGenerateNewPack,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_new_pack_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(AccentPurple, AccentBlue))),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.generate_new_pack),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartSummaryCard(
    bullets: List<SummaryBullet>,
    onDownloadSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_summary_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccentPurpleSoft
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = stringResource(R.string.cd_concept_icon),
                            tint = AccentPurple,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.card_summary_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = InkDark
                        )
                        Text(
                            text = "${bullets.size} quick revision takeaways",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSubtle
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                bullets.forEach { bullet ->
                    val (icon, tint, bg) = resolveBulletVisuals(bullet.iconCategory)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardSoftGrey,
                        border = BorderStroke(1.dp, CardSubtleBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = bg
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = stringResource(R.string.cd_concept_icon),
                                    tint = tint,
                                    modifier = Modifier
                                        .padding(7.dp)
                                        .size(18.dp)
                                )
                            }
                            Text(
                                text = bullet.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onDownloadSummary,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CardSubtleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("download_summary_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.download_summary),
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentPurple
                )
            }
        }
    }
}

@Composable
private fun PracticeQuizCard(
    questions: List<QuizQuestion>,
    quizSession: QuizSessionState,
    onSelectOption: (Int, Int) -> Unit,
    onSubmitQuiz: () -> Unit,
    onRetakeQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val answeredCount = quizSession.selectedAnswers.size
    val allAnswered = answeredCount == questions.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("practice_quiz_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccentBlueSoft
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Quiz,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.card_quiz_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = InkDark
                        )
                        Text(
                            text = if (quizSession.isSubmitted) {
                                "Quiz completed"
                            } else {
                                "$answeredCount of ${questions.size} answered"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSubtle
                        )
                    }
                }
            }

            AnimatedVisibility(visible = quizSession.isSubmitted) {
                QuizScoreBanner(
                    score = quizSession.score,
                    total = questions.size
                )
            }

            questions.forEachIndexed { qIndex, question ->
                QuizQuestionItem(
                    questionIndex = qIndex,
                    question = question,
                    selectedOption = quizSession.selectedAnswers[qIndex],
                    isSubmitted = quizSession.isSubmitted,
                    onSelectOption = { optIdx -> onSelectOption(qIndex, optIdx) }
                )
                if (qIndex < questions.lastIndex) {
                    HorizontalDivider(color = CardSubtleBorder)
                }
            }

            if (!quizSession.isSubmitted) {
                Button(
                    onClick = onSubmitQuiz,
                    enabled = allAnswered,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        disabledContainerColor = AccentBlue.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_quiz_button")
                ) {
                    Text(
                        text = if (allAnswered) {
                            stringResource(R.string.submit_answers)
                        } else {
                            "Answer all ${questions.size} questions to submit ($answeredCount/${questions.size})"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onRetakeQuiz,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("retake_quiz_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.retake_quiz),
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizScoreBanner(
    score: Int,
    total: Int
) {
    val percentage = if (total > 0) (score * 100) / total else 0
    val isStrong = score >= 4
    val bg = if (isStrong) SuccessGreenSoft else WarningAmberSoft
    val accent = if (isStrong) SuccessGreen else WarningAmber
    val feedback = when {
        score == total -> "Perfect score! You've mastered these notes."
        score >= 4 -> "Great work! Just a quick review of the missed concept."
        score >= 3 -> "Good progress! Check the explanations below to lock it in."
        else -> "Keep revising! Read the Smart Summary and explanations below."
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bg,
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quiz_score_banner")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accent
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your Score: $score / $total ($percentage%)",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted
                )
            }
        }
    }
}

@Composable
private fun QuizQuestionItem(
    questionIndex: Int,
    question: QuizQuestion,
    selectedOption: Int?,
    isSubmitted: Boolean,
    onSelectOption: (Int) -> Unit
) {
    val optionLabels = listOf("A", "B", "C", "D")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Q${questionIndex + 1}. ${question.question}",
            style = MaterialTheme.typography.titleSmall,
            color = InkDark
        )

        question.options.forEachIndexed { optIndex, optionText ->
            val isSelected = selectedOption == optIndex
            val isCorrectOption = optIndex == question.correctIndex

            val containerColor: Color
            val borderColor: Color
            val badgeBg: Color
            val badgeText: Color

            if (!isSubmitted) {
                containerColor = if (isSelected) AccentPurpleSoft else CardSoftGrey
                borderColor = if (isSelected) AccentPurple else CardSubtleBorder
                badgeBg = if (isSelected) AccentPurple else CardWhite
                badgeText = if (isSelected) Color.White else InkDark
            } else {
                when {
                    isCorrectOption -> {
                        containerColor = SuccessGreenSoft
                        borderColor = SuccessGreenBorder
                        badgeBg = SuccessGreen
                        badgeText = Color.White
                    }
                    isSelected && !isCorrectOption -> {
                        containerColor = ErrorRedSoft
                        borderColor = ErrorRedBorder
                        badgeBg = ErrorRed
                        badgeText = Color.White
                    }
                    else -> {
                        containerColor = CardSoftGrey
                        borderColor = CardSubtleBorder
                        badgeBg = CardWhite
                        badgeText = InkSubtle
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = containerColor,
                border = BorderStroke(1.2.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .pointerHoverIcon(if (!isSubmitted) PointerIcon.Hand else PointerIcon.Default)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !isSubmitted) { onSelectOption(optIndex) }
                    .testTag("quiz_q${questionIndex}_opt$optIndex")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = optionLabels[optIndex],
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = optionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkDark,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSubmitted) {
                        if (isCorrectOption) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = stringResource(R.string.cd_quiz_status),
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Cancel,
                                contentDescription = stringResource(R.string.cd_quiz_status),
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = isSubmitted) {
            val userGotItRight = selectedOption == question.correctIndex
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (userGotItRight) SuccessGreenSoft else AccentBlueSoft,
                border = BorderStroke(
                    1.dp,
                    if (userGotItRight) SuccessGreenBorder else AccentBlue.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Correct Answer: ${optionLabels[question.correctIndex]}) ${question.options[question.correctIndex]}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (userGotItRight) SuccessGreen else AccentBlue
                    )
                    Text(
                        text = question.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkDark
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyTermsCard(
    terms: List<KeyTerm>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("key_terms_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentPurpleSoft
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Key,
                        contentDescription = stringResource(R.string.cd_term_icon),
                        tint = AccentPurple,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.card_terms_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = InkDark
                    )
                    Text(
                        text = "${terms.size} core concepts to remember",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSubtle
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                terms.forEachIndexed { index, term ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardSoftGrey,
                        border = BorderStroke(1.dp, CardSubtleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AccentPurple
                                ) {
                                    Text(
                                        text = "0${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.Bookmark,
                                    contentDescription = stringResource(R.string.cd_term_icon),
                                    tint = AccentBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = term.term,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkDark
                                )
                            }
                            Text(
                                text = term.meaning,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkMuted
                            )
                            if (term.example.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AccentBlueSoft
                                ) {
                                    Text(
                                        text = "Example: ${term.example}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AccentBlue,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class BulletVisual(
    val icon: ImageVector,
    val tint: Color,
    val bg: Color
)

private fun resolveBulletVisuals(category: String): BulletVisual = when (category.lowercase()) {
    "formula" -> BulletVisual(Icons.Rounded.Functions, AccentBlue, AccentBlueSoft)
    "process" -> BulletVisual(Icons.Rounded.Timeline, AccentPurple, AccentPurpleSoft)
    "key_point" -> BulletVisual(Icons.Rounded.Star, WarningAmber, WarningAmberSoft)
    "tip" -> BulletVisual(Icons.Rounded.TipsAndUpdates, SuccessGreen, SuccessGreenSoft)
    "takeaway" -> BulletVisual(Icons.Rounded.CheckCircle, SuccessGreen, SuccessGreenSoft)
    else -> BulletVisual(Icons.Rounded.Lightbulb, AccentPurple, AccentPurpleSoft)
}
