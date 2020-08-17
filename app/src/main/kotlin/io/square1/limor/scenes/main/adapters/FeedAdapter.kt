package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.R
import io.square1.limor.uimodels.UIFeedItem

class FeedAdapter(
    var context: Context,
    list: ArrayList<UIFeedItem>
) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    var inflator: LayoutInflater
    var list: ArrayList<UIFeedItem> = ArrayList()


    init {
        this.list = list
        inflator = LayoutInflater.from(context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedAdapter.ViewHolder {
        val view = inflator.inflate(R.layout.fragment_feed_item_recycler_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: FeedAdapter.ViewHolder, position: Int) {
        val currentItem = list[position]
        println("FEED -> the title of the current item is ${currentItem.podcast?.title}")
        val fullname : String = currentItem.user.first_name + " " + currentItem.user.last_name
        holder.tvUserFullname.setText(fullname)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUserFullname: TextView

        init {
            tvUserFullname = itemView.findViewById<View>(R.id.tvUserName) as TextView
        }

    }


}