package id.tentuin.university.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UniversityColorScheme = lightColorScheme(
    primary    = Primary,
    onPrimary  = Color.White,
    background = Background,
    surface    = Surface,
)

@Composable
fun TentuinUniversityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UniversityColorScheme,
        typography  = TentuinUniversityTypography,
        content     = content,
    )
}
