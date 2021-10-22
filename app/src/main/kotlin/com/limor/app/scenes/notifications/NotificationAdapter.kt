package com.limor.app.scenes.notifications

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.limor.app.App
import com.limor.app.R
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.DateUiUtil
import com.limor.app.scenes.utils.DateUiUtil.getTimeElapsedFromDateString
import com.limor.app.uimodels.NotiUIMode
import de.hdodenhof.circleimageview.CircleImageView
import io.opencensus.internal.Utils
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.alert
import timber.log.Timber

class NotificationAdapter(val context: Context, val notificationsList: ArrayList<NotiUIMode>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private lateinit var castCallback: (castId: Int?) -> Unit
    private lateinit var userCallback: (userId: Int?, username: String?) -> Unit
    private lateinit var notiReadCallback: (nId: Int?, read: Boolean) -> Unit
    private lateinit var noInternetAlertCallback: () -> Unit
    private val imageicon = arrayOf(
        R.drawable.ic_icon_follower,
        R.drawable.ic_icon_comment,
        R.drawable.ic_icon_recast,
        R.drawable.ic_group_902,
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item_new, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (notificationsList.isNotEmpty() && notificationsList.size > position) {
            val noti = notificationsList[position]

            noti.let { notification ->
                Glide.with(context).load(notification.initiator?.imageUrl)
                    .error(R.drawable.ic_podcast_listening).into(holder.profilePic)
                when (notification.notificationType) {
                    "newFollower" -> holder.profileIcon.setImageResource(imageicon[0])
                    "podcastComment" -> holder.profileIcon.setImageResource(imageicon[1])
                    "replyComment" -> holder.profileIcon.setImageResource(imageicon[1])
                    "podcastLike" -> holder.profileIcon.setImageResource(imageicon[3])
                    "commentLike" -> holder.profileIcon.setImageResource(imageicon[3])
                    "recast" -> holder.profileIcon.setImageResource(imageicon[2])
                    "mention" -> holder.profileIcon.setImageResource(imageicon[1])
                    "friendJoined" -> holder.profileIcon.setImageResource(imageicon[0])
                    "followSuggestion" -> holder.profileIcon.setImageResource(imageicon[0])
                    "engagement" -> holder.profileIcon.setImageResource(imageicon[0])
                }
                holder.title.text = notification.message
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


                holder.bind(this, position, notificationsList, noti)
            }
        }


    }

    override fun getItemCount(): Int {
        return notificationsList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            notificationAdapter: NotificationAdapter,
            position: Int,
            notificationsList: java.util.ArrayList<NotiUIMode>,
            noti: NotiUIMode
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
                        "user" -> notificationAdapter.userCallback.invoke(
                            noti.redirectTarget.id,
                            noti.initiator?.username
                        )
                        "comment" -> notificationAdapter.userCallback.invoke(
                            noti.redirectTarget.id,
                            noti.initiator?.username
                        )
                        else -> Timber.d("Unable to handle this type")
                    }
                    notificationAdapter.updateRead(position)
                }

            }

            profilePic.setOnClickListener {
                if (!App.instance.merlinsBeard!!.isConnected) {
                    notificationAdapter.noInternetAlertCallback.invoke()
                } else {
                    //User Profile Activity
                    notificationAdapter.userCallback.invoke(
                        noti.initiator?.userId,
                        noti.initiator?.username
                    )
                }
            }
        }

        var profilePic: CircleImageView = itemView.findViewById(R.id.iv_user)
        var profileIcon: CircleImageView = itemView.findViewById(R.id.circleImageView)
        var title: TextView = itemView.findViewById(R.id.tv_title)
        var subTitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        var mainBg: ConstraintLayout = itemView.findViewById(R.id.main_notification_bg)


    }

    private fun updateRead(position: Int) {
        notificationsList[position] = notificationsList[position].copy(read = true)
        notifyItemChanged(position)
    }

    fun addItems(it: List<NotiUIMode>) {
        notificationsList.clear()
        notificationsList.addAll(it)
        notifyDataSetChanged()
    }

    fun openCastCallback(callback: (castId: Int?) -> Unit) {
        this.castCallback = callback
    }

    fun addUserTypeCallback(callback: (userId: Int?, username: String?) -> Unit) {
        this.userCallback = callback
    }

    fun addNotificationReadListener(callback: (nId: Int?, read: Boolean) -> Unit) {
        this.notiReadCallback = callback
    }

    fun addNoInternetAlertCallback(callback: () -> Unit) {
        this.noInternetAlertCallback = callback
    }

}




