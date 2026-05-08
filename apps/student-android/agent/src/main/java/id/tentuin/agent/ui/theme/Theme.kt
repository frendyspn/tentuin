package id.tentuin.agent.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AgentColorScheme = lightColorScheme(
    primary            = Primary,
    onPrimary          = androidx.compose.ui.graphics.Color.White,
    background         = Background,
    surface            = Surface,
)

@Composable
fun TentuinAgentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AgentColorScheme,
        typography  = TentuinAgentTypography,
        content     = content,
    )
}
