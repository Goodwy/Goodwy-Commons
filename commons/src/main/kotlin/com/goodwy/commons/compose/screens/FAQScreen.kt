package com.goodwy.commons.compose.screens

import android.text.Spanned
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goodwy.commons.R
import com.goodwy.commons.compose.components.LinkifyTextComponent
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.lists.SimpleLazyListScaffold
import com.goodwy.commons.compose.settings.SettingsGroup
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.compose.theme.SimpleTheme
import com.goodwy.commons.extensions.fromHtml
import com.goodwy.commons.models.FAQItem
import com.goodwy.strings.R as stringsR
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun FAQScreen(
    goBack: () -> Unit,
    faqItems: ImmutableList<FAQItem>,
    isTopAppBarColorIcon: Boolean,
    isTopAppBarColorTitle: Boolean,
    onCopy: (String) -> Unit,
) {
    SimpleLazyListScaffold(
        title = stringResource(id = R.string.frequently_asked_questions),
        goBack = goBack,
        contentPadding = PaddingValues(bottom = SimpleTheme.dimens.padding.medium),
        isTopAppBarColorIcon = isTopAppBarColorIcon,
        isTopAppBarColorTitle = isTopAppBarColorTitle,
    ) {
        itemsIndexed(faqItems) { index, faqItem ->
            val context = LocalContext.current
            val title = if (faqItem.title is Int) stringResource(faqItem.title) else faqItem.title as String
            val text = if (faqItem.value != null && faqItem.text is Int) {
                val value = if (faqItem.value is Int) context.resources.getString(faqItem.value) else faqItem.value as String
                stringResource(id = faqItem.text, value).fromHtml()
            } else if (faqItem.text is Int) {
                stringResource(id = faqItem.text).fromHtml()
            } else faqItem.text
            SettingsGroup {
                ListItem(
                    headlineContent = {
                        Text(
                            text = title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 8.dp),
                            color = SimpleTheme.colorScheme.primary,
                            lineHeight = 16.sp,
                        )
                    },
                    supportingContent = {
                        if (faqItem.value != null && faqItem.text is Int) {
                            LinkifyTextComponent(
                                modifier = Modifier.padding(bottom = 6.dp),
                                text = { text as Spanned },
                                fontSize = 14.sp
                            )
                        } else if (faqItem.text is Int) {
                            LinkifyTextComponent(
                                modifier = Modifier.padding(bottom = 6.dp),
                                text = { text as Spanned },
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                modifier = Modifier.padding(bottom = 6.dp),
                                text = faqItem.text as String,
                                fontSize = 14.sp
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        headlineColor = SimpleTheme.colorScheme.primary,
                        supportingColor = SimpleTheme.colorScheme.onSurface)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.padding(bottom = 56.dp))
        }
    }
}

@MyDevices
@Composable
private fun FAQScreenPreview() {
    AppThemeSurface {
        FAQScreen(
            goBack = {},
            faqItems = listOf(
                FAQItem(stringsR.string.app_name_g, R.string.welcome_to_app_name, stringsR.string.app_name_g),
                FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
                FAQItem(R.string.faq_4_title_commons, R.string.faq_4_text_commons),
                FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons),
                FAQItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons)
            ).toImmutableList(),
            isTopAppBarColorIcon = true,
            isTopAppBarColorTitle = true,
            onCopy = {},
        )
    }
}
