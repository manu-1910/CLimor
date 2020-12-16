package com.limor.app.scenes.main.fragments.profile.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.uimodels.UIUser


class UserFollowersAdapter(
    var context: Context,
    var list: ArrayList<UIUser>,
    private val followerClickListener: OnFollowerClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater = LayoutInflater.from(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserFollowersViewHolder(inflator, parent, followerClickListener, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]
        val viewHolder: UserFollowersViewHolder = holder as UserFollowersViewHolder
        viewHolder.bind(currentItem, position)
    }

    interface OnFollowerClickListener {
        fun onUserClicked(item: UIUser, position: Int)
        fun onFollowClicked(item: UIUser, position: Int)
    }
}