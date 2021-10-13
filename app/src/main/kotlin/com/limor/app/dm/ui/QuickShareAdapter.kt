package com.limor.app.dm.ui

import android.content.pm.ResolveInfo
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
    private var leanUsers: List<LeanUser>,
    private val onTap: () -> Unit
) :
    RecyclerView.Adapter<QuickShareAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById(R.id.imageAppIcon) as ImageView
        val name = view.findViewById(R.id.textUser) as TextView
        val check = view.findViewById<View>(R.id.check_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_grid_dm_share_user, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = leanUsers[position]

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

    override fun getItemCount() = leanUsers.size

    fun setLeanUsers(leanUsers: List<LeanUser>) {
        this.leanUsers = leanUsers
        notifyDataSetChanged()
    }
}