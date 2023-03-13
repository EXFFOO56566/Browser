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

import android.app.AlertDialog
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.CheckBox
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.tochy.tochybrowser.legacy.R
import com.tochy.tochybrowser.legacy.action.SingleAction
import com.tochy.tochybrowser.legacy.action.view.ActionActivity
import com.tochy.tochybrowser.ui.app.StartActivityInfo
import java.io.IOException

class ShowSearchBoxAction : SingleAction, Parcelable {

    var isOpenNewTab: Boolean = false
        private set
    var isReverse: Boolean = false
        private set

    @Throws(IOException::class)
    constructor(id: Int, reader: JsonReader?) : super(id) {
        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_OPEN_NEW_TAB -> {
                        if (reader.peek() != JsonReader.Token.BOOLEAN) return
                        isOpenNewTab = reader.nextBoolean()
                    }
                    FIELD_REVERSE -> {
                        if (reader.peek() != JsonReader.Token.BOOLEAN) return
                        isReverse = reader.nextBoolean()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        }
    }

    @Throws(IOException::class)
    override fun writeIdAndData(writer: JsonWriter) {
        writer.value(id)
        writer.beginObject()
        writer.name(FIELD_OPEN_NEW_TAB)
        writer.value(isOpenNewTab)
        writer.name(FIELD_REVERSE)
        writer.value(isReverse)
        writer.endObject()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(if (isOpenNewTab) 1 else 0)
        dest.writeInt(if (isReverse) 1 else 0)
    }

    private constructor(source: Parcel) : super(source) {
        isOpenNewTab = source.readInt() != 0
        isReverse = source.readInt() != 0
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_show_search_box, null)
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val bottom = view.findViewById<CheckBox>(R.id.checkBox2)
        checkBox.isChecked = isOpenNewTab
        bottom.isChecked = isReverse
        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    isOpenNewTab = checkBox.isChecked
                    isReverse = bottom.isChecked
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        return null
    }

    companion object {
        private const val FIELD_OPEN_NEW_TAB = "0"
        private const val FIELD_REVERSE = "1"

        @JvmField
        val CREATOR: Parcelable.Creator<ShowSearchBoxAction> = object : Parcelable.Creator<ShowSearchBoxAction> {
            override fun createFromParcel(source: Parcel): ShowSearchBoxAction {
                return ShowSearchBoxAction(source)
            }

            override fun newArray(size: Int): Array<ShowSearchBoxAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
