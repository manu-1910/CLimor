package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.graphics.Point
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.uimodels.UIPodcast
import org.jetbrains.anko.windowManager

class FeaturedItemAdapter(
    var context: Context,
    list: ArrayList<UIPodcast>,
    private val featuredClickListener: OnFeaturedClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<UIPodcast> = ArrayList()
    private val widthFactor = 0.824
    var itemWith = 0

    init {
        this.list = list
        inflator = LayoutInflater.from(context)
        val display = context.windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        itemWith = (point.x * widthFactor).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FeaturedItemViewHolder(inflator, parent, featuredClickListener, context, itemWith)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        val featuredItemViewHolder: FeaturedItemViewHolder =
            holder as FeaturedItemViewHolder
        featuredItemViewHolder.bind(currentItem, position)

    }

    interface OnFeaturedClicked {
        fun onFeaturedItemClicked(item: UIPodcast, position: Int)
        fun onPlayClicked(item: UIPodcast, position: Int)
        fun onMoreClicked(item: UIPodcast, position: Int)
    }
}