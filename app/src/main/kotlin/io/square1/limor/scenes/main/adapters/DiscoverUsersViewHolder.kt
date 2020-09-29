package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.square1.limor.R
import io.square1.limor.uimodels.UIUser
import org.jetbrains.anko.sdk23.listeners.onClick
import java.lang.Exception
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


        if(currentItem.followed){
            btnFollow.background = ContextCompat.getDrawable(context, R.drawable.bg_round_brand_500_ripple)
            btnFollow.setTextColor(ContextCompat.getColor(context, R.color.white))
            btnFollow.text = context.getString(R.string.unfollow)
        }else{
            btnFollow.background = ContextCompat.getDrawable(context, R.drawable.bg_round_yellow_ripple)
            btnFollow.setTextColor(ContextCompat.getColor(context, R.color.black))
            btnFollow.text = context.getString(R.string.follow)
        }

        btnFollow.onClick {
            userSearchClickListener.onFollowClicked(
                currentItem,
                position
            )
        }
        loadOwnerImage(currentItem)

    }

    private fun setTitleFromUsername(currentItem: UIUser) {
        val text = currentItem.username?.substring(0, 2)?.toUpperCase(Locale.ROOT)
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