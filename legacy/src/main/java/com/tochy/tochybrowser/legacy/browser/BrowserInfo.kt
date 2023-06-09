/*
 * Copyright (C) 2017 Hazuki
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

package com.tochy.tochybrowser.legacy.browser

import android.content.Context
import android.content.res.Resources
import com.tochy.tochybrowser.legacy.tab.manager.MainTabData

interface BrowserInfo {
    val resourcesByInfo: Resources
    val themeByInfo: Resources.Theme
    val currentTabData: MainTabData?
    val applicationContextInfo: Context
    val tabSize: Int
    val isImeShown: Boolean
    val isActivityPaused: Boolean
    val isPrivateMode: Boolean

    val isEnableUserScript: Boolean
    val isEnableGesture: Boolean
    val isEnableQuickControl: Boolean
    val isEnableMultiFingerGesture: Boolean
    val isEnableAdBlock: Boolean
}
