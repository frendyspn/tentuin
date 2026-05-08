package id.tentuin.student.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.riasecColor

@Composable
fun RiasecChip(
    code: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TentuinTypography.labelSmall
) {
    val color = riasecColor(code)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = code.uppercase(),
            style = textStyle,
            color = color,
        )
    }
}
