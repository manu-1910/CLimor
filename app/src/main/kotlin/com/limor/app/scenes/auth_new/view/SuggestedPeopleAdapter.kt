package com.limor.app.scenes.auth_new.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.extensions.setRightDrawable
import com.limor.app.scenes.auth_new.data.SuggestedUser
import com.limor.app.scenes.auth_new.util.colorStateList
import de.hdodenhof.circleimageview.CircleImageView

abstract class SuggestedPeopleAdapter(private val dataSet: List<SuggestedUser>) :
    RecyclerView.Adapter<SuggestedPeopleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: CircleImageView = view.findViewById(R.id.person_image)
        val name: TextView = view.findViewById(R.id.full_name)
        val description: TextView = view.findViewById(R.id.description)
        val btnFollow: Button = view.findViewById(R.id.follow_btn)
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
        if(item.uiUser?.isVerified == true){
            viewHolder.name.setRightDrawable(R.drawable.ic_verified_badge, R.dimen.chip_close_icon_size)
        } else{
            viewHolder.name.setRightDrawable(0, R.dimen.chip_close_icon_size)
        }
        viewHolder.description.text = item.uiUser?.description
        viewHolder.btnFollow.setOnClickListener {
            onSuggestedFollowClicked(item)
            //notifyDataSetChanged()
            viewHolder.btnFollow.setText(if (item.selected) R.string.unfollow else R.string.follow)
            viewHolder.btnFollow.backgroundTintList = colorStateList(
                viewHolder.itemView.context,
                if (item.selected) R.color.main_button_background_follow else R.color.main_button_background
            )
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