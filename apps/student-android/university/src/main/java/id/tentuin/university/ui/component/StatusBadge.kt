package id.tentuin.university.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.tentuin.university.ui.theme.*

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        "claimed"   -> "Diklaim"      to Info
        "contacted" -> "Dihubungi"    to Warning
        "qualified" -> "Qualified"    to Primary
        "converted" -> "Converted"    to Success
        "rejected"  -> "Rejected"     to Error
        "released"  -> "Dilepas"      to TextMuted
        else        -> status         to TextMuted
    }
    Text(
        text = label,
        color = Color.White,
        style = TentuinUniversityTypography.labelSmall,
        modifier = modifier
            .background(color, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
