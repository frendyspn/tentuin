package id.tentuin.university.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.tentuin.university.ui.theme.TentuinUniversityTypography
import id.tentuin.university.ui.theme.TextMuted
import id.tentuin.university.ui.theme.TextPrimary

@Composable
fun EmptyState(
    title: String,
    description: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, style = TentuinUniversityTypography.titleMedium, color = TextPrimary, textAlign = TextAlign.Center)
        description?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = TentuinUniversityTypography.bodyMedium, color = TextMuted, textAlign = TextAlign.Center)
        }
        if (actionLabel != null && onActionClick != null) {
            Spacer(Modifier.height(16.dp))
            TentuinButton(text = actionLabel, onClick = onActionClick)
        }
    }
}
