package com.limor.app.scenes.main_new.fragments.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import kotlinx.android.synthetic.main.item_home_feed.view.*

class HomeFeedAdapter(private val dataSet: List<String>) :
    RecyclerView.Adapter<HomeFeedAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(index : Int){
            view.cpiPodcastListeningProgress.progress = index
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_home_feed, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(position)
    }

    override fun getItemCount() = dataSet.size

}