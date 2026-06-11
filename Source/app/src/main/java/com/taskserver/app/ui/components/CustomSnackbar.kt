package com.taskserver.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taskserver.app.ui.theme.*

enum class AlertType { SUCCESS, ERROR, WARNING, INFO }

data class AppAlert(
    val message: String,
    val type: AlertType,
    val action: String? = null
)

@Composable
fun CustomSnackbarVisuals(
    data: SnackbarData,
    alertType: AlertType = AlertType.INFO
) {
    val (bgColor, iconVec, tint) = when (alertType) {
        AlertType.SUCCESS -> Triple(ColorSuccess.copy(0.15f), Icons.Filled.CheckCircle, ColorSuccess)
        AlertType.ERROR   -> Triple(ColorError.copy(0.15f),   Icons.Filled.Error,        ColorError)
        AlertType.WARNING -> Triple(ColorWarning.copy(0.15f), Icons.Filled.Warning,       ColorWarning)
        AlertType.INFO    -> Triple(ColorInfo.copy(0.15f),    Icons.Filled.Info,          ColorInfo)
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 24.dp) // Lifted from bottom
            .shadow(24.dp, RoundedCornerShape(16.dp), spotColor = tint.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            // Inner gradient
            .background(
                androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(bgColor, Color.Transparent)
                )
            )
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = iconVec,
                contentDescription = null,
                tint               = tint,
                modifier           = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text       = data.visuals.message,
                color      = TextPrimary,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.weight(1f)
            )
            data.visuals.actionLabel?.let { label ->
                Spacer(modifier = Modifier.width(12.dp))
                TextButton(
                    onClick = { data.performAction() },
                    colors = ButtonDefaults.textButtonColors(contentColor = tint)
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

object SnackbarController {
    suspend fun showAlert(
        host: SnackbarHostState,
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) = host.showSnackbar(
        message     = message,
        actionLabel = actionLabel,
        duration    = duration
    )
}
