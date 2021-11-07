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
import com.limor.app.uimodels.UserUIModel
import org.jetbrains.anko.layoutInflater
import timber.log.Timber


class UserFollowersAdapter(
    var list: ArrayList<UserUIModel?>,
    val listener: OnFollowerClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnFollowerClickListener {
        fun onUserClicked(item: UserUIModel, position: Int)
        fun onUserLongClicked(item: UserUIModel, position: Int)
        fun onFollowClicked(item: UserUIModel, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserFollowersViewHolder(
            ItemUserFollowersBinding.inflate(
                parent.context.layoutInflater,
                parent,
                false
            ), listener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        list[position]?.let {
            (holder as UserFollowersViewHolder)
                .bind(it, position)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun refreshItems(list: List<UserUIModel>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun addItems(list: List<UserUIModel?>) {
        this.list.addAll(list)
        notifyItemRangeChanged(this.list.size, list.size)
    }

    fun updateItem(item: UserUIModel, position: Int) {
        val follow = !list[position]?.isFollowed!!
        val i = item.copy(isFollowed = follow)
        list[position] = i
        notifyItemChanged(position)
    }
}