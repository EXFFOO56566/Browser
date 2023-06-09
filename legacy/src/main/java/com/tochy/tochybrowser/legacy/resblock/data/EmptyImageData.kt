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

package com.tochy.tochybrowser.legacy.resblock.data

import android.content.Context
import android.webkit.WebResourceResponse
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.tochy.tochybrowser.core.utility.log.ErrorReport
import com.tochy.tochybrowser.core.utility.utils.IOUtils
import com.tochy.tochybrowser.legacy.resblock.ResourceData
import java.io.ByteArrayInputStream
import java.io.IOException

class EmptyImageData : ResourceData {

    constructor()

    @Throws(IOException::class)
    constructor(reader: JsonReader) {
        //if(parser.nextToken() == JsonToken.VALUE_NULL) return;
    }

    override fun getTypeId(): Int {
        return EMPTY_IMAGE_DATA
    }

    override fun getTitle(context: Context): String? {
        return null
    }

    override fun getResource(context: Context): WebResourceResponse {
        if (sData == null)
            try {
                sData = IOUtils.readByte(context.resources.assets.open("blank.png"))
            } catch (e: IOException) {
                ErrorReport.printAndWriteLog(e)
            }

        return WebResourceResponse("image/png", null, ByteArrayInputStream(sData))
    }

    @Throws(IOException::class)
    override fun write(writer: JsonWriter): Boolean {
        writer.value(EMPTY_IMAGE_DATA)
        return true
    }

    companion object {
        private var sData: ByteArray? = null
    }
}
