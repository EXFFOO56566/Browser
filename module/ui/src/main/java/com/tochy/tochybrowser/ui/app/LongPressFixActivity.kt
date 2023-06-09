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

package com.tochy.tochybrowser.ui.app

import android.annotation.SuppressLint
import android.os.Handler
import android.view.KeyEvent
import android.view.ViewConfiguration

@SuppressLint("Registered")
open class LongPressFixActivity : ThemeActivity() {
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout()
    private val handler = Handler()

    private var time: Long = 0
    private var waiting: Boolean = false

    private val longPress = Runnable { onBackKeyLongPressed() }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> if (!waiting) {
                    waiting = true
                    time = System.currentTimeMillis()
                    handler.postDelayed(longPress, longPressTimeout.toLong())
                }
                KeyEvent.ACTION_UP -> {
                    handler.removeCallbacks(longPress)
                    waiting = false
                    if (System.currentTimeMillis() - time < longPressTimeout) {
                        onBackKeyPressed()
                    }
                    return true
                }
                else -> {
                    handler.removeCallbacks(longPress)
                    waiting = false
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    fun onDestroyActionMode() {
        handler.removeCallbacks(longPress)
    }

    override fun onBackPressed() {}

    open fun onBackKeyPressed() {
        super.onBackPressed()
    }

    open fun onBackKeyLongPressed() {

    }
}
