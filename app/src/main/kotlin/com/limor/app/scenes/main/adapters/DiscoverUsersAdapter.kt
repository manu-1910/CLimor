package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.uimodels.UIUser


class DiscoverUsersAdapter(
    var context: Context,
    list: ArrayList<UIUser>,
    private val userClickListener: OnUserSearchClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<UIUser> = ArrayList()

    init {
        this.list = list
        inflator = LayoutInflater.from(context)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DiscoverUsersViewHolder(inflator, parent, userClickListener, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        val discoverUsersViewHolder: DiscoverUsersViewHolder =
            holder as DiscoverUsersViewHolder
        discoverUsersViewHolder.bind(currentItem, position)

    }

    interface OnUserSearchClicked {
        fun onUserClicked(item: UIUser, position: Int)
        fun onFollowClicked(item: UIUser, position: Int)
    }
}