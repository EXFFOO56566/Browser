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

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.tochy.tochybrowser.legacy.R
import com.tochy.tochybrowser.legacy.action.Action
import com.tochy.tochybrowser.legacy.action.SingleAction
import com.tochy.tochybrowser.legacy.action.view.ActionActivity
import com.tochy.tochybrowser.ui.app.StartActivityInfo
import java.io.IOException

class CloseTabSingleAction : SingleAction, Parcelable {
    var defaultAction: Action
        private set

    @Throws(IOException::class)
    constructor(id: Int, reader: JsonReader?) : super(id) {
        defaultAction = Action()
        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_NAME_ACTION -> defaultAction.loadAction(reader)
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        } else {
            defaultAction.add(SingleAction.makeInstance(SingleAction.FINISH))
        }
    }

    constructor() : super(SingleAction.CLOSE_TAB) {

        defaultAction = Action()
    }

    @Throws(IOException::class)
    override fun writeIdAndData(writer: JsonWriter) {
        writer.value(id)
        writer.beginObject()
        writer.name(FIELD_NAME_ACTION)
        defaultAction.writeAction(writer)
        writer.endObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeParcelable(defaultAction, flags)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        defaultAction = source.readParcelable(Action::class.java.classLoader)!!
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        return ActionActivity.Builder(context)
                .setActionNameArray(context.actionNameArray)
                .setDefaultAction(defaultAction)
                .setTitle(R.string.action_action_cant_close_tab)
                .setOnActionActivityResultListener { defaultAction = it }
                .makeStartActivityInfo()
    }

    companion object {
        private const val FIELD_NAME_ACTION = "0"

        @JvmField
        val CREATOR: Parcelable.Creator<CloseTabSingleAction> = object : Parcelable.Creator<CloseTabSingleAction> {
            override fun createFromParcel(source: Parcel): CloseTabSingleAction {
                return CloseTabSingleAction(source)
            }

            override fun newArray(size: Int): Array<CloseTabSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
