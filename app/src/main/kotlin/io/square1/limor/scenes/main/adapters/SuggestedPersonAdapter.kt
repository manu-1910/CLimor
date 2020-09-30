package io.square1.limor.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.uimodels.UIUser

class SuggestedPersonAdapter(
    var context: Context,
    list: ArrayList<UIUser>,
    private val personClickListener: OnPersonClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<UIUser> = ArrayList()

    init {
        this.list = list
        inflator = LayoutInflater.from(context)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SuggestedPersonViewHolder(inflator, parent, personClickListener, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        val personViewHolder: SuggestedPersonViewHolder =
            holder as SuggestedPersonViewHolder
        personViewHolder.bind(currentItem, position)

    }

    interface OnPersonClicked {
        fun onPersonClicked(item: UIUser, position: Int)
    }
}