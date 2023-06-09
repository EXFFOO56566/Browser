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

package com.tochy.tochybrowser.legacy.action.item

import android.os.Environment
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.squareup.moshi.JsonReader
import com.tochy.tochybrowser.legacy.pickValue
import okio.buffer
import okio.source
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.io.ByteArrayInputStream
import java.io.File

@RunWith(PowerMockRunner::class)
@PrepareForTest(Environment::class)
class SaveScreenshotSingleActionTest {
    @Before
    fun setUp() {
        PowerMockito.mockStatic(Environment::class.java)
    }

    @Test
    fun testDecode() {
        PowerMockito.`when`(Environment.getExternalStorageDirectory()).thenReturn(File(""))
        val fiftyJson = """{"0":2,"1":"file"}"""


        val reader = JsonReader.of(ByteArrayInputStream(fiftyJson.toByteArray()).source().buffer())
        val action = SaveScreenshotSingleAction(0, reader)

        assertThat(action.pickValue<Int>("mSsType")).isEqualTo(2)
        assertThat(action.folder.path).isEqualTo("file")
        assertThat(reader.peek()).isEqualTo(JsonReader.Token.END_DOCUMENT)
    }
}
