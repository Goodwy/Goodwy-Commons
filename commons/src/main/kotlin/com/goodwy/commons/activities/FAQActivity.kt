package com.goodwy.commons.activities

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import com.goodwy.commons.R
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.models.FAQItem
import kotlinx.android.synthetic.main.activity_faq.*
import kotlinx.android.synthetic.main.item_faq.view.*
import java.util.*

class FAQActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        updateMaterialActivityViews(faq_coordinator, faq_holder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(faq_nested_scrollview, faq_toolbar)
        val textColor = getProperTextColor()
        //val backgroundColor = getProperBackgroundColor()
        val primaryColor = getProperPrimaryColor()
        val bottomNavigationBackgroundColor = getBottomNavigationBackgroundColor()

        val inflater = LayoutInflater.from(this)
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        faqItems.forEach {
            val faqItem = it
            inflater.inflate(R.layout.item_faq, null).apply {
                faq_card.setCardBackgroundColor(bottomNavigationBackgroundColor)
                faq_title.apply {
                    text = if (faqItem.title is Int) getString(faqItem.title) else faqItem.title as String
                    setTextColor(primaryColor)
                }

                faq_text.apply {
                    text = if (faqItem.value != null && faqItem.text is Int) {
                        val value = if (faqItem.value is Int) getString(faqItem.value) else faqItem.value as String
                        String.format(getString(faqItem.text, value))
                    } else {
                        if (faqItem.text is Int) Html.fromHtml(getString(faqItem.text)) else faqItem.text as String
                    }
                    setTextColor(textColor)
                    setLinkTextColor(primaryColor)

                    movementMethod = LinkMovementMethod.getInstance()
                    removeUnderlines()
                }

                faq_holder.addView(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(faq_toolbar, NavigationIcon.Arrow)
    }
}
