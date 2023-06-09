package com.limor.app.scenes.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R


class NotificationHeaderViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.notification_item_header,
        parent,
        false
    )
) {
    private var tvDay: TextView = itemView.findViewById(R.id.tv_date_header)

    fun bind(currentItem: String) {
        tvDay.text = currentItem
    }
}