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

package com.tochy.tochybrowser.legacy.reader.snacktory

import org.jsoup.nodes.Element

internal object SnacktoryUtils {

    @JvmStatic
    fun hasOtherMainParagraph(parent: Element): Boolean {
        return parent.children().all { "div" == it.tagName() && it.count() > 100 }
    }

    private fun Element.count(): Int {
        return ownText().length + children().asSequence()
                .filter { "p" == it.tagName() }
                .sumBy { it.text().length }
    }
}
