package com.limor.app.scenes.main.fragments.settings.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.R
import com.limor.app.scenes.main.fragments.profile.adapters.UserFollowersAdapter
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIUser
import org.jetbrains.anko.sdk23.listeners.onClick
import java.util.*


class ViewHolderBlockedUser(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val listener: AdapterBlockedUsers.OnFollowerClickListener,
    val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.item_user_followers,
        parent,
        false
    )
) {


    private var ivUser: ImageView = itemView.findViewById(R.id.iv_user)
    private var btnFollow: Button = itemView.findViewById(R.id.btn_follow)
    private var tvCapitals: TextView = itemView.findViewById(R.id.tv_title)
    private var tvUsername: TextView = itemView.findViewById(R.id.tv_subtitle)

    fun bind(currentItem: GetBlockedUsersQuery.GetBlockedUser, position: Int) {
        if (currentItem.blocked!!) {
            CommonsKt.setButtonLimorStylePressed(btnFollow, false, R.string.unblock, R.string.following)
        } else {
            CommonsKt.setButtonLimorStylePressed(btnFollow, true, R.string.follow, R.string.following)
        }

        btnFollow.onClick {
            if (currentItem.blocked) {
                CommonsKt.setUserItemButtonPressed(btnFollow, true, R.string.follow, R.string.following)
            } /*else {
                CommonsKt.setUserItemButtonPressed(btnFollow, false, R.string.follow, R.string.following)
            }*/

            listener.onBlockClicked(currentItem, position)
        }
        var usernameCapitals = ""
        var finalUsername = ""
        currentItem.username?.let {
            finalUsername = it
            usernameCapitals = when {
                it.length == 1 -> it.uppercase(Locale.getDefault())
                it.length >= 2 -> it.substring(0, 2).uppercase(Locale.getDefault())
                else -> ""
            }
        }
        tvCapitals.text = usernameCapitals
        tvCapitals.onClick { listener.onUserClicked(currentItem, position) }

        tvUsername.text = finalUsername
        tvUsername.onClick { listener.onUserClicked(currentItem, position) }


        Glide.with(itemView.context)
            .load(currentItem.images?.small_url)
            .placeholder(R.mipmap.ic_launcher_round)
            .error(R.mipmap.ic_launcher_round)
            .apply(RequestOptions.circleCropTransform())
            .into(ivUser)
        ivUser.onClick { listener.onUserClicked(currentItem, position) }

    }

}