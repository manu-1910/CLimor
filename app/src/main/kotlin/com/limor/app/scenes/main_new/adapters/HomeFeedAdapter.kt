package com.limor.app.scenes.main_new.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.FeedItemsQuery
import com.limor.app.databinding.ItemHomeFeedBinding
import com.limor.app.databinding.ItemHomeFeedRecastedBinding
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderBindable
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderPodcast
import com.limor.app.scenes.main_new.adapters.vh.ViewHolderRecast

class HomeFeedAdapter(private val dataSet: MutableList<FeedItemsQuery.FeedItem>) :
    RecyclerView.Adapter<ViewHolderBindable>() {

    fun addData(newData: List<FeedItemsQuery.FeedItem>){
        val toAdd = newData.filter { !dataSet.contains(it) }
        dataSet.addAll(toAdd)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val recasted = dataSet[position].recasted == true
        return if (recasted) ITEM_TYPE_RECASTED else ITEM_TYPE_PODCAST
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderBindable {
        return getViewHolderByViewType(viewGroup, viewType)
    }

    private fun getViewHolderByViewType(viewGroup: ViewGroup, viewType: Int): ViewHolderBindable {
        val inflater = LayoutInflater.from(viewGroup.context)
        return when (viewType) {
            ITEM_TYPE_RECASTED -> {
                val binding =
                    ItemHomeFeedRecastedBinding.inflate(inflater, viewGroup, false)
                ViewHolderRecast(binding)
            }
            else -> {
                val binding = ItemHomeFeedBinding.inflate(inflater, viewGroup, false)
                ViewHolderPodcast(binding)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolderBindable, position: Int) {
        viewHolder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

    companion object {
        private const val ITEM_TYPE_PODCAST = 1
        private const val ITEM_TYPE_RECASTED = 2
    }
}

