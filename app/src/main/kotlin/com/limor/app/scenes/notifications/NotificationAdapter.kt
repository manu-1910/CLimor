package com.limor.app.scenes.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.ViewHolder>(){

    private val pic = arrayOf(
        R.drawable.pic_three,
        R.drawable.pic_two,
        R.drawable.pic_one,
        R.drawable.pic_four,
        R.drawable.pic_five,
        R.drawable.pic_six
    )
    private val imageicon = arrayOf(
        R.drawable.ic_comment,
        R.drawable.ic_comment,
        R.drawable.ic_comment,
        R.drawable.ic_comment,
        R.drawable.ic_comment,
        R.drawable.ic_comment
    )
    private val title = arrayOf("Arina commented on your podcast","Harry commented on your podcast","Mr. Smith commented on your podcast","Angelina commented on your podcast","John sent you a message","Anjali sent you a message")
    private val time = arrayOf("2 Minutes Ago", "5 Minutes Ago","10 Minutes Ago", "1 Hour Ago", "2 Hours Ago", "5 Hours Ago")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
     val v = LayoutInflater.from(parent.context).inflate(R.layout.notification_item_new,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.profilePic.setImageResource(pic[position])
        holder.profileIcon.setImageResource(imageicon[position])
        holder.title.text = title[position]
        holder.subTitle.text = time[position]

    }

    override fun getItemCount(): Int {
     return pic.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var profilePic : CircleImageView
        var profileIcon : CircleImageView
        var title : TextView
        var subTitle : TextView

        init {
            profilePic =  itemView.findViewById(R.id.iv_user)
            profileIcon = itemView.findViewById(R.id.circleImageView)
            title = itemView.findViewById(R.id.tv_title)
            subTitle =  itemView.findViewById(R.id.tv_subtitle)

            itemView.setOnClickListener {
                var position: Int = adapterPosition
                var context =  itemView.context

            }
        }

        
    }

}




