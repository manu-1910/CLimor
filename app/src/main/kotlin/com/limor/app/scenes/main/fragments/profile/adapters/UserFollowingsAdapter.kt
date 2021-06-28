package com.limor.app.scenes.main.fragments.profile.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.FollowersQuery
import com.limor.app.FriendsQuery
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.databinding.ItemUserFollowersBinding
import com.limor.app.scenes.main.fragments.settings.adapters.ViewHolderBlockedUser
import com.limor.app.uimodels.UIUser
import org.jetbrains.anko.layoutInflater
import timber.log.Timber


class UserFollowingsAdapter(var list: ArrayList<FriendsQuery.GetFriend?>, val listener: OnFollowerClickListener)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnFollowerClickListener {
        fun onUserClicked(item:  FriendsQuery.GetFriend, position: Int)
        fun onUserLongClicked(item:  FriendsQuery.GetFriend, position: Int)
        fun onFollowClicked(item:  FriendsQuery.GetFriend, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserFollowingsViewHolder(ItemUserFollowersBinding.inflate(parent.context.layoutInflater,parent,false), listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        list[position]?.let{
            ( holder as UserFollowingsViewHolder)
                .bind(it,position)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun refreshItems(list:List<FriendsQuery.GetFriend>){
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
    fun addItems(list:List<FriendsQuery.GetFriend?>){
        this.list.addAll(list)
        notifyItemRangeChanged(this.list.size,list.size)
    }
    fun updateItem(item: FriendsQuery.GetFriend, position: Int) {
        val follow = !list[position]?.followed!!
        val i = item.copy(followed = follow)
        list[position] = i
        notifyItemChanged(position)
    }
}