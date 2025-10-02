package com.goodwy.commons.samples.dialogs

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goodwy.commons.compose.extensions.rememberMutableInteractionSource

@Composable
fun ThemeColorsDialog(
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Current theme colors",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                // Primary colors
                ThemeColorItem("primary", colorScheme.primary)
                ThemeColorItem("onPrimary", colorScheme.onPrimary)
                ThemeColorItem("primaryContainer", colorScheme.primaryContainer)
                ThemeColorItem("onPrimaryContainer", colorScheme.onPrimaryContainer)

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary colors
                ThemeColorItem("secondary", colorScheme.secondary)
                ThemeColorItem("onSecondary", colorScheme.onSecondary)
                ThemeColorItem("secondaryContainer", colorScheme.secondaryContainer)
                ThemeColorItem("onSecondaryContainer", colorScheme.onSecondaryContainer)

                Spacer(modifier = Modifier.height(8.dp))

                // Tertiary colors
                ThemeColorItem("tertiary", colorScheme.tertiary)
                ThemeColorItem("onTertiary", colorScheme.onTertiary)
                ThemeColorItem("tertiaryContainer", colorScheme.tertiaryContainer)
                ThemeColorItem("onTertiaryContainer", colorScheme.onTertiaryContainer)

                Spacer(modifier = Modifier.height(8.dp))

                // Error colors
                ThemeColorItem("error", colorScheme.error)
                ThemeColorItem("onError", colorScheme.onError)
                ThemeColorItem("errorContainer", colorScheme.errorContainer)
                ThemeColorItem("onErrorContainer", colorScheme.onErrorContainer)

                Spacer(modifier = Modifier.height(8.dp))

                // Background colors
                ThemeColorItem("background", colorScheme.background)
                ThemeColorItem("onBackground", colorScheme.onBackground)

                Spacer(modifier = Modifier.height(8.dp))

                // Surface colors
                ThemeColorItem("surface", colorScheme.surface)
                ThemeColorItem("onSurface", colorScheme.onSurface)
                ThemeColorItem("surfaceVariant", colorScheme.surfaceVariant)
                ThemeColorItem("onSurfaceVariant", colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(8.dp))

                // Outline colors
                ThemeColorItem("outline", colorScheme.outline)
                ThemeColorItem("outlineVariant", colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(8.dp))

                // Inverse colors
                ThemeColorItem("inverseSurface", colorScheme.inverseSurface)
                ThemeColorItem("inverseOnSurface", colorScheme.inverseOnSurface)
                ThemeColorItem("inversePrimary", colorScheme.inversePrimary)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun ThemeColorItem(
    colorName: String,
    color: Color
) {
    val interactionSource = rememberMutableInteractionSource()
    val indication = LocalIndication.current
    val clipboardManager = LocalClipboardManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                onClick = {
                    clipboardManager.setText(AnnotatedString(color.toHex()))
                },
                interactionSource = interactionSource,
                indication = indication
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color preview
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Color name and hex value
        Column {
            Text(
                text = colorName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = color.toHex(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Extension function to convert Color to hex string
fun Color.toHex(): String {
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    val alpha = (alpha * 255).toInt()

    return if (alpha == 255) {
        String.format("#%02X%02X%02X", red, green, blue)
    } else {
        String.format("#%02X%02X%02X%02X", red, green, blue, alpha)
    }
}
