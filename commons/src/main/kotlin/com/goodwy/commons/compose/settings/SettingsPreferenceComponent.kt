package com.goodwy.commons.compose.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.goodwy.commons.R
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.compose.theme.preferenceLabelColor
import com.goodwy.commons.compose.theme.preferenceValueColor

@Composable
fun SettingsPreferenceComponent(
    modifier: Modifier = Modifier,
    label: String,
    sublabel: String? = null,
    value: String? = null,
    showChevron: Boolean = false,
    isPreferenceEnabled: Boolean = true,
    doOnPreferenceLongClick: (() -> Unit)? = null,
    doOnPreferenceClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    preferenceValueColor: Color = preferenceValueColor(isEnabled = isPreferenceEnabled),
    preferenceLabelColor: Color = preferenceLabelColor(isEnabled = isPreferenceEnabled)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = backgroundColor)
            .combinedClickable(
                enabled = isPreferenceEnabled,
                onClick = { doOnPreferenceClick?.invoke() },
                onLongClick = { doOnPreferenceLongClick?.invoke() },
            )
            .padding(start = 22.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 42.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                text = label,
                color = preferenceLabelColor,
                fontSize = with(LocalDensity.current) {
                    dimensionResource(id = R.dimen.bigger_text_size).toSp()
                },
                lineHeight = with(LocalDensity.current) {
                    dimensionResource(id = R.dimen.big_text_size).toSp()
                },
            )
            AnimatedVisibility(visible = !sublabel.isNullOrBlank()) {
                Text(
                    text = sublabel.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, end = 16.dp),
                    color = preferenceLabelColor,
                    fontSize = with(LocalDensity.current) {
                        dimensionResource(id = R.dimen.smaller_text_size).toSp()
                    },
                    lineHeight = with(LocalDensity.current) {
                        dimensionResource(id = R.dimen.normal_text_size).toSp()
                    },
                )
            }
        }
        AnimatedVisibility(visible = !value.isNullOrBlank()) {
            Text(
                text = value.toString(),
                modifier = Modifier.padding(end = 6.dp),
//                    .fillMaxWidth(),
                color = preferenceValueColor,
                fontSize = with(LocalDensity.current) {
                    dimensionResource(id = R.dimen.bigger_text_size).toSp()
                },
                lineHeight = with(LocalDensity.current) {
                    dimensionResource(id = R.dimen.big_text_size).toSp()
                },
            )
        }
        if (showChevron) Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = label
        )
    }
}

@MyDevices
@Composable
private fun SettingsPreferencePreview() {
    AppThemeSurface {
        SettingsPreferenceComponent(
            label = stringResource(id = R.string.language),
            sublabel = stringResource(id = R.string.translation_english),
            value = stringResource(id = R.string.translation_english),
            showChevron = true,
            isPreferenceEnabled = true,
        )
    }
}
