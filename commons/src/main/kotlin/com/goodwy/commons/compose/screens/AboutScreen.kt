package com.goodwy.commons.compose.screens

import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.goodwy.commons.R
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.lists.SimpleColumnScaffold
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.isRuStoreInstalled
import com.goodwy.strings.R as stringsR

@Composable
internal fun AboutScreen(
    goBack: () -> Unit,
    aboutSection: @Composable () -> Unit,
    isTopAppBarColorIcon: Boolean,
    isTopAppBarColorTitle: Boolean,
) {
    SimpleColumnScaffold(
        title = stringResource(id = R.string.about),
        goBack = goBack,
        isTopAppBarColorIcon = isTopAppBarColorIcon,
        isTopAppBarColorTitle = isTopAppBarColorTitle,
    ) {
        aboutSection()
    }
}

@MyDevices
@Composable
private fun AboutScreenPreview() {
    AppThemeSurface {
        AboutScreen(
            goBack = {},
            aboutSection = {
                AboutNewSection(
                    setupFAQ = true,
                    appName = "Common",
                    appVersion = "1.0",
                    onRateUsClick = {},
                    onMoreAppsClick = {},
                    onPrivacyPolicyClick = {},
                    onFAQClick = {},
                    onTipJarClick = {},
                    onGithubClick = {}
                )
            },
            isTopAppBarColorIcon = true,
            isTopAppBarColorTitle = true,
        )
    }
}

@Composable
internal fun AboutNewSection(
    setupFAQ: Boolean = true,
    appName: String,
    appVersion: String,
    onRateUsClick: () -> Unit,
    onMoreAppsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onFAQClick: () -> Unit,
    onTipJarClick: () -> Unit,
    showGithub: Boolean = true,
    onGithubClick: () -> Unit
) {
    Box(
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.about_margin))
    ) {
        val context = LocalContext.current
        val textColor = MaterialTheme.colorScheme.onSurface
        Column(Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 26.dp)) {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth(),
                leadingContent = {
                    Box(
                        modifier = Modifier.width(72.dp).padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Icon(
                            modifier = Modifier
                                .size(68.dp)
                                .shadow(elevation = 10.dp, shape = RoundedCornerShape(34.dp), clip = true),
                            painter = painterResource(id = R.drawable.ic_launcher),
                            contentDescription = null,
                        )
                        Icon(
                            modifier = Modifier
                                .size(72.dp),
                            painter = painterResource(id = R.drawable.ic_launcher),
                            contentDescription = appName,
                            tint = Color.Unspecified
                        )
                    }
                },
                headlineContent = {
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = appName,
                        fontSize = 18.sp,
                    )
                },
                supportingContent = {
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = "Version: $appVersion",
                        color = textColor.copy(alpha = 0.5F),
                    )
                },
            )
            Spacer(modifier = Modifier.size(8.dp))
            HtmlText(stringResource(stringsR.string.about_summary), textColor = textColor)
            Spacer(modifier = Modifier.size(24.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onRateUsClick),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp)
                        .weight(1f),
                        text = stringResource(R.string.rate_us).toUpperCase(LocaleList.current),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = textColor,)
                    Box (modifier = Modifier
                        .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                        .width(42.dp)) {
                        Icon(modifier = Modifier.alpha(0.2f).size(42.dp),
                            imageVector = Icons.Rounded.Circle, contentDescription = stringResource(id = R.string.rate_us), tint = textColor)
                        Icon(modifier = Modifier.size(42.dp).padding(8.dp),
                            imageVector = Icons.Rounded.Star, contentDescription = stringResource(id = R.string.rate_us), tint = textColor)
                    }
                }
            }
            Spacer(modifier = Modifier.size(18.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onMoreAppsClick),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp)
                        .weight(1f),
                        text = stringResource(R.string.more_apps_from_us).toUpperCase(LocaleList.current),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = textColor,)
                    Box (modifier = Modifier
                        .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                        .width(42.dp)) {
                        Icon(modifier = Modifier.alpha(0.2f).size(42.dp),
                            imageVector = Icons.Rounded.Circle, contentDescription = stringResource(id = R.string.more_apps_from_us), tint = textColor)
                        if (context.isRuStoreInstalled() && !context.baseConfig.useGooglePlay) {
                            Icon(modifier = Modifier
                                .size(42.dp)
                                .padding(9.dp),
                                painter = painterResource(id = R.drawable.ic_rustore),
                                contentDescription = stringResource(id = R.string.more_apps_from_us), tint = textColor)
                        }
                        else {
                            Icon(modifier = Modifier.size(42.dp).padding(start = 10.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                                painter = painterResource(id = R.drawable.ic_google_play_vector),
                                contentDescription = stringResource(id = R.string.more_apps_from_us), tint = textColor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(18.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onPrivacyPolicyClick),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp)
                        .weight(1f),
                        text = stringResource(R.string.privacy_policy).toUpperCase(LocaleList.current),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = textColor,)
                    Box (modifier = Modifier
                        .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                        .width(42.dp)) {
                        Icon(modifier = Modifier.alpha(0.2f).size(42.dp),
                            imageVector = Icons.Rounded.Circle, contentDescription = stringResource(id = R.string.privacy_policy), tint = textColor)
                        Icon(modifier = Modifier.size(42.dp).padding(8.dp),
                            imageVector = Icons.Rounded.Policy, contentDescription = stringResource(id = R.string.privacy_policy), tint = textColor)
                    }
                }
            }
            if (setupFAQ) {
                Spacer(modifier = Modifier.size(18.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(onClick = onFAQClick),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 8.dp)
                                .weight(1f),
                            text = stringResource(R.string.frequently_asked_questions).toUpperCase(LocaleList.current),
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            color = textColor,
                        )
                        Box(modifier = Modifier
                            .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                            .width(42.dp)) {
                            Icon(
                                modifier = Modifier.alpha(0.2f).size(42.dp),
                                imageVector = Icons.Rounded.Circle,
                                contentDescription = stringResource(id = R.string.frequently_asked_questions),
                                tint = textColor
                            )
                            Icon(
                                modifier = Modifier.size(42.dp).padding(8.dp),
                                imageVector = Icons.Rounded.QuestionMark,
                                contentDescription = stringResource(id = R.string.frequently_asked_questions),
                                tint = textColor
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(18.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onTipJarClick),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp)
                        .weight(1f),
                        text = stringResource(stringsR.string.tip_jar).toUpperCase(LocaleList.current),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = textColor,)
                    Box (modifier = Modifier
                        .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                        .width(42.dp)) {
                        Icon(modifier = Modifier.alpha(0.2f).size(42.dp),
                            imageVector = Icons.Rounded.Circle, contentDescription = stringResource(id = stringsR.string.tip_jar), tint = textColor)
                        Icon(modifier = Modifier.size(42.dp).padding(8.dp),
                            imageVector = Icons.Rounded.Savings, contentDescription = stringResource(id = stringsR.string.tip_jar), tint = textColor)
                    }
                }
            }
            if (showGithub) {
                Spacer(modifier = Modifier.size(18.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(onClick = onGithubClick),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 8.dp)
                                .weight(1f),
                            text = stringResource(R.string.github).toUpperCase(LocaleList.current),
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            color = textColor,
                        )
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                                .width(42.dp)
                        ) {
                            Icon(
                                modifier = Modifier.alpha(0.2f).size(42.dp),
                                imageVector = Icons.Rounded.Circle, contentDescription = stringResource(id = R.string.privacy_policy), tint = textColor
                            )
                            Icon(
                                modifier = Modifier.size(42.dp).padding(8.dp),
                                painter = painterResource(id = R.drawable.ic_github_vector),
                                contentDescription = stringResource(id = R.string.privacy_policy),
                                tint = textColor
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier, textColor: Color = Color.Unspecified) {
    AndroidView(
        modifier = modifier.padding(horizontal = 4.dp),
        factory = { context -> TextView(context).apply {
            setTextColor(textColor.toArgb())
        } },
        update = { it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT) }
    )
}
