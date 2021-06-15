package com.limor.app.scenes.main.fragments.record.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.uimodels.UILocations


class LocationsAdapter(
    private val locationsList: ArrayList<UILocations>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<LocationsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLocations: TextView = itemView.findViewById(R.id.tvLocation)
        val ivSelected: ImageView = itemView.findViewById(R.id.ivSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.locations_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return locationsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvLocations.text = locationsList[position].address
       /* if (locationsList[position].isSelected) {
            holder.ivSelected.visibility = View.VISIBLE
        } else {
            holder.ivSelected.visibility = View.INVISIBLE
        }*/
        holder.itemView.setOnClickListener { listener.onItemClick(locationsList[position]) }
    }

    interface OnItemClickListener {
        fun onItemClick(item: UILocations)
    }
}