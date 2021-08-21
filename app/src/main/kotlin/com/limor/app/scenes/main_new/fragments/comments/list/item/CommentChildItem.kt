package com.limor.app.scenes.main_new.fragments.comments.list.item

import android.text.TextUtils
import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemChildCommentBinding
import com.limor.app.extensions.*
import com.limor.app.scenes.utils.DateUiUtil
import com.limor.app.uimodels.CommentUIModel
import com.xwray.groupie.viewbinding.BindableItem

/**
 * @param isSimplified - should item be simplified (e.g. comment size limit)
 */
class CommentChildItem(
    val parentComment: CommentUIModel,
    val comment: CommentUIModel,
    val isSimplified: Boolean,
    val onReplyClick: (parentComment: CommentUIModel, replyToComment: CommentUIModel) -> Unit,
    val onLikeClick: (comment: CommentUIModel, liked: Boolean) -> Unit,
) : BindableItem<ItemChildCommentBinding>() {

    override fun bind(viewBinding: ItemChildCommentBinding, position: Int) {
        viewBinding.tvCommentName.text = comment.user?.username
        viewBinding.tvCommentDate.text = comment.createdAt?.let { createdAt ->
            DateUiUtil.getTimeAgoText(
                createdAt,
                viewBinding.root.context
            )
        }
        comment.user?.imageLinks?.small?.let {
            viewBinding.ivCommentAvatar.loadCircleImage(it)
        }
        viewBinding.tvCommentContent.text = comment.content
        viewBinding.tvCommentContent.highlight(userMentionPattern, R.color.primaryYellowColor)

        viewBinding.replyBtn.setOnClickListener {
            onReplyClick(parentComment, comment)
        }
        if (isSimplified) {
            viewBinding.tvCommentContent.maxLines = 3
            viewBinding.tvCommentContent.ellipsize = TextUtils.TruncateAt.END
        } else {
            viewBinding.tvCommentContent.maxLines = Int.MAX_VALUE
        }
        initLikeState(viewBinding)
        initAudioPlayer(viewBinding)
    }

    private fun initAudioPlayer(binding: ItemChildCommentBinding) {
        binding.audioPlayer.apply {
            if (comment.audio != null) {
                makeVisible()
                initialize(comment.audio)
            } else {
                makeGone()
            }
        }
    }

    private fun initLikeState(binding: ItemChildCommentBinding) {
        binding.apply {
            btnCommentLike.isLiked = comment.isLiked!!
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


                likesCount.text = root.context.resources.getQuantityString(
                    R.plurals.likes_count,
                    newLikesCount,
                    newLikesCount
                )

                onLikeClick(comment, isLiked)
            }
        }
    }

    override fun getLayout() = R.layout.item_child_comment
    override fun initializeViewBinding(view: View) = ItemChildCommentBinding.bind(view)
}
