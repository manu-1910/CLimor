package com.limor.app.scenes.main_new.adapters.vh

import android.content.Context
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class ViewHolderBindable<T>(private val viewBinding: ViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {
    abstract fun bind(item: T)

    val context: Context
        get() = itemView.context

    fun getString(@StringRes int: Int): String = context.getString(int)

}