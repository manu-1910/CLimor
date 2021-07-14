package com.limor.app.scenes.main_new.fragments.comments.list.item

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemParentCommentBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.scenes.utils.DateUiUtil
import com.limor.app.uimodels.CommentUIModel
import com.xwray.groupie.viewbinding.BindableItem

class CommentParentItem(
    val comment: CommentUIModel,
    val onReplyClick: (parentComment: CommentUIModel) -> Unit
): BindableItem<ItemParentCommentBinding>() {

    override fun bind(viewBinding: ItemParentCommentBinding, position: Int) {
        viewBinding.tvCommentName.text = comment.user?.getFullName()
        viewBinding.tvCommentDate.text = comment.createdAt?.let { createdAt ->
            DateUiUtil.getPastDateDaysTextDescription(
                createdAt,
                viewBinding.root.context
            )
        }
        viewBinding.tvCommentContent.text = comment.content
        comment.user?.imageLinks?.small?.let {
            viewBinding.ivCommentAvatar.loadCircleImage(it)
        }
        viewBinding.replyBtn.setOnClickListener {
            onReplyClick(comment)
        }
    }

    override fun getLayout() = R.layout.item_parent_comment
    override fun initializeViewBinding(view: View) = ItemParentCommentBinding.bind(view)
}
