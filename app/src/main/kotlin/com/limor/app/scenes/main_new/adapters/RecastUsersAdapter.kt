package com.limor.app.scenes.main_new.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.databinding.ItemDiscoverSearchAccountBinding
import com.limor.app.scenes.main_new.adapters.vh.RecastUserViewHolder
import com.limor.app.uimodels.UserUIModel
import org.jetbrains.anko.layoutInflater

class RecastUsersAdapter(
    var users: List<UserUIModel>,
    private val onFollowClick: (account: UserUIModel, follow: Boolean) -> Unit
) : RecyclerView.Adapter<RecastUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecastUserViewHolder {
        return RecastUserViewHolder(
            ItemDiscoverSearchAccountBinding.inflate(
                parent.context.layoutInflater,
                parent,
                false
            ),
            onFollowClick
        )
    }

    override fun onBindViewHolder(holder: RecastUserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun setData(users: List<UserUIModel>) {
        this.users = users;
        notifyDataSetChanged()
    }

}