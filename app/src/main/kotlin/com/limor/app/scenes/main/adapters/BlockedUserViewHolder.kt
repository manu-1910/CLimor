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
import com.limor.app.R
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIUser
import org.jetbrains.anko.sdk23.listeners.onClick
import java.util.*

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
    private var btnBlock: Button = itemView.findViewById(R.id.btnBlock)
    private var tvCapitals: TextView = itemView.findViewById(R.id.tvCapitals)
    private var tvUsername: TextView = itemView.findViewById(R.id.tvUsername)

    fun bind(currentItem: UIUser, position: Int) {
        if (currentItem.blocked) {
            CommonsKt.setButtonLimorStylePressed(btnBlock, false, R.string.block, R.string.unblock)
        } else {
            CommonsKt.setButtonLimorStylePressed(btnBlock, true, R.string.block, R.string.unblock)
        }

        btnBlock.onClick { listener.onBlockClicked(currentItem, position) }

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