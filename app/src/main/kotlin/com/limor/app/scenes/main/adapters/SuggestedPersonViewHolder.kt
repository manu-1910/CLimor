package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.uimodels.UIUser
import org.jetbrains.anko.sdk23.listeners.onClick
import java.util.*

class SuggestedPersonViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val personClickListener: SuggestedPersonAdapter.OnPersonClicked,
    private val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.suggested_person_item,
        parent,
        false
    )
) {

    private var tvTitle: TextView = itemView.findViewById(R.id.tv_title)
    private var tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
    private var ivUser: ImageView = itemView.findViewById(R.id.iv_suggested_user)
    private var llRoot: LinearLayout = itemView.findViewById(R.id.ll_root_view)

    fun bind(currentItem: UIUser, position: Int) {

        llRoot.onClick { personClickListener.onPersonClicked(currentItem, position) }

        tvSubtitle.text = currentItem.username

        // NOTE: I commented all of this because I was told to not tu use first and last name anymore
        //      I must use username from now on, but this code is actually good, so I won't delete it
        //      just in case we need to go back
//        if (currentItem.first_name != null && currentItem.last_name != null) {
//            try {
//                val firstNameChar =
//                    currentItem.first_name?.substring(0, 1)?.toUpperCase(Locale.ROOT)
//                val secondNameChar =
//                    currentItem.last_name?.substring(0, 1)?.toUpperCase(Locale.ROOT)
//                val text = firstNameChar + secondNameChar
//                tvTitle.text = text
//            } catch (e: Exception) {
//                e.printStackTrace()
//                setTitleFromUsername(currentItem)
//            }
//        } else {
            setTitleFromUsername(currentItem)
//        }

        Glide.with(context)
            .load(currentItem.images.small_url)
            .placeholder(R.drawable.hashtag)
            .circleCrop()
            .into(ivUser)

    }

    private fun setTitleFromUsername(currentItem: UIUser) {
        val text = currentItem.username?.substring(0, 2)?.toUpperCase(Locale.ROOT)
        tvTitle.text = text
    }

}