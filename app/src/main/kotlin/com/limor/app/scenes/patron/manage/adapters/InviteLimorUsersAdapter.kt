package com.limor.app.scenes.patron.manage.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UserUIModel
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.design.snackbar
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

class InviteLimorUsersAdapter(
    private val users: ArrayList<UserUIModel>,
    private val onSelected: (id: UserUIModel) -> Unit,
) : RecyclerView.Adapter<InviteLimorUsersAdapter.LimorUserViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LimorUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_limor_user, parent, false)
        return LimorUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: LimorUserViewHolder, position: Int) {
        holder.accountName.text = users[position].getFullName()
        holder.accountNickName.text = users[position].username

        Log.d("INVITE", "-> ${users[position].patronStatus}")

        when (users[position].patronStatus) {
            "JOINED" -> {
                holder.markJoined(users[position])
            }

            null -> {
                holder.shouldInvite(users[position], onSelected, this, position)
            }

            "INVITE" -> {
                holder.shouldInvite(users[position], onSelected, this, position)
            }
            "REMINDER" -> {
                holder.shouldInviteWithoutCountChange(users[position], onSelected, this, position)
            }
            "ALREADY_INVITED" -> {
                holder.alreadyInvited()
            }
            "NOT_ELIGIBLE" -> {
                holder.isNotEligible()
            }

        }

        Glide.with(holder.avatar)
            .load(users[position].imageLinks?.medium)
            .placeholder(R.drawable.ic_podcast_listening)
            .into(holder.avatar)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun setUsers(contactsList: List<UserUIModel>) {
        users.clear()
        users.addAll(contactsList)
        notifyDataSetChanged()
    }

    class LimorUserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun markJoined(userUIModel: UserUIModel) {
            inviteButton.text = accountName.context.getString(R.string.joined)
            inviteButton.isEnabled = false
            inviteButton.backgroundColor = ContextCompat.getColor(
                accountName.context,
                R.color.main_button_background_follow
            )
        }

        fun shouldInvite(
            userUIModel: UserUIModel,
            onSelected: (id: UserUIModel) -> Unit,
            adapter: InviteLimorUsersAdapter,
            position: Int,
        ) {
            inviteButton.text = accountName.context.getString(R.string.invite)
            inviteButton.isEnabled = true
            inviteButton.backgroundColor = ContextCompat.getColor(
                accountName.context,
                R.color.colorAccent
            )
            inviteButton.setOnClickListener {
                val count = CommonsKt.user?.availableInvitations ?: 0
                if (count > 0) {
                    userUIModel.patronStatus = "JOINED"
                    adapter.updateItemAt(position, userUIModel)
                    CommonsKt.user?.availableInvitations = if(count-1 < 0) 0 else count-1
                    onSelected(userUIModel)
                } else {
                    inviteButton.snackbar("No More invites left!")
                }
            }
            if((CommonsKt.user?.availableInvitations ?: 0) == 0){
                inviteButton.isEnabled = false
                inviteButton.backgroundColor = ContextCompat.getColor(
                    accountName.context,
                    R.color.main_button_background_follow
                )
            }
        }

        fun isNotEligible(){
            inviteButton.text = accountName.context.getString(R.string.not_eligible)
            inviteButton.isEnabled = false
        }

        fun alreadyInvited() {
            inviteButton.isEnabled = false
            inviteButton.text = "Invited"
            inviteButton.backgroundColor = ContextCompat.getColor(
                accountName.context,
                R.color.main_button_background_follow
            )
        }

        fun shouldInviteWithoutCountChange(
            userUIModel: UserUIModel,
            onSelected: (id: UserUIModel) -> Unit,
            inviteLimorUsersAdapter: InviteLimorUsersAdapter,
            position: Int
        ) {
            inviteButton.text = accountName.context.getString(R.string.reminder)
            inviteButton.backgroundColor = ContextCompat.getColor(
                accountName.context,
                R.color.colorAccent
            )
            inviteButton.setOnClickListener {
                onSelected(userUIModel)
                inviteButton.snackbar("Reminder sent")
            }
            if((CommonsKt.user?.availableInvitations ?: 0) == 0){
                inviteButton.isEnabled = false
                inviteButton.backgroundColor = ContextCompat.getColor(
                    accountName.context,
                    R.color.main_button_background_follow
                )
            }
        }

        val avatar: CircleImageView = view.findViewById(R.id.account_image)
        val accountName: TextView = view.findViewById(R.id.account_name)
        val accountNickName: TextView = view.findViewById(R.id.account_nickname)
        val inviteButton: Button = view.findViewById(R.id.invite_button)
    }

    private fun updateItemAt(position: Int, userUIModel: UserUIModel) {
        users[position] = userUIModel
        notifyItemChanged(position)
    }
}