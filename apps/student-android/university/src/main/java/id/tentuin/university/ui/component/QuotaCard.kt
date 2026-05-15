package id.tentuin.university.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.tentuin.university.ui.theme.*

@Composable
fun QuotaCard(
    quotaBalance: Int,
    totalPurchased: Int,
    accountTypeLabel: String,
    onTopUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(listOf(Primary, PrimaryDark)),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Storage, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sisa Kuota Data", color = Color.White.copy(alpha = 0.9f), style = TentuinUniversityTypography.labelSmall)
            Spacer(Modifier.weight(1f))
            Text(accountTypeLabel, color = Color.White.copy(alpha = 0.9f), style = TentuinUniversityTypography.labelSmall,
                modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "%,d".format(quotaBalance),
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "dari $totalPurchased data yang pernah dibeli",
            color = Color.White.copy(alpha = 0.85f),
            style = TentuinUniversityTypography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        TentuinButton(
            text = "Top-up Kuota",
            onClick = onTopUp,
            containerColor = Color.White.copy(alpha = 0.2f),
        )
    }
}
