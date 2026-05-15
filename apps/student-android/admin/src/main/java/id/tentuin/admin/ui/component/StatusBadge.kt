package id.tentuin.admin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tentuin.admin.ui.theme.Error
import id.tentuin.admin.ui.theme.Info
import id.tentuin.admin.ui.theme.Success
import id.tentuin.admin.ui.theme.Warning

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        "active",  "transferred"          -> "Aktif"      to Success
        "approved"                        -> "Disetujui"  to Info
        "requested", "pending"            -> "Menunggu"   to Warning
        "suspended", "rejected", "cancelled","inactive" -> {
            when (status) {
                "suspended"             -> "Suspend"    to Error
                "rejected"              -> "Ditolak"    to Error
                "cancelled"             -> "Dibatalkan" to Error
                else                     -> "Nonaktif"   to Error
            }
        }
        "paid"                            -> "Dibayar"   to Success
        else                              -> status     to Info
    }
    Text(
        text = label,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        color = color,
        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
    )
}
