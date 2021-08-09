package com.limor.app.scenes.main_new.adapters.vh

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.limor.app.R
import com.limor.app.databinding.ItemUserMentionBinding
import com.limor.app.scenes.main_new.adapters.UserMentionAdapter
import com.limor.app.uimodels.UserUIModel

class UserMentionViewHolder(
    val binding: ItemUserMentionBinding,
    val listener: UserMentionAdapter.OnUserClickListener
) : RecyclerView.ViewHolder(binding.root) {

    private var ivUser: ImageView = binding.ivUser
    private var tvUserName: TextView = binding.tvTitle

    fun bind(user: UserUIModel, position: Int) {
        tvUserName.text = user.username ?: ""
        binding.root.setOnClickListener { listener.onUserClicked(user) }

        if (user.imageLinks == null) {
            binding.ivUser.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            Glide.with(itemView.context)
                .load(user.imageLinks.small)
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivUser)
        }
    }
}