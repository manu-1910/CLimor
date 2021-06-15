package com.limor.app.scenes.auth_new.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.scenes.auth_new.data.SuggestedUser
import com.limor.app.scenes.auth_new.util.colorStateList

abstract class SuggestedPeopleAdapter(private val dataSet: List<SuggestedUser>) :
    RecyclerView.Adapter<SuggestedPeopleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.ivAvatar)
        val name: TextView = view.findViewById(R.id.tvName)
        val nickName: TextView = view.findViewById(R.id.tvNickname)
        val btnFollow: Button = view.findViewById(R.id.btnFollow)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_suggested_people, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position]
        viewHolder.avatar
        viewHolder.name.text = item.name
        viewHolder.nickName.text = item.nickname
        viewHolder.btnFollow.setOnClickListener {
            onSuggestedFollowClicked(item)
            notifyDataSetChanged()
        }
        viewHolder.btnFollow.setText(if (item.selected) R.string.unfollow else R.string.follow)
        viewHolder.btnFollow.backgroundTintList = colorStateList(
            viewHolder.itemView.context,
            if (item.selected) R.color.main_button_background_follow else R.color.main_button_background
        )

        viewHolder.btnFollow.setTextColor(
            colorStateList(
                viewHolder.itemView.context,
                if (item.selected) R.color.main_button_text_color_follow else R.color.main_button_text_color
            )
        )
        Glide.with(viewHolder.avatar)
            .load(item.avatar)
            .placeholder(R.color.dark_transparent)
            .into(viewHolder.avatar)
    }

    override fun getItemCount() = dataSet.size
    abstract fun onSuggestedFollowClicked(user: SuggestedUser)
}