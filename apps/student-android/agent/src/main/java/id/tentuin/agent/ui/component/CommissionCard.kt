package id.tentuin.agent.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.tentuin.agent.core.util.toRupiah
import id.tentuin.agent.data.model.AgentCommission
import id.tentuin.agent.ui.theme.*

@Composable
fun CommissionCard(
    commission: AgentCommission,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = monthLabel(commission.month, commission.year),
                    style = TentuinAgentTypography.titleMedium,
                    color = TextPrimary,
                )
                StatusBadge(status = commission.status)
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                AmountColumn(
                    label = "Stream A",
                    value = commission.streamAAmount.toRupiah(),
                    modifier = Modifier.weight(1f),
                )
                AmountColumn(
                    label = "Stream B",
                    value = commission.streamBAmount.toRupiah(),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Total", style = TentuinAgentTypography.bodyMedium, color = TextSub)
                Text(
                    text = commission.totalAmount.toRupiah(),
                    style = TentuinAgentTypography.titleMedium,
                    color = Primary,
                )
            }

            commission.notes?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    style = TentuinAgentTypography.bodySmall,
                    color = TextMuted,
                )
            }
        }
    }
}

@Composable
private fun AmountColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = TentuinAgentTypography.labelSmall, color = TextMuted)
        Text(value, style = TentuinAgentTypography.bodyLarge, color = TextPrimary)
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (label, color) = when (status) {
        "paid"      -> "Dibayar" to Success
        "cancelled" -> "Dibatalkan" to Error
        else        -> "Pending" to Warning
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, style = TentuinAgentTypography.labelSmall, color = color)
    }
}

private fun monthLabel(month: Int, year: Int): String {
    val names = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember",
    )
    val name = names.getOrNull(month - 1) ?: "Bulan $month"
    return "$name $year"
}
