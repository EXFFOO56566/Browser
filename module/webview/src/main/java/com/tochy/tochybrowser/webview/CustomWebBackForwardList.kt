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

package com.tochy.tochybrowser.webview

import android.webkit.WebBackForwardList
import java.util.*

class CustomWebBackForwardList(val current: Int, capacity: Int) : ArrayList<CustomWebHistoryItem>(capacity) {

    val next: CustomWebHistoryItem?
        get() = getBackOrForward(1)

    val prev: CustomWebHistoryItem?
        get() = getBackOrForward(-1)

    constructor(list: WebBackForwardList) : this(list.currentIndex, list.size) {
        for (i in 0 until list.size) {
            add(CustomWebHistoryItem(list.getItemAtIndex(i)))
        }
    }

    fun getBackOrForward(dist: Int): CustomWebHistoryItem? {
        val next = current + dist
        return if (next in 0..(size - 1))
            get(next)
        else
            null
    }

    companion object {
        private const val serialVersionUID = 6556040641241028726L
    }
}
