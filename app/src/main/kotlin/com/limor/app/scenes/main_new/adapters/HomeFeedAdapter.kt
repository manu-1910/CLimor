package com.limor.app.scenes.main_new.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.limor.app.databinding.ItemHomeFeedBinding
import com.limor.app.databinding.ItemHomeFeedRecastedBinding
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderPodcast
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderRecast
import com.limor.app.uimodels.CastUIModel

class HomeFeedAdapter(
    private val onLikeClick: (castId: Int, like: Boolean) -> Unit,
    private val onCastClick: (cast: CastUIModel) -> Unit,
    private val onReCastClick: (castId: Int) -> Unit,
    private val onCommentsClick: (CastUIModel) -> Unit,
) : ListAdapter<CastUIModel, ViewHolderBindable<CastUIModel>>(
    HomeFeedDiffCallback()
) {

    override fun getItemViewType(position: Int): Int {
        val recasted = getItem(position).recasted == true
        return if (recasted) ITEM_TYPE_RECASTED else ITEM_TYPE_PODCAST
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<CastUIModel> {
        return getViewHolderByViewType(viewGroup, viewType)
    }

    private fun getViewHolderByViewType(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderBindable<CastUIModel> {
        val inflater = LayoutInflater.from(viewGroup.context)
        return when (viewType) {
            ITEM_TYPE_RECASTED -> {
                val binding =
                    ItemHomeFeedRecastedBinding.inflate(inflater, viewGroup, false)
                ViewHolderRecast(binding)
            }
            else -> {
                val binding = ItemHomeFeedBinding.inflate(inflater, viewGroup, false)
                ViewHolderPodcast(binding, onLikeClick, onCastClick, onReCastClick, onCommentsClick)
            }
        }
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolderBindable<CastUIModel>,
        position: Int
    ) {
        viewHolder.bind(getItem(position))
    }

    companion object {
        private const val ITEM_TYPE_PODCAST = 1
        private const val ITEM_TYPE_RECASTED = 2
    }
}

class HomeFeedDiffCallback : DiffUtil.ItemCallback<CastUIModel>() {
    override fun areItemsTheSame(
        oldItem: CastUIModel,
        newItem: CastUIModel
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: CastUIModel,
        newItem: CastUIModel
    ): Boolean {
        return oldItem == newItem
    }
}

