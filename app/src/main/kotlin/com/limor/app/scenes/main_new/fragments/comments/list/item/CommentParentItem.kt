package com.limor.app.scenes.main_new.fragments.comments.list.item

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.databinding.ItemParentCommentBinding
import com.limor.app.extensions.*
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.utils.DateUiUtil
import com.limor.app.uimodels.CommentUIModel
import com.xwray.groupie.viewbinding.BindableItem
import timber.log.Timber

class CommentParentItem(
    val castOwnerId: Int,
    val comment: CommentUIModel,
    val onReplyClick: (parentComment: CommentUIModel) -> Unit,
    val onThreeDotsClick: (parentComment: CommentUIModel,item: CommentParentItem) -> Unit,
    val onLikeClick: (parentComment: CommentUIModel, liked: Boolean) -> Unit,
    val onUserMentionClick: (username: String, userId: Int) -> Unit,
    val onCommentListen: (commentId: Int) -> Unit,
) : BindableItem<ItemParentCommentBinding>() {

    override fun bind(viewBinding: ItemParentCommentBinding, position: Int) {
        viewBinding.tvCommentName.text = comment.user?.username
        viewBinding.tvCommentDate.text = comment.createdAt?.let { createdAt ->
            DateUiUtil.getTimeAgoText(
                createdAt,
                viewBinding.root.context
            )
        }

        setTextWithTagging(viewBinding.tvCommentContent)

        val onUserClick: (view: View) -> Unit =  {
            onUserClick()
        }
        viewBinding.ivCommentAvatar.throttledClick(onClick = onUserClick)
        viewBinding.tvCommentName.throttledClick(onClick = onUserClick)

        comment.user?.getAvatarUrl()?.let {
            viewBinding.ivCommentAvatar.loadCircleImage(it)
        }
        viewBinding.tvCastCreator.text = if (isOwnerOf(castOwnerId,comment)) "• Cast Creator" else ""

        viewBinding.replyBtn.setOnClickListener {
            onReplyClick(comment)
        }
        viewBinding.btnCommentMore.setOnClickListener {
            onThreeDotsClick(comment,this)
        }
        initLikeState(viewBinding)
        initAudioPlayer(viewBinding)
    }

    private fun onUserClick() {
        val user = comment.user ?: return
        val username = user.username ?: return
        onUserMentionClick(username, user.id)
    }

    private fun setTextWithTagging(tvCommentContent: TextView) {
        val commentContent = comment.content ?: ""
        tvCommentContent.text = commentContent

        if (comment.mentions == null) {
            return
        }

        val color = ContextCompat.getColor(tvCommentContent.context, R.color.primaryYellowColor)
        val spannable = SpannableString(commentContent)

        for (mention in comment.mentions.content) {
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = color
                    ds.isUnderlineText = false
                }

                override fun onClick(widget: View) {
                    onUserMentionClick(mention.username, mention.userId)
                }
            }
            try {
                spannable.setSpan(clickableSpan, mention.startIndex, mention.endIndex, 0)
            } catch (throwable: Throwable) {
                if (BuildConfig.DEBUG) {
                    throwable.printStackTrace()
                }
            }
        }
        tvCommentContent.text = spannable
        tvCommentContent.movementMethod = LinkMovementMethod.getInstance();
    }

    private fun initAudioPlayer(binding: ItemParentCommentBinding) {
        binding.audioPlayer.apply {
            if (comment.audio != null) {
                makeVisible()
                initialize(comment.audio)
            } else {
                makeGone()
            }
        }
    }

    private fun initLikeState(binding: ItemParentCommentBinding) {
        setupListens(binding)

        binding.apply {
            btnCommentLike.isLiked = comment.isLiked!!
            comment.likesCount?.let {
                if (it > 0) {
                    likesCount.visibility = View.VISIBLE
                } else {
                    likesCount.visibility = View.GONE
                }
            }
            likesCount.text = root.context.resources.getQuantityString(
                R.plurals.likes_count,
                comment.likesCount ?: 0,
                comment.likesCount ?: 0
            )

            btnCommentLike.setOnClickListener {
                val isLiked = btnCommentLike.isLiked
                val textLikesCount =
                    likesCount.text.toString().takeWhile { it.isDigit() || it == '-' }.toInt()
                val newLikesCount = if (isLiked) {
                    textLikesCount + 1
                } else {
                    textLikesCount - 1
                }

                if (newLikesCount > 0) {
                    likesCount.visibility = View.VISIBLE
                } else {
                    likesCount.visibility = View.GONE
                }

                likesCount.text = root.context.resources.getQuantityString(
                    R.plurals.likes_count,
                    newLikesCount,
                    newLikesCount
                )

                onLikeClick(comment, isLiked)
            }
        }
    }

    private fun setListensUI(binding: ItemParentCommentBinding, listensCountValue: Int) {

        binding.listensCount.tag = listensCountValue
        binding.listensCount.apply {
            visibility = if (listensCountValue > 0) View.VISIBLE else View.GONE
            text = binding.root.context.resources.getQuantityString(
                R.plurals.listens_count,
                listensCountValue,
                listensCountValue
            )
        }
    }

    private fun setupListens(binding: ItemParentCommentBinding) {
        setListensUI(binding, comment.listensCount ?: 0)

        binding.audioPlayer.playListener = {
            val newCount = binding.listensCount.tag as Int + 1
            binding.listensCount.tag = newCount
            setListensUI(binding, newCount)
            onCommentListen.invoke(comment.id)
        }
    }

    override fun getLayout() = R.layout.item_parent_comment
    override fun initializeViewBinding(view: View) = ItemParentCommentBinding.bind(view)

    private fun isOwnerOf(id: Int, comment: CommentUIModel): Boolean {
        Timber.d("Owner Comment $id-> ${comment.user?.id}")
        return id == comment.user?.id
    }
}
