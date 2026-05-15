package id.tentuin.university.ui.component.toast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.tentuin.university.ui.theme.Error
import id.tentuin.university.ui.theme.Success
import id.tentuin.university.ui.theme.TentuinUniversityTypography

enum class ToastType { SUCCESS, ERROR }

@Composable
fun TentuinToast(message: String, type: ToastType = ToastType.SUCCESS, modifier: Modifier = Modifier) {
    val bg = if (type == ToastType.SUCCESS) Success else Error
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, style = TentuinUniversityTypography.bodyMedium, color = Color.White)
    }
}
