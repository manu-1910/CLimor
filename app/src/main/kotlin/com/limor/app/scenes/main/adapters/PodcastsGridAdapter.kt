package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.uimodels.UIPodcast


class PodcastsGridAdapter(
    var context: Context,
    private val list: ArrayList<UIPodcast>,
    private val feedClickListener: OnPodcastClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var inflator: LayoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        return getSpanByPosition(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // small view
        return if (viewType == 1)
            PodcastGridSmallViewHolder(inflator, parent, feedClickListener, context)

        // large view
        else
            PodcastGridLargeViewHolder(inflator, parent, feedClickListener, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]
        if (getSpanByPosition(position) == 1) {
            val podcastSmallViewHolder: PodcastGridSmallViewHolder =
                holder as PodcastGridSmallViewHolder
            podcastSmallViewHolder.bind(currentItem, position)
        } else {
            val podcastGridLargeViewHolder: PodcastGridLargeViewHolder =
                holder as PodcastGridLargeViewHolder
            podcastGridLargeViewHolder.bind(currentItem, position)
        }
    }


    fun getSpanByPosition(position: Int): Int {
        return if (position == 0 || position % 5 == 0) 2
        else 1
    }


    interface OnPodcastClickListener {
        fun onItemClicked(item: UIPodcast, position: Int)
        fun onPlayClicked(item: UIPodcast, position: Int)
        fun onUserClicked(item: UIPodcast, position: Int)
        fun onMoreClicked(
            item: UIPodcast,
            position: Int,
            view: View
        )
    }
}