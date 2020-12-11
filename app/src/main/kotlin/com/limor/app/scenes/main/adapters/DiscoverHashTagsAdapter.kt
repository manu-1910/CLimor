package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.uimodels.UITags
import org.jetbrains.anko.sdk23.listeners.onClick

class DiscoverHashTagsAdapter(
    var context: Context,
    list: ArrayList<UITags>,
    private val hashtagClickListener: OnHashTagSearchClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<UITags> = ArrayList()

    init {
        this.list = list
        inflator = LayoutInflater.from(context)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DiscoverHashTagsViewHolder(inflator, parent)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        val discoverHashTagsViewHolder: DiscoverHashTagsViewHolder =
            holder as DiscoverHashTagsViewHolder


        discoverHashTagsViewHolder.bind(currentItem.text)

        discoverHashTagsViewHolder.itemView.onClick {
            hashtagClickListener.onHashTagClicked(
                currentItem,
                position
            )
        }
    }

    interface OnHashTagSearchClicked {
        fun onHashTagClicked(item: UITags, position: Int)
    }
}