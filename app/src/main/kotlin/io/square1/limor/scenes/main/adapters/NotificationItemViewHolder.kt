package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.square1.limor.R
import io.square1.limor.scenes.utils.Commons
import io.square1.limor.uimodels.UINotificationItem
import org.jetbrains.anko.sdk23.listeners.onClick

const val NOTIFICATION_TYPE_GENERAL = "general"  //Not sure if we need this?
const val NOTIFICATION_TYPE_FOLLOW = "follow"
const val NOTIFICATION_TYPE_MENTION = "mention" //Not sure
const val NOTIFICATION_TYPE_PODCAST_BOOKMARK_SHARE = "podcast_bookmark_share"
const val NOTIFICATION_TYPE_PODCAST_LIKE = "podcast_like"
const val NOTIFICATION_TYPE_PODCAST_RECAST = "podcast_recast"
const val NOTIFICATION_TYPE_PODCAST_COMMENT = "podcast_comment"
const val NOTIFICATION_TYPE_COMMENT_LIKE = "comment_like"
const val NOTIFICATION_TYPE_AD_COMMENT = "ad_comment" //Not sure
const val NOTIFICATION_TYPE_CONVERSATION_REQUEST = "conversation_request" //Not needed now
const val NOTIFICATION_TYPE_CONVERSATION_PARTICIPANT = "conversation_participant" //Not needed now
const val NOTIFICATION_TYPE_MESSAGE_SENT = "message_sent" //Not needed now
const val NOTIFICATION_TYPE_COMMENT_COMMENT = "comment_comment"
const val NOTIFICATION_TYPE_FACEBOOK_FRIEND = "facebook_friend"  //Not needed now

class NotificationItemViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private val notificationClickListener: NotificationsAdapter.OnNotificationClicked,
    private val context: Context
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.notification_item,
        parent,
        false
    )
) {

    private var btnFollow: Button = itemView.findViewById(R.id.btn_follow)
    private var tvTime: TextView = itemView.findViewById(R.id.tv_time)
    private var tvTitle: TextView = itemView.findViewById(R.id.tv_title)
    private var tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
    private var ivUser: ImageView = itemView.findViewById(R.id.iv_user)
    private var rlRoot: RelativeLayout = itemView.findViewById(R.id.rl_root_view)

    fun bind(currentItem: UINotificationItem, position: Int) {

        rlRoot.onClick { notificationClickListener.onNotificationClicked(currentItem, position) }

        btnFollow.visibility = View.GONE
        tvSubtitle.visibility = View.VISIBLE
        tvSubtitle.text = currentItem.message
        tvTitle.visibility = View.VISIBLE

        val hourMinutes = Commons.getHourMinutesFromDateString(currentItem.createdAt)
        tvTime.text = hourMinutes

        when (currentItem.notificationType) {

            NOTIFICATION_TYPE_GENERAL -> {
                handleStandardNotification(currentItem)
            }

            NOTIFICATION_TYPE_FOLLOW -> {
                tvTitle.text = currentItem.resources.owner.username
                btnFollow.visibility = View.VISIBLE

                if(currentItem.resources.owner.followed){
                    btnFollow.background = ContextCompat.getDrawable(context, R.drawable.bg_round_brand_500_ripple)
                    btnFollow.setTextColor(ContextCompat.getColor(context, R.color.white))
                    btnFollow.text = context.getString(R.string.unfollow)
                }else{
                    btnFollow.background = ContextCompat.getDrawable(context, R.drawable.bg_round_yellow_ripple)
                    btnFollow.setTextColor(ContextCompat.getColor(context, R.color.black))
                    btnFollow.text = context.getString(R.string.follow)
                }

                btnFollow.onClick {
                    notificationClickListener.onFollowClicked(
                        currentItem,
                        position
                    )
                }
                loadOwnerImage(currentItem)
            }

            NOTIFICATION_TYPE_MENTION -> {
                handleStandardNotification(currentItem)
            }

            NOTIFICATION_TYPE_PODCAST_BOOKMARK_SHARE -> {
                handleStandardNotification(currentItem)
            }

            NOTIFICATION_TYPE_PODCAST_LIKE -> {
                handleStandardNotification(currentItem)
            }

            NOTIFICATION_TYPE_PODCAST_RECAST -> {
                handleStandardNotification(currentItem)
            }

            NOTIFICATION_TYPE_COMMENT_LIKE -> {
                handleStandardNotification(currentItem)
            }

            NOTIFICATION_TYPE_AD_COMMENT -> {
                handleStandardNotification(currentItem)
            }

            NOTIFICATION_TYPE_CONVERSATION_REQUEST -> {
                handleStandardNotification(currentItem)
            }

            NOTIFICATION_TYPE_CONVERSATION_PARTICIPANT -> {

                tvTitle.visibility = View.GONE

                Glide.with(context)
                    .load(currentItem.resources.images.small_url)
                    .placeholder(R.drawable.hashtag)
                    .circleCrop()
                    .into(ivUser)
            }

            NOTIFICATION_TYPE_MESSAGE_SENT -> {
                handleStandardNotification(currentItem)
            }

            NOTIFICATION_TYPE_PODCAST_COMMENT -> {
                handleStandardNotification(currentItem)
            }


            NOTIFICATION_TYPE_COMMENT_COMMENT -> {
                handleStandardNotification(currentItem)
            }

            NOTIFICATION_TYPE_FACEBOOK_FRIEND -> {
                handleStandardNotification(currentItem)
            }


        }

    }

    private fun handleStandardNotification(currentItem: UINotificationItem) {
        tvTitle.text = currentItem.resources.owner.username
        tvTitle.visibility = View.VISIBLE
        loadOwnerImage(currentItem)
    }

    private fun loadOwnerImage(currentItem: UINotificationItem) {
        Glide.with(context)
            .load(currentItem.resources.owner.images.small_url)
            .placeholder(R.drawable.hashtag)
            .circleCrop()
            .into(ivUser)
    }

}