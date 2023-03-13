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

package com.tochy.tochybrowser.adblock.filter.unified

import android.net.Uri
import java.util.regex.Pattern

class RegexHostFilter(
    filter: String,
    contentType: Int,
    ignoreCase: Boolean,
    domains: DomainMap?,
    thirdParty: Int
) : UnifiedFilter(filter, contentType, ignoreCase, domains, thirdParty) {
    private val regex: Pattern = Pattern.compile("//$pattern")

    override val type: Int
        get() = FILTER_TYPE_JVM_REGEX_HOST

    override fun check(url: Uri): Boolean {
        val host = url.host
        return if (host != null)
            regex.matcher(host).find()
        else
            regex.matcher(url.toString()).find()
    }
}
