package com.limor.app.scenes.main.fragments.profile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.limor.app.R
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIUser
import org.jetbrains.anko.sdk23.listeners.onClick
import java.util.*

class UserFollowingsViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val listener: UserFollowingsAdapter.OnFollowingClickListener,
    val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.fragment_followers_followings_user_item,
        parent,
        false
    )
) {


    private var ivUser: ImageView = itemView.findViewById(R.id.ivUser)
    private var btnFollowing: Button = itemView.findViewById(R.id.btnFollow)
    private var tvCapitals: TextView = itemView.findViewById(R.id.tvCapitals)
    private var tvUsername: TextView = itemView.findViewById(R.id.tvUsername)

    fun bind(currentItem: UIUser, position: Int) {
        if (currentItem.followed_by) {
            CommonsKt.setButtonLimorStylePressed(btnFollowing, false, R.string.follow, R.string.following)
        } else {
            CommonsKt.setButtonLimorStylePressed(btnFollowing, true, R.string.follow, R.string.following)
        }

        btnFollowing.onClick {

            if (currentItem.followed_by) {
                CommonsKt.setButtonLimorStylePressed(btnFollowing, true, R.string.follow, R.string.following)
            } else {
                CommonsKt.setButtonLimorStylePressed(btnFollowing, false, R.string.follow, R.string.following)
            }

            listener.onFollowClicked(currentItem, position)
        }

//        var firstName = ""
//        currentItem.first_name?.let {
//            firstName = it
//        }
//
//        var lastName = ""
//        currentItem.last_name?.let {
//            lastName = it
//        }
//        val fullname = context.getString(R.string.user_fullname, firstName, lastName)

        var usernameCapitals = ""
        var finalUsername = ""
        currentItem.username?.let {
            finalUsername = it
            usernameCapitals = when {
                it.length == 1 -> it.toUpperCase(Locale.getDefault())
                it.length >= 2 -> it.substring(0, 2).toUpperCase(Locale.getDefault())
                else -> ""
            }
        }
        tvCapitals.text = usernameCapitals
        tvCapitals.onClick { listener.onUserClicked(currentItem, position) }

        tvUsername.text = finalUsername
        tvUsername.onClick { listener.onUserClicked(currentItem, position) }


        Glide.with(itemView.context)
            .load(currentItem.images.small_url)
            .placeholder(R.mipmap.ic_launcher_round)
            .error(R.mipmap.ic_launcher_round)
            .apply(RequestOptions.circleCropTransform())
            .into(ivUser)
        ivUser.onClick { listener.onUserClicked(currentItem, position) }

    }

}