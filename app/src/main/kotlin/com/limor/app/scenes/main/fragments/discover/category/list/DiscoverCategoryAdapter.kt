package com.limor.app.scenes.main.fragments.discover.category.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.limor.app.databinding.SimpleViewGroupBinding
import com.limor.app.scenes.main.fragments.discover.common.casts.GridCastItemDecoration
import com.limor.app.scenes.main_new.adapters.vh.*
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.GroupieAdapter

class DiscoverCategoryAdapter(
    context: Context
) : PagingDataAdapter<List<CastUIModel>, ViewHolderBindable<List<CastUIModel>>>(
    DiscoverCastsDiffCallback()
) {

    var showTitle = true

    override fun getItemViewType(position: Int): Int {
        return ITEM_TOP_CASTS
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<List<CastUIModel>> {
        return getViewHolderByViewType(viewGroup, viewType)
    }

    private fun getViewHolderByViewType(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<List<CastUIModel>> {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding =
            SimpleViewGroupBinding.inflate(inflater, viewGroup, false)
        return ViewHolderTopCasts(
            binding,
            CategoriesGroupAdapter(binding.root.context, showTitle)
        )
    }

    override fun onBindViewHolder(holder: ViewHolderBindable<List<CastUIModel>>, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
            showTitle = false
        }
    }

    companion object {
        private const val ITEM_TOP_CASTS = 1
        const val SPAN_COUNT = 2
    }

    class ViewHolderTopCasts(
        val binding: SimpleViewGroupBinding,
        var groupAdapter: CategoriesGroupAdapter
    ) : ViewHolderBindable<List<CastUIModel>>(binding) {

        private val decorator = GridCastItemDecoration()

        override fun bind(item: List<CastUIModel>) {
            binding.castsList.apply {
                layoutManager = GridLayoutManager(context, SPAN_COUNT).apply {
                    spanSizeLookup = groupAdapter.spanSizeLookup
                    adapter = groupAdapter
                    removeItemDecoration(decorator)
                    addItemDecoration(decorator)
                }
                groupAdapter.updateTopCasts(item)
            }
        }
    }

}

class DiscoverCastsDiffCallback : DiffUtil.ItemCallback<List<CastUIModel>>() {
    override fun areItemsTheSame(
        oldItem: List<CastUIModel>,
        newItem: List<CastUIModel>
    ): Boolean {
        return true
    }

    override fun areContentsTheSame(
        oldItem: List<CastUIModel>,
        newItem: List<CastUIModel>
    ): Boolean {
        return true
    }
}

class CategoriesGroupAdapter(
    context: Context,
    showTitle: Boolean
) : GroupieAdapter() {

    companion object {
        private const val SPAN_COUNT = 2
    }

    private val topCastsSection = TopCastsSection(context, showTitle = showTitle)

    init {
        spanCount = SPAN_COUNT
        add(topCastsSection)
    }

    fun updateTopCasts(topCasts: List<CastUIModel>) {
        topCastsSection.updateTopCasts(topCasts)
    }

}
