package com.limor.app.scenes.main_new.adapters.vh

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemPodcastCommentBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.scenes.utils.DateUiUtil
import com.limor.app.uimodels.CommentUIModel
import com.xwray.groupie.viewbinding.BindableItem

class PodcastCommentItem(
    val comment: CommentUIModel
): BindableItem<ItemPodcastCommentBinding>() {

    override fun bind(viewBinding: ItemPodcastCommentBinding, position: Int) {
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
    }

    override fun getLayout() = R.layout.item_podcast_comment
    override fun initializeViewBinding(view: View) = ItemPodcastCommentBinding.bind(view)
}
