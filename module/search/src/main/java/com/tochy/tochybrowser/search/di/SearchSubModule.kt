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

package com.tochy.tochybrowser.search.di

import android.app.Application
import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import com.tochy.tochybrowser.bookmark.repository.BookmarkManager
import com.tochy.tochybrowser.history.repository.BrowserHistoryManager
import com.tochy.tochybrowser.search.domain.ISearchUrlRepository
import com.tochy.tochybrowser.search.domain.ISuggestRepository
import com.tochy.tochybrowser.search.domain.usecase.SearchSettingsViewUseCase
import com.tochy.tochybrowser.search.domain.usecase.SearchViewUseCase
import com.tochy.tochybrowser.search.presentation.search.SearchViewModel
import com.tochy.tochybrowser.search.presentation.settings.SearchSettingsViewModel
import com.tochy.tochybrowser.search.repository.SearchUrlManager
import com.tochy.tochybrowser.search.repository.SuggestRepository
import com.tochy.tochybrowser.ui.provider.ISuggestProvider

@Module
object SearchSubModule {

    @Provides
    @JvmStatic
    fun provideSuggestRepository(application: Application, suggestProvider: ISuggestProvider): ISuggestRepository {
        return SuggestRepository(application, suggestProvider)
    }

    @Provides
    @JvmStatic
    fun provideSearchUrlRepository(context: Context, moshi: Moshi): ISearchUrlRepository {
        return SearchUrlManager(context, moshi)
    }

    @Provides
    @JvmStatic
    internal fun provideSearchViewUseCase(context: Context, suggestRepository: ISuggestRepository, searchUrlRepository: ISearchUrlRepository): SearchViewUseCase {
        return SearchViewUseCase(
            BookmarkManager.getInstance(context),
            BrowserHistoryManager.getInstance(context),
            suggestRepository,
            searchUrlRepository
        )
    }

    @Provides
    @JvmStatic
    internal fun provideSearchViewModelFactory(application: Application, useCase: SearchViewUseCase): SearchViewModel.Factory {
        return SearchViewModel.Factory(application, useCase)
    }

    @Provides
    @JvmStatic
    internal fun provideSearchSettingsViewUseCase(searchUrlRepository: ISearchUrlRepository): SearchSettingsViewUseCase {
        return SearchSettingsViewUseCase(searchUrlRepository)
    }

    @Provides
    @JvmStatic
    internal fun provideSearchSettingsViewModelFactory(application: Application, useCase: SearchSettingsViewUseCase): SearchSettingsViewModel.Factory {
        return SearchSettingsViewModel.Factory(application, useCase)
    }
}
