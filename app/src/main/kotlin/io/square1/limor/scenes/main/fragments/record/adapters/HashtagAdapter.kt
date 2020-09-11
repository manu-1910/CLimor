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
