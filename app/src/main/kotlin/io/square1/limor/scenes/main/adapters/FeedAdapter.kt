package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.uimodels.UIFeedItem


class FeedAdapter(
    var context: Context,
    list: ArrayList<UIFeedItem>,
    private val showPlayButton: Boolean,
    private val feedClickListener: OnFeedClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<UIFeedItem> = ArrayList()

    private fun onTagClicked(clickedTag: String) {
        feedClickListener.onHashtagClicked(clickedTag)
    }

    init {
        this.list = list
        inflator = LayoutInflater.from(context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FeedItemViewHolder(inflator, parent, feedClickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        val feedItemViewHolder : FeedItemViewHolder = holder as FeedItemViewHolder
        feedItemViewHolder.bind(currentItem, position, showPlayButton)
    }

    interface OnFeedClickListener {
        fun onItemClicked(item : UIFeedItem, position: Int)
        fun onPlayClicked(item : UIFeedItem, position: Int)
        fun onListenClicked(item : UIFeedItem, position: Int)
        fun onCommentClicked(item : UIFeedItem, position: Int)
        fun onLikeClicked(item : UIFeedItem, position: Int)
        fun onRecastClicked(item : UIFeedItem, position: Int)
        fun onHashtagClicked(hashtag : String)
        fun onSendClicked(item : UIFeedItem, position: Int)
        fun onUserClicked(item : UIFeedItem, position: Int)
        fun onMoreClicked(item : UIFeedItem, position: Int)
    }
}