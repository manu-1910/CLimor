package com.limor.app.scenes.main_new.adapters.vh

import androidx.recyclerview.widget.GridLayoutManager
import com.limor.app.databinding.ItemFeedRecommendedCastsBinding
import com.limor.app.extensions.px
import com.limor.app.scenes.main.fragments.discover.common.casts.GridCastItemDecoration
import com.limor.app.scenes.main.fragments.discover.common.casts.SmallCastItem
import com.limor.app.scenes.main_new.fragments.DataItem
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.FeaturedPodcast
import com.limor.app.uimodels.FeedRecommendedCasts
import com.xwray.groupie.GroupieAdapter

class ViewHolderRecommendedCasts(
    val binding: ItemFeedRecommendedCastsBinding
) : ViewHolderBindable<DataItem>(binding) {
    private var suggestedCastsListAdapter = GroupieAdapter()
    private var gridItemDecorator = GridCastItemDecoration(ITEM_SPACING)

    companion object {
        private val ITEM_SPACING: Int = 8.px
    }

    override fun bind(item: DataItem) {
        val recommendedCasts = item as FeedRecommendedCasts
        binding.recommendedCastsTitleTextView.text = recommendedCasts.name
        setAdapter()
        updateUsers(recommendedCasts.recommendedCasts)
    }

    private fun setAdapter() {
        binding.suggestedCastsList.apply {
            adapter = suggestedCastsListAdapter
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            removeItemDecoration(gridItemDecorator)
            addItemDecoration(gridItemDecorator)
        }
    }

    private fun updateUsers(podcasts: List<FeaturedPodcast>) {
        podcasts.sortedBy { it.position }
        val list = mutableListOf<FeaturedPodcast>()
        if(podcasts.isNotEmpty()){
            list.add(podcasts[0])
            list.add(podcasts[2])
            list.add(podcasts[1])
            list.add(podcasts[3])
        }
        suggestedCastsListAdapter.update(
            list.map {
                SmallCastItem(cast = it.podcast, hideDuration = true)
            }
        )
    }

}