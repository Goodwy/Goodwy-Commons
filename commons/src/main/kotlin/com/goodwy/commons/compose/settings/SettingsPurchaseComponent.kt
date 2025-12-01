package com.goodwy.commons.compose.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.R
import com.goodwy.strings.R as StringsR

@Composable
fun SettingsPurchaseComponent(
    onPurchaseClick: () -> Unit,
    enabledShake: Boolean = false,
    onShakeFinished: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .shake(enabledShake, onShakeFinished)
            .clickable { onPurchaseClick() }
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Card(
            modifier = Modifier.padding(start = 32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                modifier = Modifier
                    .size(80.dp)
                    .padding(10.dp),
                painter = painterResource(id = R.drawable.ic_plus_support),
                contentDescription = stringResource(id = R.string.donate),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Column(
            modifier = Modifier
                .heightIn(96.dp)
                .padding(bottom = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                modifier = Modifier.padding(start = 28.dp, end = 12.dp),
                text = stringResource(StringsR.string.action_support_project),
                fontSize = 14.sp,
            )
            Text(
                modifier = Modifier.padding(start = 28.dp, end = 12.dp),
                text = stringResource(StringsR.string.pref_pay_summary),
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = LocalContentColor.current.copy(alpha = 0.5F),
            )
            Button(
                modifier = Modifier.padding(start = 28.dp, top = 6.dp).wrapContentWidth()
                    .height(20.dp).alpha(0.6f),
                onClick = { onPurchaseClick() },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    text = stringResource(StringsR.string.learn_more).toUpperCase(LocaleList.current),
                    fontSize = 10.sp,
                )
            }
        }
    }
    Spacer(modifier = Modifier.size(8.dp))
}

@Composable
fun Modifier.shake(
    enabled: Boolean,
    onAnimationFinish: () -> Unit
): Modifier = composed(
    factory = {
        val distance by animateFloatAsState(
            targetValue = if (enabled) 12f else 0f,
            animationSpec = repeatable(
                iterations = 3,
                animation = tween(durationMillis = 70, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            finishedListener = { onAnimationFinish.invoke() },
            label = ""
        )

        this.then(
            Modifier.graphicsLayer {
                translationX = if (enabled) distance else 0f
            }
        )
    },
    inspectorInfo = debugInspectorInfo {
        name = "shake"
        properties["enabled"] = enabled
    }
)

@MyDevices
@Composable
private fun SettingsPurchaseComponentPreview() = AppThemeSurface {
    SettingsPurchaseComponent(onPurchaseClick = {})
}
