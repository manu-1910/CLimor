package io.square1.limor.scenes.main.fragments.record.adapters

//import android.content.Context
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import android.widget.Filter
//import android.widget.TextView
//import io.square1.limor.R
//import java.util.*
//
//
//class TagsAdapter(
//    private val mContext: Context,
//    private val itemLayout: Int,
//    private var dataList: List<String>?
//) :
//    ArrayAdapter<Any?>(mContext, itemLayout, dataList) {
//    private val listFilter: ListFilter = ListFilter()
//    var dataListAllItems: List<String>? = null
//
//
//    override fun getCount(): Int {
//        return dataList!!.size
//    }
//
//    override fun getItem(position: Int): String? {
////        Log.d(
////            "TagsAdapter",
////            dataList!![position]
////        )
//        return dataList!![position]
//    }
//
//    override fun getView(position: Int, view: View, parent: ViewGroup): View {
//        var view = view
//        if (view == null) {
//            view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.tag_item, parent, false)
//
//        }
//        val strName = view.findViewById<View>(R.id.tvHashtag) as TextView
//        strName.text = getItem(position)
//        return view
//    }
//
//    override fun getFilter(): Filter {
//        return listFilter
//    }
//
//    inner class ListFilter : Filter() {
//        private val lock:Object = Object()
//
//        override fun performFiltering(prefix: CharSequence): FilterResults {
//            val results = FilterResults()
//            if (dataListAllItems == null) {
//                synchronized(lock) {
//                    dataListAllItems = ArrayList(dataList)
//                }
//            }
//            if (prefix == null || prefix.length == 0) {
//                synchronized(lock) {
//                    results.values = dataListAllItems
//                    results.count = dataListAllItems!!.size
//                }
//            } else {
//                val searchStrLowerCase = prefix.toString().toLowerCase()
//                val matchValues = ArrayList<String>()
//                for (dataItem in dataListAllItems!!) {
//                    if (dataItem.toLowerCase().startsWith(searchStrLowerCase)) {
//                        matchValues.add(dataItem)
//                    }
//                }
//                results.values = matchValues
//                results.count = matchValues.size
//            }
//            return results
//        }
//
//        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
//            if (results.values != null) {
//                dataList = results.values as ArrayList<String>
//            } else {
//                dataList = ArrayList<String>()
//            }
//            if (results.count > 0) {
//                notifyDataSetChanged()
//            } else {
//                notifyDataSetInvalidated()
//            }
//        }
//
//    }
//}



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.square1.limor.R
import io.square1.limor.uimodels.UITags


class TagsAdapter (
    private val tagsList: ArrayList<UITags>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<TagsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val tvHashtag: TextView = itemView.findViewById(R.id.text1)

    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.tag_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return tagsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //older.tvHashtag.text = "#"+tagsList[position].text
        holder.itemView.setOnClickListener { listener.onItemClick(tagsList[position]) }
    }

    interface OnItemClickListener {
        fun onItemClick(item: UITags)
    }
}