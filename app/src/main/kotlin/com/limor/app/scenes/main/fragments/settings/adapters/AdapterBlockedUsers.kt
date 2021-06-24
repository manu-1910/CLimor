package com.limor.app.scenes.main.fragments.settings.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.uimodels.UIUser
import org.jetbrains.anko.layoutInflater

class AdapterBlockedUsers(var list: ArrayList<GetBlockedUsersQuery.GetBlockedUser?>, val listener: OnFollowerClickListener)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {





    interface OnFollowerClickListener {
        fun onUserClicked(item:  GetBlockedUsersQuery.GetBlockedUser, position: Int)
        fun onFollowClicked(item:  GetBlockedUsersQuery.GetBlockedUser, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolderBlockedUser(parent = parent,inflater = parent.context.layoutInflater,
                listener = listener,context = parent.context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
       return list.size
    }
}