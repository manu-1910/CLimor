package com.limor.app.scenes.main.fragments.profile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.limor.app.FollowersQuery
import com.limor.app.FriendsQuery
import com.limor.app.R
import com.limor.app.databinding.ItemUserFollowersBinding
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.utils.CommonsKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.sdk23.listeners.onLongClick
import timber.log.Timber
import java.util.*

class UserFollowingsViewHolder(
    val binding: ItemUserFollowersBinding, val listener: UserFollowingsAdapter.OnFollowerClickListener
) : RecyclerView.ViewHolder(binding.root) {
    private var ivUser: ImageView = binding.ivUser
    private var btnFollow: Button = binding.btnFollow
    private var tvCapitals: TextView = binding.tvTitle
    private var tvUsername: TextView = binding.tvSubtitle

    fun bind(currentItem: FriendsQuery.GetFriend, position: Int) {

        btnFollow.visibility = if (signedInUserWith(currentItem.id,binding.root.context)) View.GONE else View.VISIBLE

        btnFollow.onClick {
            listener.onFollowClicked(currentItem, position)
        }

        if (currentItem.followed!!) {
            CommonsKt.setButtonFollowerStylePressed(
                btnFollow,
                false,
                R.string.follow,
                R.string.unfollow
            )
        } else {
            CommonsKt.setButtonFollowerStylePressed(
                btnFollow,
                true,
                R.string.follow,
                R.string.unfollow
            )
        }


        val firstName = currentItem.first_name
        val lastName = currentItem.last_name
        val fullname = binding.root.context.getString(R.string.user_fullname, firstName, lastName)

        tvCapitals.text = currentItem.username
        tvCapitals.onClick { listener.onUserClicked(currentItem, position) }

        tvUsername.text = currentItem.description
        tvUsername.onClick { listener.onUserClicked(currentItem, position) }

        binding.root.onLongClick {
            listener.onUserLongClicked(currentItem,position)
            return@onLongClick true
        }


        Glide.with(itemView.context)
            .load(currentItem.images?.small_url)
            .placeholder(R.drawable.ic_podcast_listening)
            .error(R.drawable.ic_podcast_listening)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivUser)
        ivUser.onClick { listener.onUserClicked(currentItem, position) }

    }


    private fun signedInUserWith(id: Int?, context: Context): Boolean {
        return id == PrefsHandler.getCurrentUserId(context)
    }

}