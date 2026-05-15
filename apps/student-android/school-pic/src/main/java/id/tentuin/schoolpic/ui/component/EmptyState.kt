package id.tentuin.schoolpic.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.tentuin.schoolpic.ui.theme.TentuinSchoolPicTypography
import id.tentuin.schoolpic.ui.theme.TextMuted

@Composable
fun EmptyState(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = TentuinSchoolPicTypography.titleLarge, color = TextMuted)
        if (subtitle != null) {
            Text(subtitle, style = TentuinSchoolPicTypography.bodyMedium, color = TextMuted)
        }
    }
}
