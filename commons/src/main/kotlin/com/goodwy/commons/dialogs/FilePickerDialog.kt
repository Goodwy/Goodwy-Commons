package com.goodwy.commons.dialogs

import android.os.Environment
import android.os.Parcelable
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.adapters.FilepickerFavoritesAdapter
import com.goodwy.commons.adapters.FilepickerItemsAdapter
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.ensureBackgroundThread
import com.goodwy.commons.models.FileDirItem
import com.goodwy.commons.views.Breadcrumbs
import kotlinx.android.synthetic.main.dialog_filepicker.view.*
import java.io.File
import java.util.*

/**
 * The only filepicker constructor with a couple optional parameters
 *
 * @param activity has to be activity to avoid some Theme.AppCompat issues
 * @param currPath initial path of the dialog, defaults to the external storage
 * @param pickFile toggle used to determine if we are picking a file or a folder
 * @param showHidden toggle for showing hidden items, whose name starts with a dot
 * @param showFAB toggle the displaying of a Floating Action Button for creating new folders
 * @param callback the callback used for returning the selected file/folder
 */
class FilePickerDialog(val activity: BaseSimpleActivity,
                       var currPath: String = Environment.getExternalStorageDirectory().toString(),
                       val pickFile: Boolean = true,
                       var showHidden: Boolean = false,
                       val showFAB: Boolean = false,
                       val canAddShowHiddenButton: Boolean = false,
                       val forceShowRoot: Boolean = false,
                       val showFavoritesButton: Boolean = false,
                       val callback: (pickedPath: String) -> Unit) : Breadcrumbs.BreadcrumbsListener {

    private var mFirstUpdate = true
    private var mPrevPath = ""
    private var mScrollStates = HashMap<String, Parcelable>()
    private val mDateFormat = activity.baseConfig.dateFormat
    private val mTimeFormat = activity.getTimeFormat()

    private lateinit var mDialog: AlertDialog
    private var mDialogView = activity.layoutInflater.inflate(R.layout.dialog_filepicker, null)

    init {
        if (!activity.getDoesFilePathExist(currPath)) {
            currPath = activity.internalStoragePath
        }

        if (!activity.getIsPathDirectory(currPath)) {
            currPath = currPath.getParentPath()
        }

        // do not allow copying files in the recycle bin manually
        if (currPath.startsWith(activity.filesDir.absolutePath)) {
            currPath = activity.internalStoragePath
        }

        mDialogView.filepicker_breadcrumbs.apply {
            listener = this@FilePickerDialog
            updateFontSize(activity.getTextSize())
        }

        tryUpdateItems()
        setupFavorites()

        val builder = AlertDialog.Builder(activity)
            .setNegativeButton(R.string.cancel, null)
            .setOnKeyListener { dialogInterface, i, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                    val breadcrumbs = mDialogView.filepicker_breadcrumbs
                    if (breadcrumbs.childCount > 1) {
                        breadcrumbs.removeBreadcrumb()
                        currPath = breadcrumbs.getLastItem().path.trimEnd('/')
                        tryUpdateItems()
                    } else {
                        mDialog.dismiss()
                    }
                }
                true
            }

        if (!pickFile)
            builder.setPositiveButton(R.string.ok, null)

        if (showFAB) {
            mDialogView.filepicker_fab.apply {
                beVisible()
                setOnClickListener { createNewFolder() }
            }
        }

        val secondaryFabBottomMargin = activity.resources.getDimension(if (showFAB) R.dimen.secondary_fab_bottom_margin else R.dimen.activity_margin).toInt()
        mDialogView.filepicker_fabs_holder.apply {
            (layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = secondaryFabBottomMargin
        }

        mDialogView.filepicker_fab_show_hidden.apply {
            beVisibleIf(!showHidden && canAddShowHiddenButton)
            setOnClickListener {
                activity.handleHiddenFolderPasswordProtection {
                    beGone()
                    showHidden = true
                    tryUpdateItems()
                }
            }
        }

        mDialogView.filepicker_favorites_label.text = "${activity.getString(R.string.favorites)}:"
        mDialogView.filepicker_fab_show_favorites.apply {
            beVisibleIf(showFavoritesButton && context.baseConfig.favorites.isNotEmpty())
            setOnClickListener {
                if (mDialogView.filepicker_favorites_holder.isVisible()) {
                    hideFavorites()
                } else {
                    showFavorites()
                }
            }
        }

        mDialog = builder.create().apply {
            activity.setupDialogStuff(mDialogView, this, getTitle())
        }

        if (!pickFile) {
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                verifyPath()
            }
        }
    }

    private fun getTitle() = if (pickFile) R.string.select_file else R.string.select_folder

    private fun createNewFolder() {
        CreateNewFolderDialog(activity, currPath) {
            callback(it)
            mDialog.dismiss()
        }
    }

    private fun tryUpdateItems() {
        ensureBackgroundThread {
            getItems(currPath) {
                activity.runOnUiThread {
                    updateItems(it as ArrayList<FileDirItem>)
                }
            }
        }
    }

    private fun updateItems(items: ArrayList<FileDirItem>) {
        if (!containsDirectory(items) && !mFirstUpdate && !pickFile && !showFAB) {
            verifyPath()
            return
        }

        val sortedItems = items.sortedWith(compareBy({ !it.isDirectory }, { it.name.toLowerCase() }))
        val adapter = FilepickerItemsAdapter(activity, sortedItems, mDialogView.filepicker_list) {
            if ((it as FileDirItem).isDirectory) {
                activity.handleLockedFolderOpening(it.path) { success ->
                    if (success) {
                        currPath = it.path
                        tryUpdateItems()
                    }
                }
            } else if (pickFile) {
                currPath = it.path
                verifyPath()
            }
        }

        val layoutManager = mDialogView.filepicker_list.layoutManager as LinearLayoutManager
        mScrollStates[mPrevPath.trimEnd('/')] = layoutManager.onSaveInstanceState()!!

        mDialogView.apply {
            filepicker_list.adapter = adapter
            filepicker_breadcrumbs.setBreadcrumb(currPath)
            filepicker_fastscroller.setViews(filepicker_list) {
                filepicker_fastscroller.updateBubbleText(sortedItems.getOrNull(it)?.getBubbleText(context, mDateFormat, mTimeFormat) ?: "")
            }

            filepicker_list.scheduleLayoutAnimation()
            layoutManager.onRestoreInstanceState(mScrollStates[currPath.trimEnd('/')])
            filepicker_list.onGlobalLayout {
                filepicker_fastscroller.setScrollToY(filepicker_list.computeVerticalScrollOffset())
            }
        }

        mFirstUpdate = false
        mPrevPath = currPath
    }

    private fun verifyPath() {
        if (activity.isPathOnOTG(currPath)) {
            val fileDocument = activity.getSomeDocumentFile(currPath) ?: return
            if ((pickFile && fileDocument.isFile) || (!pickFile && fileDocument.isDirectory)) {
                sendSuccess()
            }
        } else {
            val file = File(currPath)
            if ((pickFile && file.isFile) || (!pickFile && file.isDirectory)) {
                sendSuccess()
            }
        }
    }

    private fun sendSuccess() {
        currPath = if (currPath.length == 1) {
            currPath
        } else {
            currPath.trimEnd('/')
        }
        callback(currPath)
        mDialog.dismiss()
    }

    private fun getItems(path: String, callback: (List<FileDirItem>) -> Unit) {
        if (activity.isPathOnOTG(path)) {
            activity.getOTGItems(path, showHidden, false, callback)
        } else {
            val lastModifieds = activity.getFolderLastModifieds(path)
            getRegularItems(path, lastModifieds, callback)
        }
    }

    private fun getRegularItems(path: String, lastModifieds: HashMap<String, Long>, callback: (List<FileDirItem>) -> Unit) {
        val items = ArrayList<FileDirItem>()
        val base = File(path)
        val files = base.listFiles()
        if (files == null) {
            callback(items)
            return
        }

        for (file in files) {
            if (!showHidden && file.name.startsWith('.')) {
                continue
            }

            val curPath = file.absolutePath
            val curName = curPath.getFilenameFromPath()
            val size = file.length()
            var lastModified = lastModifieds.remove(curPath)
            val isDirectory = if (lastModified != null) false else file.isDirectory
            if (lastModified == null) {
                lastModified = 0    // we don't actually need the real lastModified that badly, do not check file.lastModified()
            }

            val children = if (isDirectory) file.getDirectChildrenCount(showHidden) else 0
            items.add(FileDirItem(curPath, curName, isDirectory, children, size, lastModified))
        }
        callback(items)
    }

    private fun containsDirectory(items: List<FileDirItem>) = items.any { it.isDirectory }

    private fun setupFavorites() {
        FilepickerFavoritesAdapter(activity, activity.baseConfig.favorites.toMutableList(), mDialogView.filepicker_favorites_list) {
            currPath = it as String
            verifyPath()
        }.apply {
            mDialogView.filepicker_favorites_list.adapter = this
        }
    }

    private fun showFavorites() {
        mDialogView.apply {
            filepicker_favorites_holder.beVisible()
            filepicker_files_holder.beGone()
            val drawable = activity.resources.getColoredDrawableWithColor(R.drawable.ic_folder_vector, activity.getAdjustedPrimaryColor().getContrastColor())
            filepicker_fab_show_favorites.setImageDrawable(drawable)
        }
    }

    private fun hideFavorites() {
        mDialogView.apply {
            filepicker_favorites_holder.beGone()
            filepicker_files_holder.beVisible()
            val drawable = activity.resources.getColoredDrawableWithColor(R.drawable.ic_star_on_vector, activity.getAdjustedPrimaryColor().getContrastColor())
            filepicker_fab_show_favorites.setImageDrawable(drawable)
        }
    }

    override fun breadcrumbClicked(id: Int) {
        if (id == 0) {
            StoragePickerDialog(activity, currPath, forceShowRoot, true) {
                currPath = it
                tryUpdateItems()
            }
        } else {
            val item = mDialogView.filepicker_breadcrumbs.getChildAt(id).tag as FileDirItem
            if (currPath != item.path.trimEnd('/')) {
                currPath = item.path
                tryUpdateItems()
            }
        }
    }
}
