package id.tentuin.agent.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.tentuin.agent.ui.theme.*

@Composable
fun ClaimStatusChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(label, style = TentuinAgentTypography.labelSmall, color = color)
    }
}

@Composable
fun ClaimCodeChip(
    code: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(PrimaryLight, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.ContentCopy,
            contentDescription = "Salin kode",
            modifier = Modifier.size(14.dp),
            tint = Primary,
        )
        Spacer(Modifier.width(6.dp))
        Text(code, style = TentuinAgentTypography.labelSmall, color = Primary)
    }
}
