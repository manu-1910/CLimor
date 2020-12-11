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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PodcastGridViewHolder(inflator, parent, feedClickListener, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]
        val podcastGridViewHolder: PodcastGridViewHolder = holder as PodcastGridViewHolder
        podcastGridViewHolder.bind(currentItem, position)
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