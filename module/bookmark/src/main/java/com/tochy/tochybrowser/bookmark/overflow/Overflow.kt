

package com.tochy.tochybrowser.bookmark.overflow

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tochy.tochybrowser.bookmark.overflow.model.OverflowMenuModel
import com.tochy.tochybrowser.bookmark.overflow.view.OverflowMenuAdapter

@BindingAdapter("bind:viewmodels")
internal fun RecyclerView.setViewModels(overflowMenuModels: List<OverflowMenuModel>?) {
    if (overflowMenuModels != null) {
        val adapter = adapter as OverflowMenuAdapter
        adapter.list.run {
            clear()
            addAll(overflowMenuModels)
        }
        adapter.notifyDataSetChanged()
    }
}
