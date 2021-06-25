package com.limor.app.scenes.main.fragments.profile.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.FollowersQuery
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.databinding.ItemUserFollowersBinding
import com.limor.app.scenes.main.fragments.settings.adapters.ViewHolderBlockedUser
import com.limor.app.uimodels.UIUser
import org.jetbrains.anko.layoutInflater
import timber.log.Timber


class UserFollowersAdapter(var list: ArrayList<FollowersQuery.GetFollower?>, val listener: OnFollowerClickListener)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnFollowerClickListener {
        fun onUserClicked(item:  FollowersQuery.GetFollower, position: Int)
        fun onFollowClicked(item:  FollowersQuery.GetFollower, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserFollowersViewHolder(ItemUserFollowersBinding.inflate(parent.context.layoutInflater,parent,false), listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Timber.d("MJ ISSUE bind fail check-> ${list[position]}")
        list[position]?.let{
            ( holder as UserFollowersViewHolder)
                .bind(it,position)
        }?: run {
            Timber.d("MJ ISSUE bind fail-> ${list[position]}")
        }

    }

    override fun getItemCount(): Int {
        Timber.d("MJ ISSUE size -> ${list.size}")
        return list.size
    }

    fun refreshItems(list:List<FollowersQuery.GetFollower>){
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
    fun addItems(list:List<FollowersQuery.GetFollower?>){
        this.list.addAll(list)
        notifyItemRangeChanged(this.list.size,list.size)
    }
    fun updateItem(item: FollowersQuery.GetFollower, position: Int) {
        val follow = !list[position]?.followed!!
        val i = item.copy(followed = follow)
        list[position] = i
        notifyItemChanged(position)
    }
}