package io.square1.limor.scenes.main.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.uimodels.UIDraft

class DraftAdapter(
    private val draftList: ArrayList<UIDraft>,
    private val listener: OnItemClickListener?,
    private val secondaryInformationListener: OnSecondaryInformationListenerClickListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DraftViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val centreViewHolder: DraftViewHolder = holder as DraftViewHolder
        val uiCentre: UIDraft = draftList[position]
        centreViewHolder.bind(uiCentre, secondaryInformationListener, position)
        centreViewHolder.itemView.setOnClickListener {
            listener?.onItemClick(
                draftList[position], position)
        }
    }

    override fun getItemCount(): Int = draftList.size

    interface OnItemClickListener {
        fun onItemClick(item: UIDraft, position: Int)
    }

    interface OnSecondaryInformationListenerClickListener {
        fun onSecondaryInformationClick(item: UIDraft, position: Int)
    }
}
