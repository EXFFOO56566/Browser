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

package com.tochy.tochybrowser.legacy.settings.activity

import android.content.Intent
import android.os.Bundle
import com.tochy.tochybrowser.legacy.R
import com.tochy.tochybrowser.legacy.webencode.WebTextEncodeSettingActivity

class PageSettingFragment : YuzuPreferenceFragment() {

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_page_settings)

        findPreference("web_encode_list").setOnPreferenceClickListener {
            val intent = Intent(activity, WebTextEncodeSettingActivity::class.java)
            startActivity(intent)
            true
        }
    }
}
