package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.scenes.main.fragments.podcast.CommentWithParent
import io.square1.limor.uimodels.UIComment
import io.square1.limor.uimodels.UIPodcast

class CommentsAdapter(
    private val context: Context,
    private var list: ArrayList<CommentWithParent>,
    private var podcast: UIPodcast,
    private val commentClickListener: OnCommentClickListener,
    private val podcastMode: Boolean,
    private val mainComment: CommentWithParent?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater = LayoutInflater.from(context)
    private var mainCommentPosition = 0


    init {
        mainCommentPosition = list.indexOf(mainComment)
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

        if(podcastMode) {
            commentItemViewHolder.bindPodcastComment(currentItem, podcast, context, position)
        } else {
            commentItemViewHolder.bindCommentComment(currentItem, podcast, context, position, mainComment!!, mainCommentPosition)
        }
    }

    interface OnCommentClickListener {
        fun onItemClicked(item : CommentWithParent, position: Int)
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
        fun onMoreRepliesClicked(parent: CommentWithParent, position: Int)
        fun onShowLessClicked(parent: CommentWithParent, lastChildPosition: Int)
    }
}