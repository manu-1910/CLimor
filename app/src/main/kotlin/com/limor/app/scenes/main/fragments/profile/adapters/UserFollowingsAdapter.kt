package com.limor.app.scenes.main.fragments.profile.adapters


import com.limor.app.scenes.main.adapters.BlockedUserViewHolder
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.uimodels.UIUser


class UserFollowingsAdapter(
    var context: Context,
    var list: ArrayList<UIUser>,
    private val followingClickListener: OnFollowingClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater = LayoutInflater.from(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserFollowingsViewHolder(inflator, parent, followingClickListener, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]
        val viewHolder: UserFollowingsViewHolder = holder as UserFollowingsViewHolder
        viewHolder.bind(currentItem, position)
    }

    interface OnFollowingClickListener {
        fun onUserClicked(item: UIUser, position: Int)
        fun onFollowClicked(item: UIUser, position: Int)
    }
}