package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.uimodels.UIFeedItem


class FeedAdapter(
    var context: Context,
    list: ArrayList<UIFeedItem>,
    private val itemClickListener: OnItemClickListener,
    private val hashtagClickListener: OnHashtagClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<UIFeedItem> = ArrayList()

    private fun onTagClicked(clickedTag: String) {
        hashtagClickListener.onHashtagClicked(clickedTag)
    }

    init {
        this.list = list
        inflator = LayoutInflater.from(context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FeedItemViewHolder(inflator, parent)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        val feedItemViewHolder : FeedItemViewHolder = holder as FeedItemViewHolder
        feedItemViewHolder.bind(currentItem)
    }


    interface OnItemClickListener {
        fun onItemClicked(item : UIFeedItem)
    }

    interface OnLikeClickListener {
        fun onLikeClicked(item : UIFeedItem)
    }

    interface OnCommentClickListener {
        fun onCommentClicked(item : UIFeedItem)
    }

    interface OnRecastClickListener {
        fun onRecastClicked(item : UIFeedItem)
    }

    interface OnHashtagClickListener {
        fun onHashtagClicked(hashtag : String)
    }
}