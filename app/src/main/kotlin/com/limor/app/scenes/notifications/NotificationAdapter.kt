package com.limor.app.scenes.notifications

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.limor.app.App
import com.limor.app.R
import com.limor.app.extensions.setTextWithTagging
import com.limor.app.scenes.main_new.fragments.comments.list.item.MySpannable
import com.limor.app.scenes.utils.DateUiUtil.getTimeElapsedFromDateString
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.NotiUIMode
import de.hdodenhof.circleimageview.CircleImageView
import timber.log.Timber
import androidx.core.content.ContextCompat.startActivity


class DiffUtilCallBack : DiffUtil.ItemCallback<NotiUIMode>() {
    override fun areItemsTheSame(oldItem: NotiUIMode, newItem: NotiUIMode): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NotiUIMode, newItem: NotiUIMode): Boolean {
        return oldItem == newItem && oldItem.read == newItem.read
    }
}

class NotificationAdapter(val context: Context) :
    PagingDataAdapter<NotiUIMode, NotificationAdapter.ViewHolder>(DiffUtilCallBack()) {

    private lateinit var castCallback: (castId: Int?) -> Unit
    private lateinit var userCallback: (userId: Int?, username: String?, tab: Int) -> Unit
    private lateinit var notiReadCallback: (nId: Int?, read: Boolean) -> Unit
    lateinit var noInternetAlertCallback: () -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item_new, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = getItem(position) ?: return

        Glide.with(context).load(notification.initiator?.imageUrl)
            .error(R.drawable.ic_podcast_listening).into(holder.profilePic)
        when (notification.notificationType) {
            "newFollower" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F601))
                holder.profileIcon.visibility = View.GONE
            }
            "podcastComment" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F4AC))
                holder.profileIcon.visibility = View.GONE
            }
            "replyComment" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F4AC))
                holder.profileIcon.visibility = View.GONE
            }
            "podcastLike" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F44D))
                holder.profileIcon.visibility = View.GONE
            }
            "commentLike" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F44D))
                holder.profileIcon.visibility = View.GONE
            }
            "recast" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F973))
                holder.profileIcon.visibility = View.GONE
            }
            "mention" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F4AD))
                holder.profileIcon.visibility = View.GONE
            }
            "friendJoined" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F44B))
                holder.profileIcon.visibility = View.GONE
            }
            "followSuggestion" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F929))
                holder.profileIcon.visibility = View.GONE
            }
            "engagement" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F525))
                holder.profileIcon.visibility = View.GONE
            }
            "patronRequest" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F929))
                holder.profileIcon.visibility = View.GONE
            }
            "internalPatronInvitation" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F44B))
                holder.profileIcon.visibility = View.GONE
            }
            "castPurchased" -> {
                holder.emojiTv.text = String(Character.toChars(0X1F911))
                holder.profileIcon.visibility = View.GONE
            }
        }

        if (notification.notificationType == "internalPatronInvitation") {
            holder.title.text = notification.message
            setClickablePart(notification, holder.title)
        } else {
            holder.title.text = notification.message
        }

        holder.subTitle.text = getTimeElapsedFromDateString(notification.createdAt)

        notification.read?.let {
            if (it) {
                holder.mainBg.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.read_background
                    )
                )
            } else {
                holder.mainBg.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.un_read_background
                    )
                )
            }
        }

        holder.bind(this, position, notification)
    }

    private fun setClickablePart(
        noti: NotiUIMode,
        tv: TextView
    ) {
        val userName: String = tv.text.toString().substringBefore(" ")
        val spannableString = SpannableString(tv.text.toString())
        val usernameClickableSpan: ClickableSpan = object : ClickableSpan() {

            override fun onClick(p0: View) {
                userCallback.invoke(
                    noti.initiator?.userId,
                    noti.initiator?.username,
                    0
                )
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.BLACK
                ds.isUnderlineText = false
            }

        }

        val clickableSpan: ClickableSpan = object : ClickableSpan() {

            override fun onClick(p0: View) {
                userCallback.invoke(
                    noti.receiver?.userId,
                    noti.receiver?.username,
                    1
                )
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.BLACK
                ds.isUnderlineText = false
            }
        }
        spannableString.setSpan(
            usernameClickableSpan,
            0,
            userName.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            clickableSpan,
            userName.length,
            tv.text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv.text = spannableString
        tv.linksClickable = true;
        tv.movementMethod = LinkMovementMethod.getInstance();
        tv.setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    private fun updateRead(notification: NotiUIMode, position: Int) {
        notification.read = true
        notifyItemChanged(position)
    }

    fun openCastCallback(callback: (castId: Int?) -> Unit) {
        this.castCallback = callback
    }

    fun addUserTypeCallback(callback: (userId: Int?, username: String?, tab: Int) -> Unit) {
        this.userCallback = callback
    }

    fun addNotificationReadListener(callback: (nId: Int?, read: Boolean) -> Unit) {
        this.notiReadCallback = callback
    }

    fun addNoInternetAlertCallback(callback: () -> Unit) {
        this.noInternetAlertCallback = callback
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profilePic: CircleImageView = itemView.findViewById(R.id.iv_user)
        var profileIcon: CircleImageView = itemView.findViewById(R.id.circleImageView)
        var title: TextView = itemView.findViewById(R.id.tv_title)
        var subTitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        var mainBg: ConstraintLayout = itemView.findViewById(R.id.main_notification_bg)
        var emojiTv: TextView = itemView.findViewById(R.id.emoji_text)

        fun bind(
            notificationAdapter: NotificationAdapter,
            position: Int,
            noti: NotiUIMode,
        ) {
            itemView.setOnClickListener {
                if (!App.instance.merlinsBeard!!.isConnected) {
                    notificationAdapter.noInternetAlertCallback.invoke()
                } else {
                    //Destination

                    if (noti.read == false) {
                        notificationAdapter.notiReadCallback.invoke(
                            noti.id,
                            true
                        )
                    }
                    when (noti.redirectTarget?.type) {
                        "podcast" -> notificationAdapter.castCallback.invoke(noti.redirectTarget.id)
                        "user" -> {
                            var tab = 0
                            if (noti.notificationType == "patronRequest" || noti.notificationType == "internalPatronInvitation") {
                                tab = 1
                            }
                            notificationAdapter.userCallback.invoke(
                                noti.redirectTarget.id,
                                noti.initiator?.username,
                                tab
                            )
                        }
                        "comment" -> notificationAdapter.userCallback.invoke(
                            noti.redirectTarget.id,
                            noti.initiator?.username, 0
                        )
                        else -> Timber.d("Unable to handle this type")
                    }
                    notificationAdapter.updateRead(noti, position)

                }
            }

            profilePic.setOnClickListener {
                if (!App.instance.merlinsBeard!!.isConnected) {
                    notificationAdapter.noInternetAlertCallback.invoke()
                } else {
                    //User Profile Activity
                    notificationAdapter.userCallback.invoke(
                        noti.initiator?.userId,
                        noti.initiator?.username,
                        0
                    )
                }
            }
        }
    }


}


