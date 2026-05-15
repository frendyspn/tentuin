package id.tentuin.schoolpic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SchoolPicColorScheme = lightColorScheme(
    primary    = Primary,
    onPrimary  = Color.White,
    background = Background,
    surface    = Surface,
)

@Composable
fun TentuinSchoolPicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SchoolPicColorScheme,
        typography  = TentuinSchoolPicTypography,
        content     = content,
    )
}
