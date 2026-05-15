package id.tentuin.admin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AdminColorScheme = lightColorScheme(
    primary    = Primary,
    onPrimary  = Color.White,
    background = Background,
    surface    = Surface,
)

@Composable
fun TentuinAdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdminColorScheme,
        typography  = TentuinAdminTypography,
        content     = content,
    )
}
