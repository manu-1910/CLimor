package com.limor.app.scenes.main.fragments.discover.common

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverHeaderBinding
import com.limor.app.extensions.makeGone
import com.limor.app.extensions.makeVisible
import com.xwray.groupie.viewbinding.BindableItem

class HeaderItem(
    val name: String,
    val action: HeaderAction? = null
) : BindableItem<ItemDiscoverHeaderBinding>() {
    data class HeaderAction(val name: String, val onActionClick: () -> Unit)

    override fun bind(viewBinding: ItemDiscoverHeaderBinding, position: Int) {
        viewBinding.title.text = name

        if (action != null) {
            viewBinding.action.makeVisible()
            viewBinding.action.setOnClickListener { action.onActionClick() }
        } else {
            viewBinding.action.makeGone()
        }
    }

    override fun getLayout() = R.layout.item_discover_header
    override fun initializeViewBinding(view: View) = ItemDiscoverHeaderBinding.bind(view)
}
