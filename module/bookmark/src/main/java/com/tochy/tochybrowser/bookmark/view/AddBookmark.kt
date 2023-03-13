

package com.tochy.tochybrowser.bookmark.view

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.tochy.tochybrowser.bookmark.repository.BookmarkManager

fun showAddBookmarkDialog(context: Context, fragmentManager: FragmentManager, title: String?, url: String) {
    if (BookmarkManager.getInstance(context).isBookmarked(url)) {
        AddBookmarkOptionDialog.newInstance(title ?: "", url)
                .show(fragmentManager, "bookmarkOption")
    } else {
        AddBookmarkSiteDialog(context, title ?: "", url).show()
    }
}
