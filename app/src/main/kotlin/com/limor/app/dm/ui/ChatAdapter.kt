package com.limor.app.dm.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.webkit.URLUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.limor.app.dm.ChatMessage
import com.limor.app.dm.ChatSessionWithUser
import com.limor.app.dm.ChatTarget
import com.limor.app.dm.ChatWithData
import com.limor.app.extensions.isToday
import com.limor.app.extensions.loadCircleImage
import java.text.SimpleDateFormat

class ChatAdapter(
    private val context: Context,
    private var chatData: ChatWithData
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    init {
        submitList(chatData.messages)
    }

    private class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {

        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem == newItem
    }

    inner class ViewHolderMe(view: View) : RecyclerView.ViewHolder(view) {
        val message = view.findViewById(R.id.message) as TextView
    }

    inner class ViewHolderOther(view: View) : RecyclerView.ViewHolder(view) {
        val message = view.findViewById(R.id.message) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = LayoutInflater
            .from(parent.context)
            .inflate(
                if (viewType == TYPE_ME) R.layout.item_chat_me else R.layout.item_chat_other,
                parent,
                false
            )

        if (viewType == TYPE_ME) {
            return ViewHolderMe(layout)
        }

        return ViewHolderOther(layout)
    }

    override fun getItemViewType(position: Int): Int {
        val message = chatData.messages[position]
        return if (message.chatUserId == null) TYPE_ME else TYPE_OTHER
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val message = chatData.messages[position]
        if (viewHolder.itemViewType == TYPE_ME) {
            val holder = viewHolder as ViewHolderMe
            holder.message.text = message.messageContent
        } else {
            val holder = viewHolder as ViewHolderOther
            holder.message.text = message.messageContent
        }
        viewHolder.itemView.setOnClickListener {
            if (URLUtil.isValidUrl(message.messageContent)) {
                openUrl(message.messageContent)
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            // ignored for now
        }
    }

    override fun getItemCount() = currentList.size

    fun setChatData(data: ChatWithData) {
        chatData = data
        submitList(data.messages)
    }

    companion object {
        const val TYPE_ME = 0
        const val TYPE_OTHER = 1
    }

}