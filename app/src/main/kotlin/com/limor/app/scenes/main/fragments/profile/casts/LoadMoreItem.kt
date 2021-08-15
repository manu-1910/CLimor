package com.limor.app.scenes.main.fragments.profile.casts

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemLoadMoreBinding
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder

class LoadMoreItem(private val onMoreClick: View.OnClickListener) :
    BindableItem<ItemLoadMoreBinding>() {

    var isEnabled = true
        set(value) {
            field = value
            updateStyle()
        }

    private var binding: ItemLoadMoreBinding? = null

    override fun bind(viewBinding: ItemLoadMoreBinding, position: Int) {
        updateStyle(viewBinding)
    }

    override fun createViewHolder(itemView: View): GroupieViewHolder<ItemLoadMoreBinding> {
        return super.createViewHolder(itemView).apply {
            this.binding.loadMore.setOnClickListener {
                if (isEnabled) {
                    onMoreClick.onClick(it)
                }
            }
            updateStyle(this.binding)
        }
    }

    override fun getLayout() = R.layout.item_load_more
    override fun initializeViewBinding(view: View) = ItemLoadMoreBinding.bind(view).apply {
        binding = this
        updateStyle()
    }

    private fun updateStyle(itemLoadMoreBinding: ItemLoadMoreBinding? = null) {
        val binding = itemLoadMoreBinding ?: this.binding
        binding?.let {
            it.loadMore.alpha = if (isEnabled) 1.0f else 0.3f
        }
    }
}
