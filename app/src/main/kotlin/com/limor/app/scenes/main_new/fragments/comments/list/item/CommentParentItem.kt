package com.limor.app.scenes.main_new.fragments.comments.list.item

import android.view.View
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
) : BindableItem<ItemParentCommentBinding>() {

    override fun bind(viewBinding: ItemParentCommentBinding, position: Int) {
        viewBinding.tvCommentName.text = comment.user?.getFullName()
        viewBinding.tvCommentDate.text = comment.createdAt?.let { createdAt ->
            DateUiUtil.getPastDateDaysTextDescription(
                createdAt,
                viewBinding.root.context
            )
        }
        viewBinding.tvCommentContent.text = comment.content
        viewBinding.tvCommentContent.highlight(userMentionPattern, R.color.primaryYellowColor)
        comment.user?.imageLinks?.small?.let {
            viewBinding.ivCommentAvatar.loadCircleImage(it)
        }
        viewBinding.replyBtn.setOnClickListener {
            onReplyClick(comment)
        }
        initLikeState(viewBinding)
        initAudioPlayer(viewBinding)
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
