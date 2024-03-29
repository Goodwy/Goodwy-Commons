package com.goodwy.commons.compose.screens

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.goodwy.commons.R
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.settings.scaffold.SettingsLazyScaffold
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.extensions.removeUnderlines
import com.goodwy.commons.models.BlockedNumber
import com.goodwy.commons.models.FAQItem
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
    SettingsLazyScaffold(
        title = stringResource(id = R.string.frequently_asked_questions),
        goBack = goBack,
        contentPadding = PaddingValues(bottom = 8.dp),
        isTopAppBarColorIcon = isTopAppBarColorIcon,
        isTopAppBarColorTitle = isTopAppBarColorTitle,
    ) {
        itemsIndexed(faqItems) { index, faqItem ->
            val context = LocalContext.current
            val title = if (faqItem.title is Int) stringResource(faqItem.title) else faqItem.title as String
            val text = if (faqItem.value != null && faqItem.text is Int) {
                val value = if (faqItem.value is Int) context.resources.getString(faqItem.value) else faqItem.value as String
                stringFromHTML(stringResource(id = faqItem.text, value))
            } else if (faqItem.text is Int) {
                stringFromHTML(stringResource(id = faqItem.text))
            } else faqItem.text
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                .clickable {onCopy("$title\n\n$text")}
            ) {
                ListItem(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    headlineContent = {
                        Text(
                            text = title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 16.sp,
                        )
                    },
                    supportingContent = {
                        if (faqItem.value != null && faqItem.text is Int) {
                            LinkifyText(
                                text = { text as Spanned },
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 14.sp
                            )
                        } else if (faqItem.text is Int) {
                            LinkifyText(
                                text = { text as Spanned },
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = faqItem.text as String,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 14.sp
                            )
                        }
                    },
                )
            }
        }
    }
}

@Suppress("deprecation")
fun stringFromHTML(source: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(source)
    }
}

@Composable
fun LinkifyText(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    removeUnderlines: Boolean = true,
    textAlignment : Int = TextView.TEXT_ALIGNMENT_TEXT_START,
    text: () -> Spanned
) {
    val context = LocalContext.current
    val customLinkifyTextView = remember {
        TextView(context)
    }
    val textColor = MaterialTheme.colorScheme.onSurface
    val linkTextColor = MaterialTheme.colorScheme.primary
    AndroidView(modifier = modifier, factory = { customLinkifyTextView }) { textView ->
        textView.setTextColor(textColor.toArgb())
        textView.setLinkTextColor(linkTextColor.toArgb())
        textView.text = text()
        textView.textAlignment = textAlignment
        textView.textSize = fontSize.value
        textView.movementMethod = LinkMovementMethod.getInstance()
        if (removeUnderlines) {
            customLinkifyTextView.removeUnderlines()
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
                FAQItem(R.string.app_name_g, R.string.welcome_to_app_name, R.string.app_name_g),
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
