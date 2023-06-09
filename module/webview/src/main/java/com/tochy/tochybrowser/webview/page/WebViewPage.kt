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

package com.tochy.tochybrowser.webview.page

import com.tochy.tochybrowser.webview.CustomWebView

internal class WebViewPage(val webView: CustomWebView, val page: Page) : PageInfo by page {

    constructor(webView: CustomWebView) : this(webView, Page(webView.identityId))

    var originalUrl: String? = null
        get() = field ?: url

    fun onPageStarted(url: String) {
        this.url = url
        originalUrl = url
    }

    fun onPageFinished() {
        url = webView.url
        originalUrl = webView.originalUrl
        title = webView.title
    }
}
