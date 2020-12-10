package com.limor.app.scenes.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R

class DiscoverMainCategoriesViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.discover_chip_tag_item,
        parent,
        false
    )
) {
    private var tvTitle: TextView = itemView.findViewById(R.id.tv_tag)

    fun bind(currentItem: String) {
        val text = String.format("#%s", currentItem)
        tvTitle.text = text
    }
}