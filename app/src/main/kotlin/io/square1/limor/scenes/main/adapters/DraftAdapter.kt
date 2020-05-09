package io.square1.limor.scenes.main.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.R
import io.square1.limor.uimodels.UIDraft
import org.jetbrains.anko.sdk23.listeners.onClick
import java.util.*

class DraftAdapter(
    private val draftsList: ArrayList<UIDraft>,
    private val listener: OnItemClickListener,
    private val deleteListener: OnDeleteItemClickListener
) : RecyclerView.Adapter<DraftAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //Item components
        val tvDraftTitle: TextView = itemView.findViewById(R.id.tvDraftTitle)
        val tvDraftDescription: TextView = itemView.findViewById(R.id.tvDraftDescription)
        val ivDraftDelete: ImageView = itemView.findViewById(R.id.ivDraftDelete)

    }


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): DraftAdapter.ViewHolder {
        return DraftAdapter.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.fragment_drafts_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return draftsList.size
    }

    override fun onBindViewHolder(holder: DraftAdapter.ViewHolder, position: Int) {

        holder.tvDraftTitle.text = draftsList[position].title
        holder.tvDraftDescription.text = draftsList[position].caption
        holder.itemView.setOnClickListener { listener.onItemClick(draftsList[position]) }

        if (draftsList[position].isEditMode!!) {
            holder.ivDraftDelete.setImageResource(R.drawable.delete_symbol)
            holder.ivDraftDelete.onClick { deleteListener.onDeleteItemClick(position) }
        } else {
            holder.ivDraftDelete.setImageResource(android.R.color.transparent)
        }

    }


    interface OnItemClickListener {
        fun onItemClick(item: UIDraft)
    }

    interface OnDeleteItemClickListener {
        fun onDeleteItemClick(position: Int)
    }

}


