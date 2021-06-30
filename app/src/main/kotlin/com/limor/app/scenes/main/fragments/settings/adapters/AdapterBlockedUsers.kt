package com.limor.app.scenes.main.fragments.settings.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.FollowersQuery
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.databinding.ItemUserFollowersBinding
import com.limor.app.scenes.main.adapters.BlockedUserViewHolder
import com.limor.app.scenes.main.fragments.profile.adapters.UserFollowersViewHolder
import com.limor.app.uimodels.UIUser
import org.jetbrains.anko.layoutInflater

class AdapterBlockedUsers(var list: ArrayList<GetBlockedUsersQuery.GetBlockedUser?>, val listener: OnFollowerClickListener)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnFollowerClickListener {
        fun onUserClicked(item:  GetBlockedUsersQuery.GetBlockedUser, position: Int)
        fun onBlockClicked(item:  GetBlockedUsersQuery.GetBlockedUser, position: Int)
        fun onUserLongClicked(item:  GetBlockedUsersQuery.GetBlockedUser, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return BlockedUserViewHolder(ItemUserFollowersBinding.inflate(parent.context.layoutInflater,parent,false),listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        list[position]?.let{
            ( holder as BlockedUserViewHolder)
                .bind(it,position)
        }
    }

    override fun getItemCount(): Int {
       return list.size
    }

    fun updateItem(item: GetBlockedUsersQuery.GetBlockedUser, position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }
}