/*
 * Copyright (C) 2017 Hazuki
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

package com.tochy.tochybrowser.legacy.webkit.handler

import com.tochy.tochybrowser.legacy.browser.BrowserController
import com.tochy.tochybrowser.legacy.webkit.TabType
import java.lang.ref.WeakReference

class WebSrcImageOpenBackgroundHandler(controller: BrowserController) : WebSrcImageHandler() {
    private val mReference: WeakReference<BrowserController> = WeakReference(controller)

    override fun handleUrl(url: String) {
        mReference.get()?.openInBackground(url, TabType.WINDOW)
    }
}
