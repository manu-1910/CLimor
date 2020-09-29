package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.uimodels.UIPodcast

class TopCastAdapter(
    var context: Context,
    list: ArrayList<UIPodcast>,
    private val topCastClickListener: OnTopCastClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<UIPodcast> = ArrayList()

    init {
        this.list = list
        inflator = LayoutInflater.from(context)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TopCastViewHolder(inflator, parent, topCastClickListener, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        val topCastViewHolder: TopCastViewHolder =
            holder as TopCastViewHolder
        topCastViewHolder.bind(currentItem, position)

    }

    interface OnTopCastClicked {
        fun onTopCastItemClicked(item: UIPodcast, position: Int)
        fun onTopCastPlayClicked(item: UIPodcast, position: Int)
    }
}