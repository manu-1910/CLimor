package com.limor.app.scenes.patron.manage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.uimodels.UserUIModel
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.backgroundColor

class InviteLimorUsersAdapter(
    private val users: ArrayList<UserUIModel>,
    private val onSelected: (id: Int) -> Unit
) : RecyclerView.Adapter<InviteLimorUsersAdapter.LimorUserViewHolder>() {

    var inviteCount: Int = 5

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LimorUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_limor_user, parent, false)
        return LimorUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: LimorUserViewHolder, position: Int) {
        holder.accountName.text = users[position].getFullName()
        holder.accountNickName.text = users[position].username
        holder.inviteButton.setOnClickListener {
            if (inviteCount > 0) {
                inviteCount -= 1
                holder.inviteButton.text = holder.accountName.context.getString(R.string.joined)
                it.backgroundColor = ContextCompat.getColor(
                    holder.accountName.context,
                    R.color.main_button_background_follow
                )
            } else {
                holder.inviteButton.text = holder.accountName.context.getString(R.string.invite)
            }
            onSelected(inviteCount)
        }
        Glide.with(holder.avatar)
            .load(users[position].imageLinks?.medium)
            .placeholder(R.drawable.ic_podcast_listening)
            .into(holder.avatar)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun setUsers(contactsList: List<UserUIModel>) {
        users.clear()
        users.addAll(contactsList)
        notifyDataSetChanged()
    }

    class LimorUserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: CircleImageView = view.findViewById(R.id.account_image)
        val accountName: TextView = view.findViewById(R.id.account_name)
        val accountNickName: TextView = view.findViewById(R.id.account_nickname)
        val inviteButton: Button = view.findViewById(R.id.invite_button)
    }
}