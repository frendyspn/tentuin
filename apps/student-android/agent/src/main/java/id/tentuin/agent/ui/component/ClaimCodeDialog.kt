package id.tentuin.agent.ui.component

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import id.tentuin.agent.ui.theme.*

@Composable
fun ClaimCodeDialog(
    code: String,
    title: String = "Klaim Berhasil",
    description: String = "Bagikan kode ini ke PIC sekolah/kampus untuk verifikasi:",
    expiryNote: String = "Berlaku 30 hari sejak diterbitkan.",
    onDismiss: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val context   = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                title,
                style = TentuinAgentTypography.titleLarge,
                color = TextPrimary,
            )
        },
        text = {
            Column {
                Text(
                    description,
                    style = TentuinAgentTypography.bodyMedium,
                    color = TextSub,
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryLight, RoundedCornerShape(12.dp))
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = code,
                        style = TentuinAgentTypography.headlineSmall,
                        color = Primary,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    expiryNote,
                    style = TentuinAgentTypography.labelSmall,
                    color = TextMuted,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                clipboard.setText(AnnotatedString(code))
                Toast.makeText(context, "Kode disalin", Toast.LENGTH_SHORT).show()
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Salin Kode", color = Primary)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = TextSub)
            }
        },
        containerColor = Surface,
    )
}
