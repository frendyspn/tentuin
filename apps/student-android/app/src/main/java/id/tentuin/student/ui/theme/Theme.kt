package id.tentuin.student.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TentuinColorScheme = lightColorScheme(
    primary          = Primary,
    onPrimary        = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    background       = Background,
    onBackground     = TextPrimary,
    surface          = Surface,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = TextSub,
    error            = Error,
    outline          = Border,
)

@Composable
fun TentuinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TentuinColorScheme,
        typography  = TentuinTypography,
        content     = content,
    )
}
