package id.tentuin.admin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.tentuin.admin.ui.theme.Error
import id.tentuin.admin.ui.theme.Info
import id.tentuin.admin.ui.theme.Success
import id.tentuin.admin.ui.theme.TentuinAdminTypography

enum class ToastType { SUCCESS, ERROR, INFO }

@Composable
fun TentuinToast(
    message: String,
    type: ToastType = ToastType.SUCCESS,
    modifier: Modifier = Modifier,
) {
    val bg = when (type) {
        ToastType.SUCCESS -> Success
        ToastType.ERROR   -> Error
        ToastType.INFO    -> Info
    }
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, style = TentuinAdminTypography.bodyMedium, color = Color.White)
    }
}
