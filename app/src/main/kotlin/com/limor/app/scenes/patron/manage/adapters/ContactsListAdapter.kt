package com.limor.app.scenes.patron.manage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.uimodels.ContactUIModel
import de.hdodenhof.circleimageview.CircleImageView

class ContactsListAdapter(
    private val contacts: ArrayList<ContactUIModel>/*,
    private val onSelected: (like: Boolean) -> Unit*/
) : RecyclerView.Adapter<ContactsListAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact_list, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.name.text = contacts.get(position).contactName
        Glide.with(holder.avatar)
            .load(contacts.get(position).profilePhoto)
            .placeholder(R.color.dark_transparent)
            .into(holder.avatar)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    fun setContacts(contactsList: List<ContactUIModel>){
        contacts.clear()
        contacts.addAll(contactsList)
        notifyDataSetChanged()
    }

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: CircleImageView = view.findViewById(R.id.iv_user)
        val name: TextView = view.findViewById(R.id.contact_name_text_view)
    }

}

class ContactsDiffCallback : DiffUtil.ItemCallback<ContactUIModel>() {
    override fun areItemsTheSame(
        oldItem: ContactUIModel,
        newItem: ContactUIModel
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: ContactUIModel,
        newItem: ContactUIModel
    ): Boolean {
        return oldItem == newItem
    }
}