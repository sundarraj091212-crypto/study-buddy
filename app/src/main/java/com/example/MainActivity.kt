package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.firebase.configureAppCheck
import com.example.ui.StudyBuddyScreen
import com.example.ui.theme.StudyBuddyTheme

data class IncomingStudyPayload(
    val sharedText: String? = null,
    val pdfUri: Uri? = null,
    val nonce: Long = System.nanoTime()
)

class MainActivity : ComponentActivity() {
    private var incomingPayload by mutableStateOf<IncomingStudyPayload?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before rendering UI or ViewModels.
        configureAppCheck(this, intent)
        handleIncomingIntent(intent)

        enableEdgeToEdge()
        setContent {
            StudyBuddyTheme {
                StudyBuddyScreen(
                    incomingPayload = incomingPayload,
                    onPayloadConsumed = { incomingPayload = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val mime = intent.type
                if (mime == "text/plain") {
                    val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!text.isNullOrBlank()) {
                        incomingPayload = IncomingStudyPayload(sharedText = text)
                    }
                } else if (mime == "application/pdf") {
                    val uri = extractStreamUri(intent)
                    if (uri != null) {
                        incomingPayload = IncomingStudyPayload(pdfUri = uri)
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                val uri = intent.data
                if (uri != null) {
                    incomingPayload = IncomingStudyPayload(pdfUri = uri)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun extractStreamUri(intent: Intent): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
    }
}
