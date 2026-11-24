package com.example.offlinesubplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SkyAccent = Color(0xFF7DD3FC)
val DeepMidnight = Color(0xFF090D16)
val SurfaceElevated = Color(0xFF131A29)
val SurfaceContainerHigh = Color(0xFF1C2538)

private val OfflineSubPlayerColorScheme = darkColorScheme(
    primary = SkyAccent,
    onPrimary = DeepMidnight,
    secondary = SkyAccent,
    onSecondary = DeepMidnight,
    secondaryContainer = SurfaceContainerHigh,
    onSecondaryContainer = Color.White,
    surface = DeepMidnight,
    onSurface = Color.White,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = Color.White.copy(alpha = 0.78f),
    background = DeepMidnight,
    onBackground = Color.White,
    outline = Color.White.copy(alpha = 0.35f)
)

@Composable
fun OfflineSubPlayerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OfflineSubPlayerColorScheme,
        typography = Typography(),
        content = content
    )
}
