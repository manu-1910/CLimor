package io.square1.limor.scenes.main.fragments.record.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hendraanggrian.appcompat.widget.Hashtag
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter
import io.square1.limor.R


@SuppressLint("ResourceType")
class HashtagAdapter(context: Context) : HashtagArrayAdapter<Hashtag>(context, R.layout.tag_item) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        var view = convertView
        when (view) {
            null -> {
                view = LayoutInflater.from(context).inflate(R.layout.tag_item, parent, false)
                holder = ViewHolder(view!!)
                view.tag = holder
            }
            else -> holder = view.tag as ViewHolder
        }
        getItem(position)?.let { model -> holder.textView.text = model.id }
        return view
    }

    private class ViewHolder(view: View) {
        val textView: TextView = view.findViewById(R.id.text1)
    }
}

//import android.graphics.Typeface
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import io.square1.limor.R
//import io.square1.limor.uimodels.UITags
//
//
//class HashtagsAdapter(
//    private val tagsList: ArrayList<UITags>,
//    private val listener: OnItemClickListener
//) :
//    RecyclerView.Adapter<HashtagsAdapter.ViewHolder>() {
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvHashtag: TextView = itemView.findViewById(R.id.tvHashtag)
//        val ivSelected: ImageView = itemView.findViewById(R.id.ivSelected)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
//        return ViewHolder(
//            LayoutInflater.from(parent.context).inflate(
//                R.layout.hashtag_item,
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun getItemCount(): Int {
//        return tagsList.size
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.tvHashtag.text = "#"+tagsList[position].text
//        if (tagsList[position].isSelected) {
//            //holder.tvHashtag.setTypeface(null, Typeface.BOLD)
//            holder.ivSelected.visibility = View.VISIBLE
//        } else {
//            //holder.tvHashtag.setTypeface(null, Typeface.NORMAL)
//            holder.ivSelected.visibility = View.INVISIBLE
//        }
//        holder.itemView.setOnClickListener { listener.onItemClick(tagsList[position]) }
//    }
//
//    interface OnItemClickListener {
//        fun onItemClick(item: UITags)
//    }
//}