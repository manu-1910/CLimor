package com.limor.app.scenes.main_new.adapters.vh

import android.content.Context
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.limor.app.FeedItemsQuery

abstract class ViewHolderBindable(private val viewBinding: ViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {
    abstract fun bind(item: FeedItemsQuery.FeedItem)

    val context: Context
        get() = itemView.context

    fun getString(@StringRes int: Int): String = context.getString(int)

}