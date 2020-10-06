package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.square1.limor.R
import io.square1.limor.scenes.utils.CommonsKt
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
    private var btnBlock: Button = itemView.findViewById(R.id.btnBlock)
    private var tvUserFullname: TextView = itemView.findViewById(R.id.tvUserFullname)
    private var tvUsername: TextView = itemView.findViewById(R.id.tvUsername)

    fun bind(currentItem: UIUser, position: Int) {
        if (currentItem.blocked) {
            CommonsKt.setButtonLimorStylePressed(btnBlock, false, R.string.block, R.string.unblock)
        } else {
            CommonsKt.setButtonLimorStylePressed(btnBlock, true, R.string.block, R.string.unblock)
        }

        btnBlock.onClick { listener.onBlockClicked(currentItem, position) }

        var firstName = ""
        currentItem.first_name?.let {
            firstName = it
        }

        var lastName = ""
        currentItem.last_name?.let {
            lastName = it
        }

        tvUserFullname.text = context.getString(R.string.user_fullname, firstName, lastName)
        tvUserFullname.onClick { listener.onUserClicked(currentItem, position) }

        tvUsername.text = currentItem.username
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