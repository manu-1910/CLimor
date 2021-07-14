package com.limor.app.scenes.main_new.fragments.comments.list.item

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemViewMoreCommentsBinding
import com.limor.app.uimodels.CommentUIModel
import com.xwray.groupie.viewbinding.BindableItem

class ViewMoreCommentsItem(
    val comment: CommentUIModel,
    val onViewMoreCommentsClick: (CommentUIModel) -> Unit
) : BindableItem<ItemViewMoreCommentsBinding>() {

    override fun bind(viewBinding: ItemViewMoreCommentsBinding, position: Int) {
        viewBinding.viewMoreCommentsBtn.text = viewBinding.root.context.getString(
            R.string.view_more_comments,
            comment.innerComments.size - 1
        )
        viewBinding.viewMoreCommentsBtn.setOnClickListener {
            onViewMoreCommentsClick(comment)
        }
    }

    override fun getLayout() = R.layout.item_view_more_comments
    override fun initializeViewBinding(view: View) = ItemViewMoreCommentsBinding.bind(view)
}