package com.limor.app.dm.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R

import android.content.Context
import com.limor.app.dm.LeanUser
import com.limor.app.extensions.loadCircleImage
import org.jetbrains.anko.backgroundResource


class QuickShareAdapter(
    private val context: Context,
    private var allUsers: List<LeanUser>,
    private val onTap: () -> Unit,
    private val onMore: () -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var leanUsers = mutableListOf<LeanUser>()

    inner class MoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById(R.id.imageAppIcon) as ImageView
        val name = view.findViewById(R.id.textUser) as TextView
        val check = view.findViewById<View>(R.id.check_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_MORE) {
            return MoreViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_grid_dm_more, parent, false)
            )
        }
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_grid_dm_share_user, parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (position == maxUsers) {
            viewHolder.itemView.setOnClickListener {
                onMore()
            }
            return
        }
        val user = leanUsers[position]

        val holder = viewHolder as ViewHolder

        holder.name.text = user.displayName
        holder.image.loadCircleImage(user.profileUrl)

        setCheck(holder, user)

        holder.itemView.setOnClickListener {
            user.selected = !user.selected
            setCheck(holder, user)
            onTap()
        }
    }

    private fun setCheck(holder: ViewHolder, user: LeanUser) {
        holder.check.backgroundResource = if (user.selected) R.drawable.ic_dm_user_checked_bg else R.drawable.ic_dm_user_unchecked_bg
    }

    private fun hasMore(): Boolean {
        return allUsers.size > maxUsers
    }

    override fun getItemViewType(position: Int): Int {
        if (position == maxUsers) {
            return TYPE_MORE
        }
        return TYPE_USER
    }

    override fun getItemCount() = if (hasMore()) maxUsers + 1 else leanUsers.size

    companion object {
        const val TYPE_USER = 0
        const val TYPE_MORE = 1

        const val maxUsers = 10
    }

    fun setAllLeanUsers(users: List<LeanUser>) {
        this.allUsers = users

        this.leanUsers.apply {
            clear()
            addAll(if (hasMore()) users.subList(0, 10) else users)
        }

        notifyDataSetChanged()
    }
}