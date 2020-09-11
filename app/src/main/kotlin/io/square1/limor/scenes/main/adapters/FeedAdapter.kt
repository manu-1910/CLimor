package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.common.SessionManager
import io.square1.limor.uimodels.UIFeedItem
import io.square1.limor.uimodels.UIUser


class FeedAdapter(
    var context: Context,
    list: ArrayList<UIFeedItem>,
    private val feedClickListener: OnFeedClickListener,
    private val sessionManager: SessionManager,
    private val showPlayButton: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val userLogged : UIUser?
    private var inflator: LayoutInflater
    var list: ArrayList<UIFeedItem> = ArrayList()

    init {
        this.list = list
        inflator = LayoutInflater.from(context)
        userLogged = sessionManager.getStoredUser()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FeedItemViewHolder(inflator, parent, feedClickListener, userLogged, context)
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
        fun onMoreClicked(
            item: UIFeedItem,
            position: Int,
            view: View
        )
    }
}