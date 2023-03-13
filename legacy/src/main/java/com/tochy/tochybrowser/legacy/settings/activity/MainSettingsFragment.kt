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

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.tochy.tochybrowser.legacy.R

class MainSettingsFragment : YuzuPreferenceFragment() {

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_main)

        findPreference("fragment_browser").setOnPreferenceClickListener {
            openFragment(BrowserSettingsFragment())
            true
        }
        findPreference("fragment_page").setOnPreferenceClickListener {
            openFragment(PageSettingFragment())
            true
        }
        findPreference("fragment_privacy").setOnPreferenceClickListener {
            openFragment(PrivacyFragment())
            true
        }
        findPreference("fragment_ui").setOnPreferenceClickListener {
            openFragment(UiSettingFragment())
            true
        }
        findPreference("fragment_action").setOnPreferenceClickListener {
            openFragment(ActionSettingsFragment())
            true
        }

        findPreference("fragment_application").setOnPreferenceClickListener {
            openFragment(ApplicationSettingsFragment())
            true
        }
        findPreference("fragment_import_export").setOnPreferenceClickListener {
            openFragment(ImportExportFragment())
            true
        }

    }

    private fun openFragment(fragment: PreferenceFragmentCompat) {
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

}
