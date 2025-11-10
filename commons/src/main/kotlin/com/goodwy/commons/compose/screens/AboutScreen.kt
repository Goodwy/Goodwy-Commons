package com.goodwy.commons.compose.screens

import android.content.Context
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.goodwy.commons.R
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.extensions.rememberMutableInteractionSource
import com.goodwy.commons.compose.lists.SimpleColumnScaffold
import com.goodwy.commons.compose.menus.ActionIconButton
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.compose.theme.SimpleTheme
import com.goodwy.commons.extensions.toast
import java.util.Calendar
import com.goodwy.strings.R as stringsR

@Composable
internal fun AboutScreen(
    goBack: () -> Unit,
    onInviteClick: () -> Unit,
    onKnownIssuesClick: () -> Unit,
    aboutSection: @Composable () -> Unit,
    isTopAppBarColorIcon: Boolean,
    isTopAppBarColorTitle: Boolean,
) {
    SimpleColumnScaffold(
        title = stringResource(id = R.string.about),
        goBack = goBack,
        actions = {
            val iconColor =
                if (isTopAppBarColorIcon) MaterialTheme.colorScheme.primary
                else LocalContentColor.current
            ActionIconButton(
                onClick = onKnownIssuesClick,
                contentColor = iconColor,
            ) {
                Icon(
                    imageVector = Icons.Rounded.BugReport,
                    contentDescription = stringResource(id = R.string.known_issues)
                )
            }
            ActionIconButton(
                onClick = onInviteClick,
                contentColor = iconColor,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = stringResource(id = R.string.invite_friends)
                )
            }
            Spacer(modifier = Modifier.size(4.dp))
        },
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
                    appFlavor = "foss",
                    onRateUsClick = {},
                    onMoreAppsClick = {},
                    onPrivacyPolicyClick = {},
                    onFAQClick = {},
                    onTipJarClick = {},
                    onGithubClick = {},
                    onPatreonClick = {},
                    onBuyMeaCoffeeClick = {},
                    onWebsiteClick = {},
                    showGithub = true,
                    onLicenseClick = {},
                    onContributorsClick = {},
                    onVersionClick = {},
                )
            },
            onInviteClick = {},
            onKnownIssuesClick = {},
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
    appFlavor: String,
    onRateUsClick: () -> Unit,
    onMoreAppsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onFAQClick: () -> Unit,
    onTipJarClick: () -> Unit,
    onGithubClick: () -> Unit,
    onPatreonClick: () -> Unit,
    onBuyMeaCoffeeClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    showGithub: Boolean = true,
    onLicenseClick: () -> Unit,
    onContributorsClick: () -> Unit,
    onVersionClick: () -> Unit,
) {
    Box(
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.about_margin))
    ) {
        val context = LocalContext.current
        val textColor = MaterialTheme.colorScheme.onSurface
        Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 26.dp)) {
            Card(shape = RoundedCornerShape(16.dp)) {
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onVersionClick),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .width(72.dp)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
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
            }
            Spacer(modifier = Modifier.size(8.dp))
            HtmlText(stringResource(stringsR.string.about_summary), textColor = textColor)
            Spacer(modifier = Modifier.size(24.dp))
            if (appFlavor == "gplay" || appFlavor == "rustore") {
                AboutItem(
                    text = stringResource(stringsR.string.rate_g),
                    imageVector = Icons.Rounded.Star,
                    onClick = onRateUsClick,
                )
                Spacer(modifier = Modifier.size(18.dp))
            }
            AboutItem(
                modifierIcon = when (appFlavor) {
                    "gplay" -> {
                        Modifier
                            .size(42.dp)
                            .padding(start = 10.dp, end = 6.dp, top = 8.dp, bottom = 8.dp)
                    }
                    else -> Modifier.size(42.dp).padding(9.dp)
                },
                text = stringResource(stringsR.string.more_apps_from_us_g),
                painter = painterResource(
                    id = when (appFlavor) {
                        "foss" -> R.drawable.ic_github_vector
                        "rustore" -> R.drawable.ic_rustore
                        else -> R.drawable.ic_google_play_vector
                    }
                ),
                onClick = onMoreAppsClick,
            )
            if (setupFAQ) {
                Spacer(modifier = Modifier.size(18.dp))
                AboutItem(
                    text = stringResource(R.string.frequently_asked_questions),
                    imageVector = Icons.Rounded.QuestionMark,
                    onClick = onFAQClick,
                )
            }
            Spacer(modifier = Modifier.size(18.dp))
            AboutItem(
                cardColor = MaterialTheme.colorScheme.primaryContainer,
                text = stringResource(stringsR.string.tip_jar),
                imageVector = Icons.Rounded.Savings,
                onClick = onTipJarClick,
            )
            Spacer(modifier = Modifier.size(18.dp))
            AboutItem(
                text = stringResource(stringsR.string.participants_title),
                imageVector = Icons.Rounded.Diversity3,
                onClick = onContributorsClick,
            )
            Spacer(modifier = Modifier.size(18.dp))
            AboutItem(
                text = stringResource(R.string.third_party_licences),
                imageVector = Icons.AutoMirrored.Outlined.Article,
                onClick = onLicenseClick,
            )
            Spacer(modifier = Modifier.size(18.dp))
            AboutItem(
                text = stringResource(R.string.privacy_policy),
                imageVector = Icons.Rounded.Policy,
                onClick = onPrivacyPolicyClick,
            )
            Spacer(modifier = Modifier.size(18.dp))
            if (showGithub) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    MyButton(
                        context= context,
                        text = stringResource(R.string.github),
                        painter = painterResource(id = R.drawable.ic_github_vector),
                        onClick = onGithubClick,
                    )
                    Spacer(modifier = Modifier.size(14.dp))
                    MyButton(
                        context= context,
                        text = stringResource(R.string.patreon),
                        painter = painterResource(id = R.drawable.ic_patreon),
                        onClick = onPatreonClick,
                    )
                    Spacer(modifier = Modifier.size(14.dp))
                    MyButton(
                        context= context,
                        text = stringResource(R.string.buymeacoffee),
                        painter = painterResource(id = R.drawable.ic_bmc),
                        onClick = onBuyMeaCoffeeClick,
                    )
                    Spacer(modifier = Modifier.size(14.dp))
                    MyButton(
                        context= context,
                        text = "goodwy.dev",
                        painter = painterResource(id = R.drawable.ic_goodwy),
                        onClick = onWebsiteClick,
                    )
                }
            }
            Spacer(modifier = Modifier.size(20.dp))
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            Text(
                modifier = Modifier.fillMaxSize(),
                text = stringResource(R.string.copyright_new, currentYear),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun MyButton(
    context: Context,
    text: String,
    painter: Painter,
    onClick: () -> Unit,
) {
    val navigationIconInteractionSource = rememberMutableInteractionSource()
    Box(
        modifier = Modifier
            .size(62.dp)
            .padding(SimpleTheme.dimens.padding.small)
            .clip(RoundedCornerShape(50))
            .combinedClickable(
                interactionSource = navigationIconInteractionSource,
                indication = ripple(
                    color = SimpleTheme.colorScheme.onSurface,
                    bounded = true
                ),
                onClick = onClick,
                onLongClick = {
                    context.toast(text)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            painter = painter,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AboutItem(
    modifierIcon: Modifier = Modifier
        .size(42.dp)
        .padding(8.dp),
    cardColor: Color = MaterialTheme.colorScheme.surface,
    text: String,
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        val colorFor = contentColorFor(cardColor)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .weight(1f),
                text = text.toUpperCase(LocaleList.current),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = colorFor,
            )
            Box(
                modifier = Modifier
                    .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                    .width(42.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .alpha(0.2f)
                        .size(42.dp),
                    imageVector = Icons.Rounded.Circle,
                    contentDescription = text,
                    tint = colorFor
                )
                if (imageVector != null) Icon(
                    modifier = Modifier
                        .size(42.dp)
                        .padding(8.dp),
                    imageVector = imageVector,
                    contentDescription = text,
                    tint = colorFor
                )
                if (painter != null) Icon(
                    modifier = modifierIcon,
                    painter = painter,
                    contentDescription = text,
                    tint = colorFor
                )
            }
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
