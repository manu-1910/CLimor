package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.uimodels.UIUser

class BlockedUsersAdapter(
    var context: Context,
    var list: ArrayList<UIUser>,
    private val blockedClickListener: OnBlockedUserClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater = LayoutInflater.from(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BlockedUserViewHolder(inflator, parent, blockedClickListener, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]
        val viewHolder: BlockedUserViewHolder = holder as BlockedUserViewHolder
        viewHolder.bind(currentItem, position)
    }

    interface OnBlockedUserClickListener {
        fun onUserClicked(item: UIUser, position: Int)
        fun onBlockClicked(item: UIUser, position: Int)
    }
}