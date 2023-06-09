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
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

import org.mockito.Mockito

class StartEndFilterTest {

    @Test
    fun check() {
        val uri = Mockito.mock(Uri::class.java)
        whenever(uri.schemeSpecificPart).thenReturn("//js.ad-stir.com/js/nativeapi.js")

        assertThat(StartEndFilter("ad-stir.com", 0, false, null, -1).check(uri))
            .isEqualTo(true)

        whenever(uri.schemeSpecificPart).thenReturn("//js.ad-stir.com")
        assertThat(StartEndFilter("ad-stir.com", 0, false, null, -1).check(uri))
            .isEqualTo(true)
    }
}
