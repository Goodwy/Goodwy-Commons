package com.goodwy.commons.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.goodwy.commons.R
import com.goodwy.commons.compose.components.LinkifyTextComponent
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.lists.SimpleLazyListScaffold
import com.goodwy.commons.compose.settings.SettingsGroupTitle
import com.goodwy.commons.compose.settings.SettingsHorizontalDivider
import com.goodwy.commons.compose.settings.SettingsListItem
import com.goodwy.commons.compose.settings.SettingsTitleTextComponent
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.compose.theme.SimpleTheme
import com.goodwy.commons.extensions.fromHtml
import com.goodwy.commons.models.LanguageContributor

private val titleStartPadding = Modifier.padding(start = 40.dp)

@Composable
internal fun ContributorsScreen(
    goBack: () -> Unit,
    contributors: ImmutableList<LanguageContributor>
) {
    SimpleLazyListScaffold(
        title = { scrolledColor ->
            Text(
                text = stringResource(id = R.string.contributors),
                modifier = Modifier.fillMaxWidth(),
                color = scrolledColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        goBack = goBack
    ) {
        item {
            SettingsGroupTitle {
                SettingsTitleTextComponent(
                    text = stringResource(id = R.string.development),
                    modifier = titleStartPadding
                )
            }
        }
        item {
            SettingsListItem(
                text = stringResource(id = R.string.contributors_developers),
                icon = R.drawable.ic_code_vector,
                tint = SimpleTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        }
        item {
            Spacer(modifier = Modifier.padding(vertical = SimpleTheme.dimens.padding.medium))
        }
        item {
            SettingsHorizontalDivider()
        }
        item {
            SettingsGroupTitle {
                SettingsTitleTextComponent(
                    text = stringResource(id = R.string.translation),
                    modifier = titleStartPadding
                )
            }
        }
        items(contributors, key = { it.contributorsId.plus(it.iconId).plus(it.labelId) }) {
            ContributorItem(
                languageContributor = it
            )
        }

        item {
            SettingsListItem(
                icon = R.drawable.ic_heart_vector,
                text = {
                    val source = stringResource(id = com.goodwy.strings.R.string.contributors_label_g)
                    LinkifyTextComponent {
                        source.fromHtml()
                    }
                },
                tint = SimpleTheme.colorScheme.onSurface
            )
        }

        item {
            Spacer(modifier = Modifier.padding(bottom = SimpleTheme.dimens.padding.medium))
        }
    }
}

@Composable
private fun ContributorItem(
    modifier: Modifier = Modifier,
    languageContributor: LanguageContributor
) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(id = languageContributor.labelId),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(modifier)
            )
        },
        leadingContent = {
            val imageSize = Modifier.size(SimpleTheme.dimens.icon.extraSmall)
            Image(
                modifier = imageSize,
                painter = painterResource(id = languageContributor.iconId),
                contentDescription = stringResource(id = languageContributor.contributorsId),
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        supportingContent = {
            Text(
                text = stringResource(id = languageContributor.contributorsId),
                modifier = Modifier
                    .fillMaxWidth(),
                color = SimpleTheme.colorScheme.onSurface
            )
        }
    )
}

@Composable
@MyDevices
private fun ContributorsScreenPreview() {
    AppThemeSurface {
        ContributorsScreen(
            goBack = {},
            contributors = listOf(
                LanguageContributor(
                    iconId = R.drawable.ic_flag_arabic_vector,
                    labelId = R.string.translation_arabic,
                    contributorsId = R.string.translators_arabic
                ),
                LanguageContributor(
                    iconId = R.drawable.ic_flag_azerbaijani_vector,
                    labelId = R.string.translation_azerbaijani,
                    contributorsId = R.string.translators_azerbaijani
                ),
                LanguageContributor(
                    iconId = R.drawable.ic_flag_bengali_vector,
                    labelId = R.string.translation_bengali,
                    contributorsId = R.string.translators_bengali
                ),
                LanguageContributor(
                    iconId = R.drawable.ic_flag_catalan_vector,
                    labelId = R.string.translation_catalan,
                    contributorsId = R.string.translators_catalan
                ),
            ).toImmutableList(),
        )
    }
}
