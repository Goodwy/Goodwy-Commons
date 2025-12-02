package com.goodwy.commons.compose.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.theme.SimpleTheme

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
    title: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        if (title != null) {
            SettingsGroupTitle(title = title)
        }
        Card(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            content()
        }
    }
}

@Composable
fun SettingsGroupTitle(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val primary = SimpleTheme.colorScheme.primary
        val titleStyle = SimpleTheme.typography.titleSmall.copy(color = primary)
        ProvideTextStyle(value = titleStyle) { title() }
    }
}

@MyDevices
@Composable
private fun SettingsGroupPreview() {
    MaterialTheme {
        SettingsGroup(
            title = { Text(text = "Title".uppercase(), modifier = Modifier.padding(start = 36.dp)) }
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(text = "Settings group")
                }
            )
        }
    }
}
