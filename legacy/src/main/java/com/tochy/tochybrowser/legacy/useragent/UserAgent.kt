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

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import java.io.Serializable

@JsonSerializable
data class UserAgent(
        @Json(name = "1") var name: String,
        @Json(name = "0") var useragent: String
) : Serializable
