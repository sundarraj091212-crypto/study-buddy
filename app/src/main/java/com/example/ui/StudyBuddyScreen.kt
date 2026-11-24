package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.IncomingStudyPayload
import com.example.model.InputMode
import com.example.model.StudyUiState
import com.example.ui.components.ErrorBanner
import com.example.ui.components.HeroHeader
import com.example.ui.components.InputSectionCard
import com.example.ui.components.LoadingStudyCard
import com.example.ui.components.ResultsDashboard
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.CardWhite
import com.example.ui.theme.IndigoAbyss
import com.example.ui.theme.InkDark
import com.example.ui.theme.NavyMidnight
import com.example.ui.theme.NavyVoid
import com.example.ui.theme.PurpleGlow
import com.example.viewmodel.StudyBuddyViewModel
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StudyBuddyScreen(
    incomingPayload: IncomingStudyPayload? = null,
    onPayloadConsumed: () -> Unit = {},
    viewModel: StudyBuddyViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingTxtContent by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(incomingPayload?.nonce) {
        val payload = incomingPayload ?: return@LaunchedEffect
        when {
            payload.pdfUri != null -> {
                viewModel.selectInputMode(InputMode.UPLOAD_PDF)
                viewModel.loadPdf(context, payload.pdfUri)
            }
            !payload.sharedText.isNullOrBlank() -> {
                viewModel.selectInputMode(InputMode.PASTE_NOTES)
                viewModel.updatePastedNotes(payload.sharedText)
            }
        }
        onPayloadConsumed()
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadPdf(context, uri)
        }
    }

    val createTxtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        val contentToWrite = pendingTxtContent
        pendingTxtContent = null
        if (uri != null && contentToWrite != null) {
            scope.launch {
                val saved = writeTextToUri(context, uri, contentToWrite)
                snackbarHostState.showSnackbar(
                    if (saved) "Revision file saved successfully!" else "Failed to save file."
                )
            }
        }
    }

    BackHandler(enabled = state.studyUiState is StudyUiState.Success || state.studyUiState is StudyUiState.Error) {
        viewModel.generateNewPack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                if (
                    keyEvent.type == KeyEventType.KeyDown &&
                    keyEvent.key == Key.Enter &&
                    (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) &&
                    state.studyUiState is StudyUiState.Idle
                ) {
                    viewModel.generateRevisionPack()
                    true
                } else {
                    false
                }
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyVoid, NavyMidnight, IndigoAbyss, PurpleGlow)
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isWideLayout = maxWidth >= 840.dp
                val isCompact = maxWidth < 600.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = if (isCompact) 16.dp else 28.dp,
                            vertical = 16.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = if (isWideLayout) 1080.dp else 720.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        HeroHeader(isCompact = isCompact)

                        AnimatedContent(
                            targetState = state.studyUiState,
                            label = "studyContentTransition"
                        ) { uiState ->
                            when (uiState) {
                                is StudyUiState.Idle -> {
                                    InputSectionCard(
                                        inputMode = state.inputMode,
                                        pastedNotes = state.pastedNotes,
                                        pdfInfo = state.pdfInfo,
                                        isExtractingPdf = state.isExtractingPdf,
                                        pdfError = state.pdfError,
                                        validationError = state.inputValidationError,
                                        isGenerating = false,
                                        onModeChange = viewModel::selectInputMode,
                                        onNotesChange = viewModel::updatePastedNotes,
                                        onPickPdfClick = {
                                            pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                        },
                                        onClearClick = viewModel::clearAll,
                                        onGenerateClick = viewModel::generateRevisionPack
                                    )
                                }

                                is StudyUiState.Loading -> {
                                    LoadingStudyCard()
                                }

                                is StudyUiState.Success -> {
                                    ResultsDashboard(
                                        pack = uiState.pack,
                                        quizSession = state.quizSession,
                                        isWideLayout = isWideLayout,
                                        onSelectQuizOption = viewModel::selectQuizOption,
                                        onSubmitQuiz = viewModel::submitQuiz,
                                        onRetakeQuiz = viewModel::retakeQuiz,
                                        onDownloadSummary = {
                                            pendingTxtContent = viewModel.getSummaryTxt(uiState.pack)
                                            createTxtLauncher.launch("study_buddy_summary.txt")
                                        },
                                        onDownloadCompletePack = {
                                            pendingTxtContent = viewModel.getCompletePackTxt(uiState.pack)
                                            createTxtLauncher.launch("study_buddy_revision_pack.txt")
                                        },
                                        onSharePack = {
                                            val text = viewModel.getCompletePackTxt(uiState.pack)
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, "AI Study Buddy Revision Pack")
                                                putExtra(Intent.EXTRA_TEXT, text)
                                            }
                                            context.startActivity(
                                                Intent.createChooser(shareIntent, "Share Revision Pack")
                                            )
                                        },
                                        onGenerateNewPack = viewModel::generateNewPack
                                    )
                                }

                                is StudyUiState.Error -> {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("error_state_card"),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Text(
                                                text = "Generation Interrupted",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = InkDark
                                            )
                                            ErrorBanner(message = uiState.message)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = viewModel::dismissError,
                                                    shape = RoundedCornerShape(14.dp),
                                                    border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.4f)),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("retry_input_button")
                                                ) {
                                                    Text(
                                                        text = "Edit Notes",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = AccentPurple
                                                    )
                                                }
                                                Button(
                                                    onClick = viewModel::generateRevisionPack,
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("retry_generate_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Refresh,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Try Again",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

private suspend fun writeTextToUri(
    context: android.content.Context,
    uri: Uri,
    content: String
): Boolean = withContext(Dispatchers.IO) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
        } != null
    } catch (e: IOException) {
        false
    }
}
