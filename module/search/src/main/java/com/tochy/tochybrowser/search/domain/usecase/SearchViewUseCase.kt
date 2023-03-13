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

package com.tochy.tochybrowser.search.domain.usecase

import com.tochy.tochybrowser.bookmark.repository.BookmarkManager
import com.tochy.tochybrowser.history.repository.BrowserHistoryManager
import com.tochy.tochybrowser.search.domain.ISearchUrlRepository
import com.tochy.tochybrowser.search.domain.ISuggestRepository
import com.tochy.tochybrowser.search.model.SearchSuggestModel
import com.tochy.tochybrowser.search.model.provider.SearchSuggestProviders
import com.tochy.tochybrowser.ui.utils.isUrl
import java.util.*

internal class SearchViewUseCase(
    private val bookmarkManager: BookmarkManager,
    private val historyManager: BrowserHistoryManager,
    private val suggestRepository: ISuggestRepository,
    private val searchUrlRepository: ISearchUrlRepository
) {
    var suggestType = 0

    fun getSearchQuery(query: String): List<SearchSuggestModel> {
        return suggestRepository.getSearchQuery(suggestType, query)
    }

    fun getHistoryQuery(query: String): List<SearchSuggestModel> {
        if (query.isEmpty()) {
            return listOf()
        }

        val histories = historyManager.search(query, 0, 5)
        val list = ArrayList<SearchSuggestModel>(histories.size)
        histories.forEach {
            list.add(SearchSuggestModel.HistoryModel(it.title ?: "", it.url ?: ""))
        }
        return list
    }

    fun getBookmarkQuery(query: String): List<SearchSuggestModel> {
        if (query.isEmpty()) {
            return listOf()
        }

        val bookmarks = bookmarkManager.search(query)
        val list = ArrayList<SearchSuggestModel>(if (bookmarks.size >= 5) 5 else bookmarks.size)
        bookmarks.asSequence().take(5).forEach {
            list.add(SearchSuggestModel.HistoryModel(it.title!!, it.url))
        }
        return list
    }

    fun saveQuery(query: String) {
        if (query.isNotEmpty() && !query.isUrl() && suggestType != SUGGEST_TYPE_NONE) {
            suggestRepository.insert(suggestType, query)
        }
    }

    fun deleteQuery(query: String) {
        suggestRepository.delete(suggestType, query)
    }

    fun loadSuggestProviders(): SearchSuggestProviders {
        return SearchSuggestProviders(searchUrlRepository.load())
    }

    fun saveSuggestProviders(providers: SearchSuggestProviders) {
        searchUrlRepository.save(providers.toSettings())
    }

    companion object {
        private const val SUGGEST_TYPE_NONE = 3
    }
}
