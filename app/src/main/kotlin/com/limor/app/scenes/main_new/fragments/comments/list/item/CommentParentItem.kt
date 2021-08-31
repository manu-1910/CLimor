package com.limor.app.scenes.main_new.fragments.comments.list.item

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
import com.limor.app.R
import com.limor.app.databinding.ItemParentCommentBinding
import com.limor.app.extensions.*
import com.limor.app.scenes.utils.DateUiUtil
import com.limor.app.uimodels.CommentUIModel
import com.xwray.groupie.viewbinding.BindableItem

class CommentParentItem(
    val comment: CommentUIModel,
    val onReplyClick: (parentComment: CommentUIModel) -> Unit,
    val onLikeClick: (parentComment: CommentUIModel, liked: Boolean) -> Unit,
    val onUserMentionClick: (username: String, userId: Int) -> Unit,
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

        comment.user?.imageLinks?.small?.let {
            viewBinding.ivCommentAvatar.loadCircleImage(it)
        }
        viewBinding.replyBtn.setOnClickListener {
            onReplyClick(comment)
        }
        initLikeState(viewBinding)
        initAudioPlayer(viewBinding)
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
            spannable.setSpan(clickableSpan, mention.startIndex, mention.endIndex, 0)
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
        binding.apply {
            btnCommentLike.isLiked = comment.isLiked!!

            btnCommentLike.setOnClickListener {
                val isLiked = btnCommentLike.isLiked
                onLikeClick(comment, isLiked)
            }
        }
    }

    override fun getLayout() = R.layout.item_parent_comment
    override fun initializeViewBinding(view: View) = ItemParentCommentBinding.bind(view)
}
