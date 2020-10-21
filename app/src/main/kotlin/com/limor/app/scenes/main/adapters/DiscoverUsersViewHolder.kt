package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIUser
import org.jetbrains.anko.sdk23.listeners.onClick
import java.util.*


class DiscoverUsersViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val userSearchClickListener: DiscoverUsersAdapter.OnUserSearchClicked,
    private val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.discover_user_item,
        parent,
        false
    )
) {

    private var btnFollow: Button = itemView.findViewById(R.id.btn_follow)
    private var tvTitle: TextView = itemView.findViewById(R.id.tv_title)
    private var tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
    private var ivUser: ImageView = itemView.findViewById(R.id.iv_user)
    private var rlRoot: RelativeLayout = itemView.findViewById(R.id.rl_root_view)

    fun bind(currentItem: UIUser, position: Int) {

        rlRoot.onClick { userSearchClickListener.onUserClicked(currentItem, position) }

        if (currentItem.first_name != null && currentItem.last_name != null) {
            try {
                val firstNameChar =
                    currentItem.first_name?.substring(0, 1)?.toUpperCase(Locale.ROOT)
                val secondNameChar =
                    currentItem.last_name?.substring(0, 1)?.toUpperCase(Locale.ROOT)
                val text = firstNameChar + secondNameChar
                tvTitle.text = text

                val names = currentItem.first_name + currentItem.last_name
                tvSubtitle.text = names

            } catch (e: Exception) {
                //e.printStackTrace()
                setTitleFromUsername(currentItem)
                tvSubtitle.text = currentItem.username
            }
        } else {
            setTitleFromUsername(currentItem)
            tvSubtitle.text = currentItem.username
        }


        if(currentItem.blocked) {
            btnFollow.visibility = View.GONE
        } else {
            btnFollow.visibility = View.VISIBLE
            if(currentItem.followed){
                CommonsKt.setButtonLimorStylePressed(btnFollow, false, R.string.follow, R.string.unfollow)
            }else{
                CommonsKt.setButtonLimorStylePressed(btnFollow, true, R.string.follow, R.string.unfollow)
            }

            btnFollow.onClick {
                userSearchClickListener.onFollowClicked(
                    currentItem,
                    position
                )
            }
        }


        loadOwnerImage(currentItem)

    }

    private fun setTitleFromUsername(currentItem: UIUser) {
        var text : String? = ""
        if(currentItem.username?.length ?: 0 >= 2) {
            text = currentItem.username?.substring(0, 2)?.toUpperCase(Locale.ROOT)
        }
        tvTitle.text = text
    }

    private fun loadOwnerImage(currentItem: UIUser) {
        Glide.with(context)
            .load(currentItem.images.small_url)
            .placeholder(R.drawable.hashtag)
            .circleCrop()
            .into(ivUser)
    }

}