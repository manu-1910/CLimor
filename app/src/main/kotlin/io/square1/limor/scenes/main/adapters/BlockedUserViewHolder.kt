package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.square1.limor.R
import io.square1.limor.uimodels.UIUser
import org.jetbrains.anko.sdk23.listeners.onClick

class BlockedUserViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val listener: BlockedUsersAdapter.OnBlockedUserClickListener,
    val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.fragment_blocked_user_item,
        parent,
        false
    )
) {


    private var ivUser: ImageView = itemView.findViewById(R.id.ivUser)
    private var btnFollow: Button = itemView.findViewById(R.id.btnBlock)
    private var tvUserName: TextView = itemView.findViewById(R.id.tvUserName)

    fun bind(currentItem: UIUser, position: Int) {
        if (currentItem.blocked) {
            btnFollow.background =
                ContextCompat.getDrawable(context, R.drawable.bg_round_brand_500_ripple)
            btnFollow.setTextColor(ContextCompat.getColor(context, R.color.white))
            btnFollow.text = context.getString(R.string.unblock)
        } else {
            btnFollow.background =
                ContextCompat.getDrawable(context, R.drawable.bg_round_yellow_ripple)
            btnFollow.setTextColor(ContextCompat.getColor(context, R.color.black))
            btnFollow.text = context.getString(R.string.block)
        }

        btnFollow.onClick { listener.onBlockClicked(currentItem, position) }

        tvUserName.text = "${currentItem.first_name} ${currentItem.last_name}"
        tvUserName.onClick { listener.onUserClicked(currentItem, position) }

        Glide.with(itemView.context)
            .load(currentItem.images.small_url)
            .placeholder(R.mipmap.ic_launcher_round)
            .error(R.mipmap.ic_launcher_round)
            .apply(RequestOptions.circleCropTransform())
            .into(ivUser)
        ivUser.onClick { listener.onUserClicked(currentItem, position) }

    }

}