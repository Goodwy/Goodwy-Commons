package com.goodwy.commons.compose.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.goodwy.commons.R
import com.goodwy.commons.compose.extensions.BooleanPreviewParameterProvider
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.extensions.rememberMutableInteractionSource
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.compose.theme.preferenceLabelColor
import com.goodwy.commons.compose.theme.preferenceValueColor

@Composable
fun SettingsSwitchComponent(
    modifier: Modifier = Modifier,
    label: String,
    value: String? = null,
    initialValue: Boolean = false,
    isPreferenceEnabled: Boolean = true,
    showCheckmark: Boolean,
    checkmark: ImageVector = Icons.Rounded.Check,
    onChange: ((Boolean) -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    switchColors: SwitchColors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onSecondary,//Color.White,
        checkedTrackColor = MaterialTheme.colorScheme.secondary,
        checkedIconColor = Color.Black
    ),
    scaleSwitch: Float = 1F,
) {
    val interactionSource = rememberMutableInteractionSource()
    val indication = LocalIndication.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(color = backgroundColor)
            .clickable(
                enabled = isPreferenceEnabled,
                onClick = { onChange?.invoke(!initialValue) },
                interactionSource = interactionSource,
                indication = indication
            )
            .padding(start = 22.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                text = label,
                color = preferenceLabelColor(isEnabled = isPreferenceEnabled),
                fontSize = with(LocalDensity.current) {
                    dimensionResource(id = R.dimen.bigger_text_size).toSp()
                },
                lineHeight = with(LocalDensity.current) {
                    dimensionResource(id = R.dimen.big_text_size).toSp()
                },
            )
            AnimatedVisibility(visible = !value.isNullOrBlank()) {
                Text(
                    text = value.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    color = preferenceValueColor(isEnabled = isPreferenceEnabled),
                    fontSize = with(LocalDensity.current) {
                        dimensionResource(id = R.dimen.smaller_text_size).toSp()
                    },
                    lineHeight = with(LocalDensity.current) {
                        dimensionResource(id = R.dimen.normal_text_size).toSp()
                    },
                )
            }
        }
        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            Switch(
                modifier = Modifier.scale(scaleSwitch),
                checked = initialValue,
                onCheckedChange = { onChange?.invoke(it) },
                enabled = isPreferenceEnabled,
                colors = switchColors,
                interactionSource = interactionSource,
                thumbContent = {
                    if (showCheckmark && initialValue) {
                        Icon(
                            modifier = Modifier.padding(4.dp),
                            imageVector = checkmark,
                            contentDescription = label,
                        )
                    }
                }
            )
        }
    }
}

@MyDevices
@Composable
private fun SettingsSwitchComponentPreview(@PreviewParameter(BooleanPreviewParameterProvider::class) isChecked: Boolean) {
    var checked by remember { mutableStateOf(isChecked) }
    AppThemeSurface {
        SettingsSwitchComponent(
            label = "Some label",
            value = "Some value",
            initialValue = checked,
            showCheckmark = false,
            onChange = {
                checked = it
            }
        )
    }
}
