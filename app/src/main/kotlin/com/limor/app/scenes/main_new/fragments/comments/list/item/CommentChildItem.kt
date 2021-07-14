package com.limor.app.scenes.main_new.fragments.comments.list.item

import android.text.TextUtils
import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemChildCommentBinding
import com.limor.app.extensions.loadCircleImage
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
    val onReplyClick: (parentComment: CommentUIModel, childComment: CommentUIModel) -> Unit
): BindableItem<ItemChildCommentBinding>() {

    override fun bind(viewBinding: ItemChildCommentBinding, position: Int) {
        viewBinding.tvCommentName.text = comment.user?.getFullName()
        viewBinding.tvCommentDate.text = comment.createdAt?.let { createdAt ->
            DateUiUtil.getPastDateDaysTextDescription(
                createdAt,
                viewBinding.root.context
            )
        }
        comment.user?.imageLinks?.small?.let {
            viewBinding.ivCommentAvatar.loadCircleImage(it)
        }
        viewBinding.tvCommentContent.text = comment.content
        viewBinding.likesCount.text = viewBinding.root.context.resources.getQuantityString(
            R.plurals.likes_count,
            comment.likesCount ?: 0,
            comment.likesCount ?: 0
        )
        viewBinding.replyBtn.setOnClickListener {
            onReplyClick(parentComment, comment)
        }
        if (isSimplified) {
            viewBinding.tvCommentContent.maxLines = 3
            viewBinding.tvCommentContent.ellipsize = TextUtils.TruncateAt.END
        } else {
            viewBinding.tvCommentContent.maxLines = Int.MAX_VALUE
        }
    }

    override fun getLayout() = R.layout.item_child_comment
    override fun initializeViewBinding(view: View) = ItemChildCommentBinding.bind(view)
}
