package com.limor.app.components.tabselector

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.extensions.px
import com.xwray.groupie.GroupieAdapter

class TabSelectorView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_tab_selector, this)
    }

    private val tabSpacing = 6.px
    private val tabsList = findViewById<RecyclerView>(R.id.tabs_list)
    private val tabsAdapter = GroupieAdapter()

    private var tabSelectionListener: (tabName: String, position: Int) -> Unit = { _, _ -> }
    private var selectedPosition = 0

    private var mode: Mode = Mode.FIXED

    init {
        tabsList.adapter = tabsAdapter
        tabsList.addItemDecoration(TabItemDecoration(tabSpacing))
        tabsAdapter.setOnItemClickListener { item, view ->
            // Skip tapping on the same tab
            if (selectedPosition != tabsAdapter.getAdapterPosition(item)) {
                onItemSelected(item as TabItem)
            }
        }
    }

    fun setMode(mode: Mode) {
        this.mode = mode
    }

    fun setTabs(tabs: List<String>) {
        applyMode(tabs.size)
        tabsAdapter.clear()
        tabsAdapter.addAll(
            tabs.mapIndexed { index, tabName ->
                TabItem(
                    name = tabName,
                    context = context,
                    isSelected = index == 0
                )
            }
        )
    }

    fun setOnTabSelectedListener(onTabSelected: (tabName: String, position: Int) -> Unit) {
        this.tabSelectionListener = onTabSelected
    }

    private fun onItemSelected(selectedItem: TabItem) {
        val prevSelectedItem = (tabsAdapter.getItem(selectedPosition) as TabItem)
        prevSelectedItem.isSelected = false
        selectedItem.isSelected = true
        selectedPosition = tabsAdapter.getAdapterPosition(selectedItem)

        prevSelectedItem.notifyChanged()
        selectedItem.notifyChanged()

        tabSelectionListener(selectedItem.name, selectedPosition)
    }

    private fun applyMode(tabsCount: Int) {
        when (mode) {
            Mode.FIXED -> {
                tabsList.layoutManager =
                    object : LinearLayoutManager(context, RecyclerView.HORIZONTAL, false) {
                        override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                            // Entire visible recyclerView filled with tabs
                            val desiredWidth = (width - (tabSpacing * 2)) / tabsCount
                            if (lp.width < desiredWidth) {
                                lp.width = desiredWidth
                            }
                            return true
                        }
                    }
            }
            Mode.SCROLLABLE -> {
                tabsList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            }
        }
    }

    private class TabItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            // Set the same spacing for the first item
            if (position == 0) {
                outRect.left = spacing
            }
            if (position == parent.childCount) {
                outRect.right = spacing
            }
            outRect.top = spacing
            outRect.bottom = spacing
        }
    }

    enum class Mode {
        /**
         * Tabs fill the width of RecyclerView container
         */
        FIXED,
        /**
         * Tabs width = wrap_content
         */
        SCROLLABLE
    }
}