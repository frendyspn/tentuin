package id.tentuin.schoolpic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.tentuin.schoolpic.ui.theme.Error
import id.tentuin.schoolpic.ui.theme.Success
import id.tentuin.schoolpic.ui.theme.TentuinSchoolPicTypography
import id.tentuin.schoolpic.ui.theme.Warning

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (label, bg) = when (status) {
        "paid"      -> "Dibayar"   to Success
        "pending"   -> "Menunggu"  to Warning
        "cancelled" -> "Dibatalkan" to Error
        else        -> status      to Color(0xFF9CA3AF)
    }
    Text(
        text = label,
        color = Color.White,
        style = TentuinSchoolPicTypography.labelSmall,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
