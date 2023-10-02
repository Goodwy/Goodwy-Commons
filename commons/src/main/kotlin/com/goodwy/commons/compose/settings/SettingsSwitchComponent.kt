package com.goodwy.commons.compose.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.goodwy.commons.R
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.extensions.NoRippleTheme
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
    onChange: ((Boolean) -> Unit)? = null,
    switchColors: SwitchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedIconColor = Color.Black
    ),
    checkedIcon: Boolean = false,
) {
    val interactionSource = rememberMutableInteractionSource()
    val indication = LocalIndication.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onChange?.invoke(!initialValue) },
                interactionSource = interactionSource,
                indication = indication
            )
            .padding(horizontal = 20.dp, vertical = 2.dp),
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
                        dimensionResource(id = R.dimen.normal_text_size).toSp()
                    },
                    lineHeight = with(LocalDensity.current) {
                        dimensionResource(id = R.dimen.bigger_text_size).toSp()
                    },
                )
            }
        }
        CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
            Switch(
                checked = initialValue,
                onCheckedChange = { onChange?.invoke(it) },
                enabled = isPreferenceEnabled,
                colors = switchColors,
                interactionSource = interactionSource,
                thumbContent = {
                    if (checkedIcon && initialValue) {
                        Icon(
                            modifier = Modifier.padding(4.dp),
                            imageVector = Icons.Rounded.Check,
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
private fun SettingsSwitchComponentPreview() {
    AppThemeSurface {
        SettingsSwitchComponent(
            label = "Some label",
            value = "Some value",
        )
    }
}
