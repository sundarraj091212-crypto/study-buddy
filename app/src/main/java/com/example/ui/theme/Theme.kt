package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val StudyBuddyColorScheme = lightColorScheme(
    primary = AccentPurple,
    onPrimary = CardWhite,
    primaryContainer = AccentPurpleSoft,
    onPrimaryContainer = AccentPurpleDark,
    secondary = AccentBlue,
    onSecondary = CardWhite,
    secondaryContainer = AccentBlueSoft,
    onSecondaryContainer = AccentBlue,
    tertiary = AccentCyan,
    onTertiary = CardWhite,
    background = NavyVoid,
    onBackground = InkOnDark,
    surface = CardWhite,
    onSurface = InkDark,
    surfaceVariant = CardSoftGrey,
    onSurfaceVariant = InkMuted,
    outline = CardSubtleBorder,
    error = ErrorRed,
    onError = CardWhite,
    errorContainer = ErrorRedSoft,
    onErrorContainer = ErrorRed
)

val StudyBuddyShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun StudyBuddyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudyBuddyColorScheme,
        typography = StudyBuddyTypography,
        shapes = StudyBuddyShapes,
        content = content
    )
}
