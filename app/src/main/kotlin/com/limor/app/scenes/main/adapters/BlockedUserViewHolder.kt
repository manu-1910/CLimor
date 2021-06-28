package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.limor.app.FollowersQuery
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.R
import com.limor.app.databinding.ItemUserFollowersBinding
import com.limor.app.scenes.main.fragments.settings.adapters.AdapterBlockedUsers
import com.limor.app.scenes.utils.CommonsKt
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.sdk23.listeners.onLongClick
import timber.log.Timber
import java.util.*

class BlockedUserViewHolder(
    val binding: ItemUserFollowersBinding, val listener: AdapterBlockedUsers.OnFollowerClickListener
) : RecyclerView.ViewHolder(binding.root) {
    private var ivUser: ImageView = binding.ivUser
    private var btnFollow: Button = binding.btnFollow
    private var tvCapitals: TextView = binding.tvTitle
    private var tvUsername: TextView = binding.tvSubtitle

    fun bind(currentItem: GetBlockedUsersQuery.GetBlockedUser, position: Int) {

        Timber.d("Blocked -> %s", currentItem)
        if (currentItem.blocked!!) {
            CommonsKt.setButtonFollowerStylePressed(
                btnFollow,
                false,
                R.string.block,
                R.string.unblock
            )
        } else {
            CommonsKt.setButtonFollowerStylePressed(
                btnFollow,
                true,
                R.string.block,
                R.string.unblock
            )
        }

        btnFollow.onClick {

            /*if (currentItem.followed) {
                CommonsKt.setButtonFollowerStylePressed(
                    btnFollow,
                    false,
                    R.string.follow,
                    R.string.following
                )
            } else {
                CommonsKt.setButtonFollowerStylePressed(
                    btnFollow,
                    true,
                    R.string.follow,
                    R.string.following
                )

            }*/

            listener.onBlockClicked(currentItem, position)
        }

        val firstName = currentItem.first_name
        val lastName = currentItem.last_name
        val fullname = binding.root.context.getString(R.string.user_fullname, firstName, lastName)

        tvCapitals.text = fullname
        tvCapitals.onClick { listener.onUserClicked(currentItem, position) }

        tvUsername.text = currentItem.description
        tvUsername.onClick { listener.onUserClicked(currentItem, position) }

        binding.root.onLongClick {
            listener.onUserLongClicked(currentItem,position)
            return@onLongClick true
        }


        Glide.with(itemView.context)
            .load(currentItem.images?.small_url)
            .placeholder(R.mipmap.ic_launcher_round)
            .error(R.mipmap.ic_launcher_round)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivUser)
        ivUser.onClick { listener.onUserClicked(currentItem, position) }

    }

}