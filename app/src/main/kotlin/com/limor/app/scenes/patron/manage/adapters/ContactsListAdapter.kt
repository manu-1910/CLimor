package com.limor.app.scenes.patron.manage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.uimodels.ContactUIModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_contact_list.view.*

class ContactsListAdapter(
    private val contacts: ArrayList<ContactUIModel>,
    private val onSelected: (id: Int) -> Unit
) : RecyclerView.Adapter<ContactsListAdapter.ContactViewHolder>() {

    val selectedContacts = ArrayList<ContactUIModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact_list, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.name.text = contacts[position].contactName
        Glide.with(holder.avatar)
            .load(contacts[position].profilePhoto)
            .placeholder(R.drawable.ic_podcast_listening)
            .into(holder.avatar)
        holder.selector.setImageDrawable(
            if (selectedContacts.find { it.id == contacts[position].id } != null) holder.rootView.context.getDrawable(
                R.drawable.ic_selected_checkbox
            ) else holder.rootView.context.getDrawable(R.drawable.ic_unselected_checkbox)
        )
        holder.rootView.setOnClickListener {
            if (contacts[position].checked == false) {
                if (selectedContacts.size < 5) {
                    selectedContacts.add(contacts[position])
                    holder.selector.setImageDrawable(holder.rootView.context.getDrawable(R.drawable.ic_selected_checkbox))
                    contacts[position].checked = true
                }
            } else {
                selectedContacts.removeIf { it.id == contacts[position].id }
                holder.selector.setImageDrawable(holder.rootView.context.getDrawable(R.drawable.ic_unselected_checkbox))
                contacts[position].checked = false
            }
            onSelected(selectedContacts.size)
        }
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    fun setContacts(contactsList: List<ContactUIModel>) {
        contactsList.onEach {
            val contact = selectedContacts.filter { uiModel -> it.id == uiModel.id }.firstOrNull()
            contact?.let { model ->
                it.checked = model.checked
            }
        }
        contacts.clear()
        contacts.addAll(contactsList)
        notifyDataSetChanged()
    }

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: CircleImageView = view.findViewById(R.id.iv_user)
        val name: TextView = view.findViewById(R.id.contact_name_text_view)
        val selector: ImageView = view.findViewById(R.id.select_contact_checkbox)
        val rootView: ConstraintLayout = view.findViewById(R.id.parent_layout)
    }

}