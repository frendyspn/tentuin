package id.tentuin.schoolpic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.tentuin.schoolpic.ui.theme.Error

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Error)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    )
}

@Composable
fun InfoBanner(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF10B981))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    )
}
