package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.uimodels.UITags
import org.jetbrains.anko.sdk23.listeners.onClick


class DiscoverMainTagsAdapter(
    var context: Context,
    list: ArrayList<UITags>,
    private val tagClickListener: OnDiscoverMainTagClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<UITags> = ArrayList()

    init {
        this.list = list
        inflator = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DiscoverMainTagsViewHolder(inflator, parent)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        val headerViewHolder: DiscoverMainTagsViewHolder =
            holder as DiscoverMainTagsViewHolder

        headerViewHolder.bind(currentItem.text)

        headerViewHolder.itemView.onClick {
            tagClickListener.onDiscoverTagClicked(
                currentItem,
                position
            )
        }
    }

    interface OnDiscoverMainTagClicked {
        fun onDiscoverTagClicked(item: UITags, position: Int)
    }
}