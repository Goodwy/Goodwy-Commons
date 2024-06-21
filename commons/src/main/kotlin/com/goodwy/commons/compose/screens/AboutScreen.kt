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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.goodwy.commons.R
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.lists.SimpleColumnScaffold
import com.goodwy.commons.compose.settings.SettingsGroup
import com.goodwy.commons.compose.settings.SettingsHorizontalDivider
import com.goodwy.commons.compose.settings.SettingsListItem
import com.goodwy.commons.compose.settings.SettingsTitleTextComponent
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.compose.theme.SimpleTheme

private val startingTitlePadding = Modifier.padding(start = 56.dp)

@Composable
internal fun AboutScreen(
    goBack: () -> Unit,
    //helpUsSection: @Composable () -> Unit,
    aboutSection: @Composable () -> Unit,
    //socialSection: @Composable () -> Unit,
    //otherSection: @Composable () -> Unit,
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
        //helpUsSection()
        //socialSection()
        //otherSection()
        //SettingsListItem(text = stringResource(id = R.string.about_footer))
    }
}

@Composable
internal fun HelpUsSection(
    onRateUsClick: () -> Unit,
    onInviteClick: () -> Unit,
    onContributorsClick: () -> Unit,
    showRateUs: Boolean,
    showInvite: Boolean,
    showDonate: Boolean,
    onDonateClick: () -> Unit,
) {
    SettingsGroup(title = {
        SettingsTitleTextComponent(text = stringResource(id = R.string.help_us), modifier = startingTitlePadding)
    }) {
        if (showRateUs) {
            TwoLinerTextItem(text = stringResource(id = R.string.rate_us), icon = R.drawable.ic_star_vector, click = onRateUsClick)
        }
        if (showInvite) {
            TwoLinerTextItem(text = stringResource(id = R.string.invite_friends), icon = R.drawable.ic_add_person_vector, click = onInviteClick)
        }
        TwoLinerTextItem(
            click = onContributorsClick,
            text = stringResource(id = R.string.contributors),
            icon = R.drawable.ic_person
        )
        if (showDonate) {
            TwoLinerTextItem(
                click = onDonateClick,
                text = stringResource(id = R.string.donate),
                icon = R.drawable.ic_dollar_vector
            )
        }
        SettingsHorizontalDivider()
    }
}

@Composable
internal fun OtherSection(
    showMoreApps: Boolean,
    onMoreAppsClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    showWebsite: Boolean,
    showPrivacyPolicy: Boolean,
    onPrivacyPolicyClick: () -> Unit,
    onLicenseClick: () -> Unit,
    version: String,
    onVersionClick: () -> Unit,
) {
    SettingsGroup(title = {
        SettingsTitleTextComponent(text = stringResource(id = R.string.other), modifier = startingTitlePadding)
    }) {
        if (showMoreApps) {
            TwoLinerTextItem(
                click = onMoreAppsClick,
                text = stringResource(id = R.string.more_apps_from_us),
                icon = R.drawable.ic_heart_vector
            )
        }
        if (showWebsite) {
            TwoLinerTextItem(
                click = onWebsiteClick,
                text = stringResource(id = R.string.website),
                icon = R.drawable.ic_link_vector
            )
        }
        if (showPrivacyPolicy) {
            TwoLinerTextItem(
                click = onPrivacyPolicyClick,
                text = stringResource(id = R.string.privacy_policy),
                icon = R.drawable.ic_unhide_vector
            )
        }
        TwoLinerTextItem(
            click = onLicenseClick,
            text = stringResource(id = R.string.third_party_licences),
            icon = R.drawable.ic_article_vector
        )
        TwoLinerTextItem(
            click = onVersionClick,
            text = version,
            icon = R.drawable.ic_info_vector
        )
        SettingsHorizontalDivider()
    }
}

@Composable
internal fun AboutSection(
    setupFAQ: Boolean,
    onFAQClick: () -> Unit,
    onEmailClick: () -> Unit,
) {
    SettingsGroup(title = {
        SettingsTitleTextComponent(text = stringResource(id = R.string.support), modifier = startingTitlePadding)
    }) {
        if (setupFAQ) {
            TwoLinerTextItem(
                click = onFAQClick,
                text = stringResource(id = R.string.frequently_asked_questions),
                icon = R.drawable.ic_question_mark_vector
            )
        }
        TwoLinerTextItem(
            click = onEmailClick,
            text = stringResource(id = R.string.my_email),
            icon = R.drawable.ic_mail_vector
        )
        SettingsHorizontalDivider()
    }
}

@Composable
internal fun SocialSection(
    onGithubClick: () -> Unit,
    onRedditClick: () -> Unit,
    onTelegramClick: () -> Unit
) {
    SettingsGroup(title = {
        SettingsTitleTextComponent(text = stringResource(id = R.string.social), modifier = startingTitlePadding)
    }) {
        SocialText(
            click = onGithubClick,
            text = stringResource(id = R.string.github),
            icon = R.drawable.ic_github_vector,
            tint = SimpleTheme.colorScheme.onSurface
        )
        SocialText(
            click = onRedditClick,
            text = stringResource(id = R.string.reddit),
            icon = R.drawable.ic_reddit_vector,
        )
        SocialText(
            click = onTelegramClick,
            text = stringResource(id = R.string.telegram),
            icon = R.drawable.ic_telegram_vector,
        )
        SettingsHorizontalDivider()
    }
}

@Composable
internal fun SocialText(
    text: String,
    icon: Int,
    tint: Color? = null,
    click: () -> Unit,
) {
    SettingsListItem(
        click = click,
        text = text,
        icon = icon,
        isImage = true,
        tint = tint,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
internal fun TwoLinerTextItem(text: String, icon: Int, click: () -> Unit) {
    SettingsListItem(
        tint = SimpleTheme.colorScheme.onSurface,
        click = click,
        text = text,
        icon = icon,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@MyDevices
@Composable
private fun AboutScreenPreview() {
    AppThemeSurface {
        AboutScreen(
            goBack = {},
//            helpUsSection = {
////                HelpUsSection(
////                    onRateUsClick = {},
////                    onInviteClick = {},
////                    onContributorsClick = {},
////                    showRateUs = true,
////                    showInvite = true,
////                    showDonate = true,
////                    onDonateClick = {}
////                )
//            },
            aboutSection = {
                //AboutSection(setupFAQ = true, onFAQClick = {}, onEmailClick = {})
                AboutNewSection(
                    setupFAQ = true,
                    appName = "Common",
                    appVersion = "1.0",
                    onRateUsClick = {},
                    onMoreAppsClick = {},
                    onPrivacyPolicyClick = {},
                    onFAQClick = {},
                    onTipJarClick = {}
                )
            },
//            socialSection = {
////                SocialSection(
////                    onGithubClick = {},
////                    onRedditClick = {},
////                    onTelegramClick = {}
////                )
//            }
            isTopAppBarColorIcon = true,
            isTopAppBarColorTitle = true,
        ) //{
//            OtherSection(
//                showMoreApps = true,
//                onMoreAppsClick = {},
//                onWebsiteClick = {},
//                showWebsite = true,
//                showPrivacyPolicy = true,
//                onPrivacyPolicyClick = {},
//                onLicenseClick = {},
//                version = "5.0.4",
//                onVersionClick = {}
//            )
//        }
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
    onTipJarClick: () -> Unit
) {
    Box(
        //Modifier.verticalScroll(rememberScrollState())
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.about_margin))
    ) {
        val textColor = MaterialTheme.colorScheme.onSurface
        Column(Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 26.dp)) {
            ListItem(
                modifier = Modifier
//                    .clickable {
//                        openAbout()
//                    }
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
            HtmlText(stringResource(R.string.about_summary), textColor = textColor)
//            Text(modifier = Modifier.padding(horizontal = 4.dp),
//                text = stringResource(R.string.plus_summary),
//                fontSize = 14.sp,
//                lineHeight = 20.sp,
//                color = textColor,)
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
                        Icon(modifier = Modifier
                            .alpha(0.2f)
                            .size(42.dp), imageVector = Icons.Rounded.Circle, contentDescription = stringResource(id = R.string.rate_us), tint = textColor)
                        Icon(modifier = Modifier
                            .size(42.dp)
                            .padding(8.dp), imageVector = Icons.Rounded.Star, contentDescription = stringResource(id = R.string.rate_us), tint = textColor)
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
                        Icon(modifier = Modifier
                            .alpha(0.2f)
                            .size(42.dp), imageVector = Icons.Rounded.Circle, contentDescription = stringResource(id = R.string.more_apps_from_us), tint = textColor)
                        Icon(modifier = Modifier
                            .size(42.dp)
                            .padding(start = 10.dp, end = 10.dp, top = 9.dp, bottom = 11.dp), imageVector = Icons.Rounded.Shop, contentDescription = stringResource(id = R.string.more_apps_from_us), tint = textColor)
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
                        Icon(modifier = Modifier
                            .alpha(0.2f)
                            .size(42.dp), imageVector = Icons.Rounded.Circle, contentDescription = stringResource(id = R.string.privacy_policy), tint = textColor)
                        Icon(modifier = Modifier
                            .size(42.dp)
                            .padding(8.dp), imageVector = Icons.Rounded.Policy, contentDescription = stringResource(id = R.string.privacy_policy), tint = textColor)
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
                                modifier = Modifier
                                    .alpha(0.2f)
                                    .size(42.dp),
                                imageVector = Icons.Rounded.Circle,
                                contentDescription = stringResource(id = R.string.frequently_asked_questions),
                                tint = textColor
                            )
                            Icon(
                                modifier = Modifier
                                    .size(42.dp)
                                    .padding(8.dp),
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
                        text = stringResource(R.string.tip_jar).toUpperCase(LocaleList.current),
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = textColor,)
                    Box (modifier = Modifier
                        .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                        .width(42.dp)) {
                        Icon(modifier = Modifier
                            .alpha(0.2f)
                            .size(42.dp), imageVector = Icons.Rounded.Circle, contentDescription = stringResource(id = R.string.tip_jar), tint = textColor)
                        Icon(modifier = Modifier
                            .size(42.dp)
                            .padding(8.dp), imageVector = Icons.Rounded.Savings, contentDescription = stringResource(id = R.string.tip_jar), tint = textColor)
                    }
                }
            }
            Spacer(modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier, textColor: Color = Color.Unspecified,) {
    AndroidView(
        modifier = modifier.padding(horizontal = 4.dp),
        factory = { context -> TextView(context).apply {
            setTextColor(textColor.toArgb())
        } },
        update = { it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT) }
    )
}
