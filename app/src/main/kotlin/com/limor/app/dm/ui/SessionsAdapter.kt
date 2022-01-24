package com.limor.app.dm.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import android.content.Context
import com.limor.app.dm.ChatSessionWithUser
import com.limor.app.dm.ChatTarget
import com.limor.app.extensions.isToday
import com.limor.app.extensions.loadCircleImage
import com.limor.app.extensions.visibleIf
import java.text.SimpleDateFormat

class SessionsAdapter(
    private val context: Context,
    private var sessions: List<ChatSessionWithUser>,
    private val onTap: (target: ChatSessionWithUser) -> Unit
) :
    RecyclerView.Adapter<SessionsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById(R.id.person_image) as ImageView
        val name = view.findViewById(R.id.full_name) as TextView
        val userName = view.findViewById(R.id.description) as TextView
        val textTime = view.findViewById(R.id.textTime) as TextView
        val textUnread = view.findViewById(R.id.textUnread) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_chat_session, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]

        holder.name.text = session.user.limorDisplayName
        holder.userName.text = session.session.lastMessageContent
        holder.image.loadCircleImage(session.user.limorProfileUrl)

        val unread = session.session.unreadCount ?: 0
        holder.textUnread.apply {
            visibleIf(unread > 0)
            text = "$unread"
        }


        session.session.lastMessageDate.let {
            holder.textTime.text = (if (it.isToday()) hourFormat else dateFormat).format(it.time)
        }

        holder.itemView.setOnClickListener {
            onTap(session)
        }
    }

    override fun getItemCount() = sessions.size

    fun setSessions(chatSessions: List<ChatSessionWithUser>) {
        // TODO diff util
        sessions = chatSessions
    }

    fun hasData(): Boolean {
        return sessions.isNotEmpty()
    }

    companion object {
        // It is OK to suppress the SimpleDateFormat linter because we don't need any month names or
        // week names, but only numbers in both formats

        @SuppressLint("SimpleDateFormat")
        private val hourFormat = SimpleDateFormat("HH:mm")

        @SuppressLint("SimpleDateFormat")
        private val dateFormat = SimpleDateFormat("dd/MM/yy")
    }

}