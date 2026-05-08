package id.tentuin.student.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import id.tentuin.student.data.model.UniversityRow
import id.tentuin.student.ui.theme.Border
import id.tentuin.student.ui.theme.PartnerBlue
import id.tentuin.student.ui.theme.PartnerGold
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextMuted
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub

@Composable
fun UniversityCard(
    university: UniversityRow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPartnerPremium = university.isPartner && university.partnerTier == "premium"
    val isPartnerBasic   = university.isPartner && university.partnerTier == "basic"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isPartnerPremium) 1.5.dp else 1.dp,
                color = when {
                    isPartnerPremium -> PartnerGold.copy(alpha = 0.6f)
                    isPartnerBasic   -> PartnerBlue.copy(alpha = 0.4f)
                    else             -> Border
                },
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            // Logo or initials avatar
            if (!university.logoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = university.logoUrl,
                    contentDescription = university.shortName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary.copy(alpha = 0.12f)),
                ) {
                    Text(
                        text = university.shortName.take(2).uppercase(),
                        style = TentuinTypography.titleMedium,
                        color = Primary,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = university.name,
                    style = TentuinTypography.titleSmall,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TypeBadge(type = university.type)
                    Text(
                        text = "${university.city}, ${university.province}",
                        style = TentuinTypography.bodySmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeBadge(type: String) {
    val (bg, fg, label) = when (type.lowercase()) {
        "negeri" -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), "PTN")
        else     -> Triple(Color(0xFFFFEDD5), Color(0xFF9A3412), "PTS")
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text = label, style = TentuinTypography.labelSmall, color = fg)
    }
}
