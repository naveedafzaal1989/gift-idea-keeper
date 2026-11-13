package com.giftidea.keeper.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NotebookColors = darkColorScheme(
    primary = Primary, onPrimary = Background, primaryContainer = Secondary, onPrimaryContainer = TextPrimary,
    secondary = Secondary, onSecondary = TextPrimary, tertiary = Action, onTertiary = TextPrimary,
    background = Background, onBackground = TextPrimary, surface = BackgroundRaised, onSurface = TextPrimary,
    surfaceVariant = Secondary, onSurfaceVariant = TextSecondary, outline = Outline, error = ErrorLight,
    onError = Background, errorContainer = Ribbon, onErrorContainer = PaperLight
)

@Composable
fun AppTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = NotebookColors, typography = AppTypography, content = content) }
