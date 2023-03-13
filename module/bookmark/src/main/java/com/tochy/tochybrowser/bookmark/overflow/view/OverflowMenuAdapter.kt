

package com.tochy.tochybrowser.bookmark.overflow.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tochy.bookmark.databinding.BookmarkOverlowMenuItemBinding
import com.tochy.tochybrowser.bookmark.overflow.model.OverflowMenuModel

class OverflowMenuAdapter : RecyclerView.Adapter<OverflowMenuAdapter.OverflowMenuHolder>() {

    val list: MutableList<OverflowMenuModel> = mutableListOf()
    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: OverflowMenuHolder, position: Int) {
        holder.binding.model = list[position]
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverflowMenuHolder {
        val inflater = LayoutInflater.from(parent.context)
        return OverflowMenuHolder(BookmarkOverlowMenuItemBinding.inflate(inflater, parent, false))
    }


    class OverflowMenuHolder(var binding: BookmarkOverlowMenuItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                binding.model?.itemClick()
            }
        }
    }
}
