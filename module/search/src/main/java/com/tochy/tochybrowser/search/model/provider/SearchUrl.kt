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

package com.tochy.tochybrowser.search.model.provider

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import java.io.Serializable

@JsonSerializable
data class SearchUrl constructor(
    @Json(name = "3") var id: Int,
    @Json(name = "0") var title: String,
    @Json(name = "1") var url: String,
    @Json(name = "2") var color: Int,
    @Json(name = "4") var isUseFavicon: Boolean = true
) : Serializable {

    constructor(title: String, url: String, color: Int) : this(-1, title, url, color)
}
