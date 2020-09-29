package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.uimodels.UITags

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
        return DiscoverHashTagsViewHolder(inflator, parent, hashtagClickListener, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        val discoverHashTagsViewHolder: DiscoverHashTagsViewHolder =
            holder as DiscoverHashTagsViewHolder
        discoverHashTagsViewHolder.bind(currentItem, position)

    }

    interface OnHashTagSearchClicked {
        fun onHashTagClicked(item: UITags, position: Int)
    }
}