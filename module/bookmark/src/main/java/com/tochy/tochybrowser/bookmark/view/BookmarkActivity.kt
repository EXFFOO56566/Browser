/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tochy.tochybrowser.bookmark.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.fragment.app.transaction
import com.tochy.bookmark.R
import com.tochy.tochybrowser.ui.INTENT_EXTRA_MODE_FULLSCREEN
import com.tochy.tochybrowser.ui.INTENT_EXTRA_MODE_ORIENTATION
import com.tochy.tochybrowser.ui.app.DaggerLongPressFixActivity
import com.tochy.tochybrowser.ui.settings.AppPrefs

class BookmarkActivity : DaggerLongPressFixActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        val intent = intent
        var pickMode = false
        var itemId: Long = -1
        var fullscreen = AppPrefs.fullscreen.get()
        var orientation = AppPrefs.oritentation.get()
        if (intent != null) {
            pickMode = Intent.ACTION_PICK == intent.action
            itemId = intent.getLongExtra("id", -1)

            fullscreen = intent.getBooleanExtra(INTENT_EXTRA_MODE_FULLSCREEN, fullscreen)
            orientation = intent.getIntExtra(INTENT_EXTRA_MODE_ORIENTATION, orientation)
        }

        if (fullscreen)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = orientation

        supportFragmentManager.transaction {
            replace(R.id.container, BookmarkFragment(pickMode, itemId))
        }
    }

    override fun onBackKeyLongPressed() {
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
