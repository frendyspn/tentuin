package id.tentuin.agent.ui.component.toast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.tentuin.agent.ui.theme.Error
import id.tentuin.agent.ui.theme.Success
import id.tentuin.agent.ui.theme.TentuinAgentTypography

@Composable
fun TentuinToast(
    message: String,
    type: ToastType = ToastType.SUCCESS,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (type == ToastType.SUCCESS) Success else Error
    
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = TentuinAgentTypography.bodyMedium,
            color = Color.White
        )
    }
}

enum class ToastType { SUCCESS, ERROR }
