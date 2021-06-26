package com.limor.app.components.tabselector

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.limor.app.R
import com.limor.app.databinding.ItemTabselectorTabBinding
import com.xwray.groupie.viewbinding.BindableItem

class TabItem(
    val name: String,
    var isSelected: Boolean,
    context: Context
): BindableItem<ItemTabselectorTabBinding>() {

    @ColorInt
    private val selectedContainerColor: Int = Color.WHITE
    @ColorInt
    private val unSelectedContainerColor: Int = Color.TRANSPARENT

    private val selectedContainerElevation = 36f
    private val unSelectedContainerElevation = 0f

    @ColorInt
    private val selectedTextColor: Int = ContextCompat.getColor(context, R.color.textPrimary)
    @ColorInt
    private val unSelectedTextColor: Int = ContextCompat.getColor(context, R.color.textSecondary)

    override fun bind(viewBinding: ItemTabselectorTabBinding, position: Int) {
        viewBinding.textView.text = name
        applyStyle(viewBinding, isSelected)
    }

    private fun applyStyle(viewBinding: ItemTabselectorTabBinding, isSelected: Boolean) {
        if (isSelected) {
            viewBinding.container.apply {
                setCardBackgroundColor(selectedContainerColor)
                cardElevation = selectedContainerElevation
            }
            viewBinding.textView.apply {
                setTextColor(selectedTextColor)
                setTypeface(null, Typeface.BOLD)
            }
        } else {
            viewBinding.container.apply {
                setCardBackgroundColor(unSelectedContainerColor)
                cardElevation = unSelectedContainerElevation
            }
            viewBinding.textView.apply {
                setTextColor(unSelectedTextColor)
                setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    override fun getLayout() = R.layout.item_tabselector_tab
    override fun initializeViewBinding(view: View) = ItemTabselectorTabBinding.bind(view)
}