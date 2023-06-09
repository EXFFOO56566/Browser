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

package com.tochy.tochybrowser.legacy.useragent

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.tochy.tochybrowser.core.utility.log.ErrorReport
import okio.buffer
import okio.sink
import okio.source
import java.io.IOException
import java.util.*

class UserAgentList : ArrayList<UserAgent>() {

    fun read(context: Context, moshi: Moshi): Boolean {
        clear()

        val file = context.getFileStreamPath(FILENAME)

        if (file == null || !file.exists() || file.isDirectory) return true

        try {
            val userAgents = file.source().buffer().use {
                val type = Types.newParameterizedType(List::class.java, UserAgent::class.java)
                val adapter = moshi.adapter<List<UserAgent>>(type)
                adapter.fromJson(it)
            }
            if (userAgents != null) {
                addAll(userAgents)
                return true
            }
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }

        return false
    }

    fun write(context: Context, moshi: Moshi): Boolean {
        val file = context.getFileStreamPath(FILENAME)

        try {
            file.sink().buffer().use {
                val type = Types.newParameterizedType(List::class.java, UserAgent::class.java)
                val adapter = moshi.adapter<List<UserAgent>>(type)
                adapter.toJson(it, this)
                return true
            }
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }

        return false
    }

    companion object {
        private const val FILENAME = "ualist_1.dat"
    }
}
