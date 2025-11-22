package com.goodwy.commons.dialogs

import android.app.Activity
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.goodwy.commons.R
import com.goodwy.commons.compose.alert_dialog.*
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.settings.SettingsHorizontalDivider
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.databinding.DialogWhatsNewBinding
import com.goodwy.commons.extensions.getAlertDialogBuilder
import com.goodwy.commons.extensions.getProperBackgroundColor
import com.goodwy.commons.extensions.getProperPrimaryColor
import com.goodwy.commons.extensions.getProperTextColor
import com.goodwy.commons.extensions.getSurfaceColor
import com.goodwy.commons.extensions.isBlackTheme
import com.goodwy.commons.extensions.isDynamicTheme
import com.goodwy.commons.extensions.isSystemInDarkMode
import com.goodwy.commons.extensions.setupDialogStuff
import com.goodwy.commons.models.Release

class WhatsNewDialog(val activity: Activity, val releases: List<Release>) {
    init {
        val view = DialogWhatsNewBinding.inflate(LayoutInflater.from(activity), null, false)
//        view.whatsNewContent.text = getNewReleases()
        // Find the container and hide the original TextView
        val container = view.whatsNewHolder

        val sortedReleases = releases.sortedByDescending { it.id }

        // Add cards to the container before the disclaimer
        val disclaimerIndex = container.indexOfChild(view.whatsNewDisclaimer)
        setupReleaseCards(container, disclaimerIndex, sortedReleases)

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.whats_new, cancelOnTouchOutside = false)
            }
    }

//    private fun getNewReleases(): String {
//        val sb = StringBuilder()
//
//        releases.forEach {
//            val parts = activity.getString(it.textId).split("\n").map(String::trim)
//            parts.forEach {
//                sb.append("- $it\n")
//            }
//        }
//
//        return sb.toString()
//    }

    private fun setupReleaseCards(container: LinearLayout, insertIndex: Int, releases: List<Release>) {
        releases.forEachIndexed { index, release ->
            val cardView = createReleaseCard(release)
            container.addView(cardView, insertIndex + index * 2) // *2 because we add more indents

            // Add spacing between cards (except the last one)
            if (release != releases.last()) {
                val space = createSpace()
                container.addView(space, insertIndex + index * 2 + 1)
            }
        }
    }

    private fun createSpace(): View {
        return View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                activity.resources.getDimensionPixelSize(R.dimen.medium_margin)
            )
        }
    }

    private fun createReleaseCard(release: Release): CardView {
        return CardView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // Add spacing around the card
                setMargins(
                    activity.resources.getDimensionPixelSize(R.dimen.normal_margin),
                    activity.resources.getDimensionPixelSize(R.dimen.small_margin),
                    activity.resources.getDimensionPixelSize(R.dimen.normal_margin),
                    activity.resources.getDimensionPixelSize(R.dimen.small_margin)
                )
            }

            cardElevation = activity.resources.getDimension(R.dimen.zero)
            radius = activity.resources.getDimension(R.dimen.normal_margin)

            val backgroundColor = when {
                activity.isDynamicTheme() && !activity.isSystemInDarkMode() -> activity.getSurfaceColor()
                activity.isBlackTheme() -> activity.getProperBackgroundColor()
                else -> activity.getSurfaceColor()
            }
            setCardBackgroundColor(backgroundColor)

            // Create and configure the internal layout of the card
            val cardLayout = createCardLayout(release)
            addView(cardLayout)
        }
    }

    private fun createCardLayout(release: Release): LinearLayout {
        return LinearLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(
                activity.resources.getDimensionPixelSize(R.dimen.activity_margin),
                activity.resources.getDimensionPixelSize(R.dimen.ten_dpi),
                activity.resources.getDimensionPixelSize(R.dimen.activity_margin),
                activity.resources.getDimensionPixelSize(R.dimen.normal_margin)
            )

            // Add version header
            release.id.let { version ->
                val title = TextView(activity).apply {
                    text = version.toString()
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(activity.getProperPrimaryColor())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = activity.resources.getDimensionPixelSize(R.dimen.small_margin)
                    }
                }
                addView(title)
            }

            // Add content
            val content = createCardContent(release)
            addView(content)
        }
    }

    private fun createCardContent(release: Release): TextView {
        return TextView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            text = formatReleaseText(release)
            setTextColor(activity.getProperTextColor())

            // Creating indented text for list items
            setLineSpacing(
                activity.resources.getDimension(R.dimen.small_margin),
                1f
            )
        }
    }

    private fun formatReleaseText(release: Release): SpannableStringBuilder {
        val sb = SpannableStringBuilder()
        val parts = activity.getString(release.textId).split("\n").map(String::trim)

        parts.forEachIndexed { index, part ->
            if (part.isNotEmpty()) {
                // Add a bullet point with indentation
                sb.append("• $part")

                // Add newline except for the last item
                if (index < parts.size - 1) {
                    sb.append("\n")
                }
            }
        }

        return sb
    }
}

@Composable
fun WhatsNewAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    releases: ImmutableList<Release>
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = {
                alertDialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        containerColor = dialogContainerColor,
        shape = dialogShape,
        tonalElevation = dialogElevation,
        modifier = modifier.dialogBorder(),
        title = {
            Text(
                text = stringResource(id = R.string.whats_new),
                color = dialogTextColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = getNewReleases(releases), color = dialogTextColor)
                SettingsHorizontalDivider()
                Text(
                    text = stringResource(id = R.string.whats_new_disclaimer),
                    color = dialogTextColor.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    )
}

@Composable
private fun getNewReleases(releases: ImmutableList<Release>): String {
    val sb = StringBuilder()

    releases.forEach { release ->
        val parts = stringResource(release.textId).split("\n").map(String::trim)
        parts.forEach {
            sb.append("- $it\n")
        }
    }

    return sb.toString()
}

@MyDevices
@Composable
private fun WhatsNewAlertDialogPreview() {
    AppThemeSurface {
        WhatsNewAlertDialog(
            alertDialogState = rememberAlertDialogState(), releases =
            listOf(
                Release(14, R.string.temporarily_show_excluded),
                Release(3, R.string.temporarily_show_hidden)
            ).toImmutableList()
        )
    }
}
