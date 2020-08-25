package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.uimodels.UIComment

class CommentsAdapter(
    var context: Context,
    list: ArrayList<UIComment>,
    private val commentClickListener: OnCommentClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<UIComment> = ArrayList()

    private fun onTagClicked(clickedTag: String) {
        commentClickListener.onHashtagClicked(clickedTag)
    }

    init {
        this.list = list
        inflator = LayoutInflater.from(context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CommentItemViewHolder(inflator, parent, commentClickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        val commentItemViewHolder : CommentItemViewHolder = holder as CommentItemViewHolder
        commentItemViewHolder.bind(currentItem, position)
    }

    interface OnCommentClickListener {
        fun onItemClicked(item : UIComment, position: Int)
        fun onPlayClicked(item : UIComment, position: Int)
        fun onListenClicked(item : UIComment, position: Int)
        fun onCommentClicked(item : UIComment, position: Int)
        fun onLikeClicked(item : UIComment, position: Int)
        fun onRecastClicked(item : UIComment, position: Int)
        fun onHashtagClicked(hashtag : String)
        fun onSendClicked(item : UIComment, position: Int)
        fun onUserClicked(item : UIComment, position: Int)
        fun onMoreClicked(item : UIComment, position: Int)
        fun onReplyClicked(item: UIComment, position: Int)
    }
}