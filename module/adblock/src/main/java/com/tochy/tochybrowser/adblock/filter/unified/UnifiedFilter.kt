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
import com.tochy.tochybrowser.adblock.filter.Filter
import com.tochy.tochybrowser.adblock.filter.SingleFilter

abstract class UnifiedFilter(
    override val pattern: String,
    val contentType: Int,
    val ignoreCase: Boolean,
    val domains: DomainMap?,
    val thirdParty: Int
) : SingleFilter() {
    abstract val type: Int

    internal abstract fun check(url: Uri): Boolean

    override fun find(url: Uri, pageUrl: Uri, contentType: Int, isThirdParty: Boolean): Filter? {
        if ((this.contentType and contentType) != 0 && checkThird(isThirdParty) && checkDomain(pageUrl.host!!.toLowerCase())) {
            if (check(url)) return this
        }
        return null
    }

    private fun checkThird(isThirdParty: Boolean): Boolean {
        if (thirdParty == -1) return true
        return if (isThirdParty) {
            thirdParty == 1
        } else {
            thirdParty == 0
        }
    }

    private fun checkDomain(domain: String): Boolean {
        if (domains == null) return true
        return if (domains.include) {
            domains[domain] == true
        } else {
            domains[domain] != false
        }
    }

    protected fun Char.checkSeparator(): Boolean {
        val it = this.toInt()
        return it in 0..0x24 || it in 0x26..0x2c || it == 0x2f || it in 0x3a..0x40 ||
            it in 0x5b..0x5e || it == 0x60 || it in 0x7b..0x7f
    }

    protected fun String.checkIsDomainInSsp(end: Int): Boolean {
        if (end == 0 || end == 2) return true

        for (i in 2 until end - 1) {
            if (this[i] == '/') return false
        }

        return this[end - 1] == '.'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnifiedFilter

        if (pattern != other.pattern) return false
        if (contentType != other.contentType) return false
        if (ignoreCase != other.ignoreCase) return false
        if (domains != other.domains) return false
        if (thirdParty != other.thirdParty) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pattern.hashCode()
        result = 31 * result + contentType
        result = 31 * result + ignoreCase.hashCode()
        result = 31 * result + (domains?.hashCode() ?: 0)
        result = 31 * result + thirdParty
        result = 31 * result + type
        return result
    }
}
