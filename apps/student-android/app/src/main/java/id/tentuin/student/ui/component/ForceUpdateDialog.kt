package id.tentuin.student.ui.component

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextMuted
import id.tentuin.student.ui.theme.TextPrimary

@Composable
fun ForceUpdateDialog(storeUrl: String) {
    val context = LocalContext.current

    // Block back button — user MUST update
    BackHandler(enabled = true) {}

    AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Perbarui Aplikasi",
                style = TentuinTypography.headlineSmall,
                color = TextPrimary,
            )
        },
        text = {
            Column {
                Text(
                    text = "Versi aplikasi yang kamu gunakan sudah tidak didukung. Harap perbarui ke versi terbaru untuk melanjutkan.",
                    style = TentuinTypography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Start,
                )
            }
        },
        confirmButton = {
            TentuinButton(
                text = "Perbarui Sekarang",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        },
    )
}
