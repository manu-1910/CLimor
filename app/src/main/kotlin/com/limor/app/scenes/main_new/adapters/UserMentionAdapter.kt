package com.limor.app.scenes.main_new.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.databinding.ItemUserMentionBinding
import com.limor.app.scenes.main_new.adapters.vh.UserMentionViewHolder
import com.limor.app.uimodels.UserUIModel
import org.jetbrains.anko.layoutInflater

class UserMentionAdapter(var users: List<UserUIModel>, val listener: OnUserClickListener)
    : RecyclerView.Adapter<UserMentionViewHolder>() {

    interface OnUserClickListener {
        fun onUserClicked(item: UserUIModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserMentionViewHolder {
        return UserMentionViewHolder(ItemUserMentionBinding.inflate(parent.context.layoutInflater, parent, false), listener)
    }

    override fun onBindViewHolder(holder: UserMentionViewHolder, position: Int) {
        holder.bind(users[position], position)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun setData(users: List<UserUIModel>) {
        this.users = users;
        notifyDataSetChanged()
    }

}