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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.InputMode
import com.example.model.PdfDocumentInfo
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentBlueSoft
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleSoft
import com.example.ui.theme.CardSoftGrey
import com.example.ui.theme.CardSubtleBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ErrorRedBorder
import com.example.ui.theme.ErrorRedSoft
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSubtle
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenSoft
import java.text.NumberFormat

@Composable
fun InputSectionCard(
    inputMode: InputMode,
    pastedNotes: String,
    pdfInfo: PdfDocumentInfo?,
    isExtractingPdf: Boolean,
    pdfError: String?,
    validationError: String?,
    isGenerating: Boolean,
    onModeChange: (InputMode) -> Unit,
    onNotesChange: (String) -> Unit,
    onPickPdfClick: () -> Unit,
    onClearClick: () -> Unit,
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.upload_section_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = InkDark
                    )
                    Text(
                        text = "Choose notes or PDF to build your study pack",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSubtle
                    )
                }
            }

            // Segmented Input Mode Switcher
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardSoftGrey,
                border = BorderStroke(1.dp, CardSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ModeTabButton(
                        selected = inputMode == InputMode.PASTE_NOTES,
                        icon = Icons.Rounded.EditNote,
                        label = stringResource(R.string.tab_paste_notes),
                        onClick = { onModeChange(InputMode.PASTE_NOTES) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_paste_notes")
                    )
                    ModeTabButton(
                        selected = inputMode == InputMode.UPLOAD_PDF,
                        icon = Icons.Rounded.PictureAsPdf,
                        label = stringResource(R.string.tab_upload_pdf),
                        onClick = { onModeChange(InputMode.UPLOAD_PDF) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_upload_pdf")
                    )
                }
            }

            if (inputMode == InputMode.PASTE_NOTES) {
                PasteNotesSection(
                    notes = pastedNotes,
                    onNotesChange = onNotesChange
                )
            } else {
                UploadPdfSection(
                    pdfInfo = pdfInfo,
                    isExtracting = isExtractingPdf,
                    pdfError = pdfError,
                    onPickPdfClick = onPickPdfClick
                )
            }

            AnimatedVisibility(visible = validationError != null) {
                validationError?.let { errorMsg ->
                    ErrorBanner(message = errorMsg)
                }
            }

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onGenerateClick,
                    enabled = !isGenerating && !isExtractingPdf,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_pack_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (!isGenerating && !isExtractingPdf) {
                                        listOf(AccentPurple, AccentBlue)
                                    } else {
                                        listOf(
                                            AccentPurple.copy(alpha = 0.5f),
                                            AccentBlue.copy(alpha = 0.5f)
                                        )
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.generate_cta),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val hasInput = pastedNotes.isNotBlank() || pdfInfo != null
                if (hasInput && !isGenerating) {
                    OutlinedButton(
                        onClick = onClearClick,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardSubtleBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("clear_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = stringResource(R.string.cd_clear_input),
                            tint = InkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.clear_reset),
                            style = MaterialTheme.typography.labelLarge,
                            color = InkMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeTabButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) CardWhite else Color.Transparent,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .pointerHoverIcon(PointerIcon.Hand)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) AccentPurple else InkMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) InkDark else InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PasteNotesSection(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    val formattedCount = NumberFormat.getIntegerInstance().format(notes.length)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.notes_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSubtle
                )
            },
            minLines = 6,
            maxLines = 11,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardSoftGrey,
                unfocusedContainerColor = CardSoftGrey,
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = CardSubtleBorder,
                focusedTextColor = InkDark,
                unfocusedTextColor = InkDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notes_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tip: Paste a chapter, lecture notes, or article",
                style = MaterialTheme.typography.bodySmall,
                color = InkSubtle
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentPurpleSoft
            ) {
                Text(
                    text = "$formattedCount chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentPurple,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("notes_char_counter")
                )
            }
        }
    }
}

@Composable
private fun UploadPdfSection(
    pdfInfo: PdfDocumentInfo?,
    isExtracting: Boolean,
    pdfError: String?,
    onPickPdfClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CardSoftGrey,
            border = BorderStroke(1.5.dp, AccentPurple.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon.Hand)
                .clip(RoundedCornerShape(18.dp))
                .clickable(enabled = !isExtracting, onClick = onPickPdfClick)
                .testTag("select_pdf_button")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isExtracting) {
                    CircularProgressIndicator(
                        color = AccentPurple,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = "Extracting text from PDF...",
                        style = MaterialTheme.typography.titleSmall,
                        color = InkDark
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AccentBlueSoft
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.UploadFile,
                            contentDescription = stringResource(R.string.cd_pdf_file),
                            tint = AccentBlue,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(28.dp)
                        )
                    }
                    Text(
                        text = if (pdfInfo == null) {
                            stringResource(R.string.choose_pdf)
                        } else {
                            stringResource(R.string.change_pdf)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = AccentPurple
                    )
                    Text(
                        text = "Supports PDF textbooks, lecture slides & study guides (up to 18 MB)",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSubtle
                    )
                }
            }
        }

        AnimatedVisibility(visible = pdfError != null) {
            pdfError?.let { ErrorBanner(message = it) }
        }

        if (pdfInfo != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SuccessGreenSoft,
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pdf_file_info")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PictureAsPdf,
                        contentDescription = stringResource(R.string.cd_pdf_file),
                        tint = SuccessGreen,
                        modifier = Modifier.size(26.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pdfInfo.fileName,
                            style = MaterialTheme.typography.titleSmall,
                            color = InkDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${pdfInfo.pageCount} pages • ${pdfInfo.formattedSize}" +
                                if (pdfInfo.isScannedFallback) " • Scanned PDF (AI Vision)" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen
                        )
                    }
                }
            }

            if (pdfInfo.extractedText.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.pdf_preview_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = InkDark
                        )
                        Text(
                            text = "${NumberFormat.getIntegerInstance().format(pdfInfo.extractedText.length)} chars",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentBlue
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardSoftGrey,
                        border = BorderStroke(1.dp, CardSubtleBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 160.dp)
                    ) {
                        Text(
                            text = pdfInfo.extractedText.take(1500) +
                                if (pdfInfo.extractedText.length > 1500) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                                .testTag("pdf_text_preview")
                        )
                    }
                }
            } else if (pdfInfo.isScannedFallback) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentBlueSoft
                ) {
                    Text(
                        text = "This PDF appears to be scanned/image-based. Gemini AI will read the PDF pages directly when generating your revision pack.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentBlue,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ErrorRedSoft,
        border = BorderStroke(1.dp, ErrorRedBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = ErrorRed
            )
        }
    }
}
