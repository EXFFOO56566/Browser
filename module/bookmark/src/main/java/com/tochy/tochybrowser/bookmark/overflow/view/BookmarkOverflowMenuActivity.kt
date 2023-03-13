

package com.tochy.tochybrowser.bookmark.overflow.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.transaction
import com.tochy.bookmark.R
import com.tochy.tochybrowser.bookmark.overflow.HideMenuType
import com.tochy.tochybrowser.bookmark.overflow.MenuType
import com.tochy.tochybrowser.ui.app.ThemeActivity

class BookmarkOverflowMenuActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.transaction {
                replace(R.id.container,
                    BookmarkOverflowMenuFragment.create(intent?.getIntExtra(EXTRA_MENU_TYPE, MenuType.SITE)
                        ?: MenuType.SITE))
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    companion object {
        const val EXTRA_MENU_TYPE = "type"

        fun createIntent(context: Context, @HideMenuType type: Int): Intent {
            return Intent(context, BookmarkOverflowMenuActivity::class.java).apply {
                putExtra(EXTRA_MENU_TYPE, type)
            }
        }
    }
}
