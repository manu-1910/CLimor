package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.scenes.main.viewmodels.NotificationsViewModel
import com.limor.app.uimodels.UINotificationItem

const val VIEW_TYPE_HEADER = 0
const val VIEW_TYPE_NOTIFICATION = 1

class NotificationsAdapter(
    var context: Context,
    list: ArrayList<NotificationsViewModel.Notification>,
    private val notificationClickListener: OnNotificationClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<NotificationsViewModel.Notification> = ArrayList()

    init {
        this.list = list
        inflator = LayoutInflater.from(context)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            NotificationHeaderViewHolder(inflator, parent)
        } else {
            NotificationItemViewHolder(inflator, parent, notificationClickListener, context)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].isHeader) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_NOTIFICATION
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            val headerViewHolder: NotificationHeaderViewHolder =
                holder as NotificationHeaderViewHolder
            headerViewHolder.bind(currentItem.headerText)
        } else {
            val notificationItemViewHolder: NotificationItemViewHolder =
                holder as NotificationItemViewHolder
            currentItem.notificationItem?.let { notificationItemViewHolder.bind(it, position) }
        }

    }

    interface OnNotificationClicked {
        fun onNotificationClicked(item: UINotificationItem, position: Int)
        fun onFollowClicked(item: UINotificationItem, position: Int)
    }
}