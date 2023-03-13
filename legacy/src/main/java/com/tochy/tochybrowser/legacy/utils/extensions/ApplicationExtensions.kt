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

package com.tochy.tochybrowser.legacy.utils.extensions

import android.content.Context
import android.widget.Toast
import com.tochy.tochybrowser.core.utility.extensions.clipboardText
import com.tochy.tochybrowser.legacy.R
import com.tochy.tochybrowser.ui.BrowserApplication

inline val Context.browserApplicationContext: BrowserApplication
    get() = applicationContext as BrowserApplication

fun Context.setClipboardWithToast(text: String?) {
    if (text == null) return

    clipboardText = text
    Toast.makeText(this, getString(R.string.copy_clipboard_mes_before) + text, Toast.LENGTH_SHORT).show()
}
