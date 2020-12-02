package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronTiersFragment

class TiersAdapter(
    var context: Context,
    var list: ArrayList<SetupPatronTiersFragment.Tier>,
    private val tierClickedListener: OnTierClickedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater = LayoutInflater.from(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TiersViewHolder(inflator, parent, tierClickedListener, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]
        val viewHolder: TiersViewHolder = holder as TiersViewHolder
        viewHolder.bind(currentItem, position)
    }

    interface OnTierClickedListener {
        fun onTierClicked(item: SetupPatronTiersFragment.Tier, position: Int)
        fun onEditTierClicked(item: SetupPatronTiersFragment.Tier, position: Int)
        fun onRemoveTierClicked(currentItem: SetupPatronTiersFragment.Tier, position: Int)
    }
}