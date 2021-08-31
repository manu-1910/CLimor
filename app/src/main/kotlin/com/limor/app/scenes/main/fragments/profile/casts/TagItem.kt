package com.limor.app.scenes.main.fragments.profile.casts

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemTagBinding
import com.limor.app.uimodels.TagUIModel
import com.xwray.groupie.viewbinding.BindableItem

class TagItem(
    val tag: TagUIModel,
    private val onHashTagClick: (hashTag: TagUIModel) -> Unit
) : BindableItem<ItemTagBinding>() {

    override fun bind(viewBinding: ItemTagBinding, position: Int) {
        viewBinding.tag.text = String.format("#%s", tag.tag)
        viewBinding.tag.setOnClickListener {
            onHashTagClick(tag)
        }
    }

    override fun getLayout() = R.layout.item_tag
    override fun initializeViewBinding(view: View) = ItemTagBinding.bind(view)
}