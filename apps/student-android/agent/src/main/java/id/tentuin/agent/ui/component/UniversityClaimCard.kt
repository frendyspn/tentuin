package id.tentuin.agent.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.tentuin.agent.data.model.UniversityBrief
import id.tentuin.agent.data.model.UniversityClaim
import id.tentuin.agent.ui.theme.*

@Composable
fun UniversityClaimCard(
    university: UniversityBrief,
    claim: UniversityClaim?,
    isMyClaim: Boolean,
    isClaiming: Boolean,
    onClaim: () -> Unit,
    onTapCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = university.name,
                        style = TentuinAgentTypography.titleMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = "${university.shortName} • ${university.city}",
                        style = TentuinAgentTypography.bodySmall,
                        color = TextMuted,
                    )
                }

                if (university.isPartner) {
                    val tierLabel = when (university.partnerTier) {
                        "premium" -> "PREMIUM"
                        "partner" -> "PARTNER"
                        else      -> "BASIC"
                    }
                    Box(
                        modifier = Modifier
                            .background(Primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = tierLabel,
                            style = TentuinAgentTypography.labelSmall,
                            color = Primary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Sisa kuota: ${university.quotaBalance}",
                    style = TentuinAgentTypography.labelSmall,
                    color = TextSub,
                )

                ClaimAction(
                    claim = claim,
                    isMyClaim = isMyClaim,
                    isClaiming = isClaiming,
                    onClaim = onClaim,
                    onTapCode = onTapCode,
                )
            }
        }
    }
}

@Composable
private fun ClaimAction(
    claim: UniversityClaim?,
    isMyClaim: Boolean,
    isClaiming: Boolean,
    onClaim: () -> Unit,
    onTapCode: (String) -> Unit,
) {
    when {
        claim == null -> {
            TentuinButton(
                text = "Klaim",
                onClick = onClaim,
                modifier = Modifier.height(36.dp),
                isLoading = isClaiming,
            )
        }
        isMyClaim && claim.status == "pending" -> {
            Column(horizontalAlignment = Alignment.End) {
                ClaimStatusChip(label = "Menunggu Approval", color = Warning)
                Spacer(Modifier.height(4.dp))
                claim.claimCode?.let { code ->
                    ClaimCodeChip(code = code, onClick = { onTapCode(code) })
                }
            }
        }
        isMyClaim && claim.status == "active" -> {
            ClaimStatusChip(label = "Aktif", color = Success)
        }
        else -> {
            ClaimStatusChip(label = "Diklaim Agen Lain", color = TextMuted)
        }
    }
}
