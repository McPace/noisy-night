package com.papapace.noisynight.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.papapace.noisynight.ui.GoldenAccent
import com.papapace.noisynight.ui.MidnightBlue
import com.papapace.noisynight.ui.TextWhite

private val DarkColorScheme = darkColorScheme(
    primary = GoldenAccent,
    background = Color.Black,
    surface = MidnightBlue,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun NoisyNightTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}
