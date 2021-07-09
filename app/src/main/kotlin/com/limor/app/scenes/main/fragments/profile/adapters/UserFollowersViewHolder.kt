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
import com.limor.app.R
import com.limor.app.databinding.ItemUserFollowersBinding
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.utils.CommonsKt
import kotlinx.coroutines.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.sdk23.listeners.onLongClick
import timber.log.Timber
import java.util.*

class UserFollowersViewHolder(
    val binding: ItemUserFollowersBinding, val listener: UserFollowersAdapter.OnFollowerClickListener
) : RecyclerView.ViewHolder(binding.root) {
    private var ivUser: ImageView = binding.ivUser
    private var btnFollow: Button = binding.btnFollow
    private var tvCapitals: TextView = binding.tvTitle
    private var tvUsername: TextView = binding.tvSubtitle

    fun bind(currentItem: FollowersQuery.GetFollower, position: Int) {

        Timber.d("Follower -> " + currentItem.followed)
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

        CoroutineScope(Dispatchers.Main).launch{
            val userId = withContext(Dispatchers.IO){
                JwtChecker.getUserIdFromJwt()
            }
            if(currentItem.id == userId){
                btnFollow.visibility = View.GONE
            }else{
                btnFollow.visibility = View.VISIBLE
            }
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

            listener.onFollowClicked(currentItem, position)
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