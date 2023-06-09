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

package com.tochy.tochybrowser.legacy.settings.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import com.tochy.tochybrowser.legacy.R;
import com.tochy.tochybrowser.legacy.utils.AppUtils;

public class UiSettingFragment extends YuzuPreferenceFragment {

    @Override
    public void onCreateYuzuPreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_ui_settings);

        findPreference("theme_setting").setOnPreferenceChangeListener((preference, newValue) -> {
            AppUtils.restartApp(getActivity());
            return true;
        });

        findPreference("restart").setOnPreferenceClickListener(preference -> {
            AppUtils.restartApp(getActivity());
            return true;
        });

        findPreference("reader_settings").setOnPreferenceClickListener(preference -> {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new ReaderSettingsFragment())
                    .addToBackStack("reader_settings")
                    .commit();
            return true;
        });
    }
}
