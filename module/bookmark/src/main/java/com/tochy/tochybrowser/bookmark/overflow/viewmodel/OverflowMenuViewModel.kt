

package com.tochy.tochybrowser.bookmark.overflow.viewmodel

import android.app.Application
import android.view.MenuItem
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tochy.tochybrowser.bookmark.overflow.model.OverflowMenuModel
import com.tochy.tochybrowser.bookmark.repository.HideMenuRepository
import javax.inject.Inject


class OverflowMenuViewModel @Inject constructor(
    application: Application,
    private val repository: HideMenuRepository
) : AndroidViewModel(application) {

    val menuModels = MutableLiveData<List<OverflowMenuModel>>()

    fun setOverflowMenus(type: Int, menuList: List<MenuItem>) {
        val hides = repository.getHideMenu(type).toHashSet()
        menuModels.value = menuList.map { OverflowMenuModel(it.itemId, !hides.contains(it.itemId), it.title.toString()) }
    }

    fun save(type: Int) {
        menuModels.value?.let { models ->
            repository.saveHideMenu(type, models.filter { !it.enable }.map { it.id })
        }
    }


    class Factory(
        private val application: Application,
        private val repository: HideMenuRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return OverflowMenuViewModel(application, repository) as T
        }
    }
}
