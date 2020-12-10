package com.limor.app.scenes.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.uimodels.UICategory
import org.jetbrains.anko.sdk23.listeners.onClick

class DiscoverMainCategoriesAdapter(
    var context: Context,
    list: ArrayList<UICategory>,
    private val categoryClickedListener: OnDiscoverMainCategoryClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var inflator: LayoutInflater
    var list: ArrayList<UICategory> = ArrayList()

    init {
        this.list = list
        inflator = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DiscoverMainCategoriesViewHolder(inflator, parent)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]

        val headerViewHolder: DiscoverMainCategoriesViewHolder =
            holder as DiscoverMainCategoriesViewHolder

        headerViewHolder.bind(currentItem.name)

        headerViewHolder.itemView.onClick {
            categoryClickedListener.onDiscoverCategoryClicked(
                currentItem,
                position
            )
        }
    }

    interface OnDiscoverMainCategoryClicked {
        fun onDiscoverCategoryClicked(item: UICategory, position: Int)
    }
}