package com.limor.app.dm

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "chat_session",
    foreignKeys = [
        ForeignKey(entity = ChatUser::class, parentColumns = ["user_id"], childColumns = ["chat_user_id"])
    ],
)
data class ChatSession(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "session_id")
    var id: Int = 0,

    @ColumnInfo(name = "chat_user_id", index = true)
    val chatUserId: Int,

    @ColumnInfo(name = "last_message_timestamp")
    var lastMessageDate: Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "last_message_content")
    var lastMessageContent: String,

    @ColumnInfo(name = "draft_content")
    var draftContent: String = "",

    @ColumnInfo(name = "unread_count")
    var unreadCount: Int?,

    @ColumnInfo(name = "last_read_message_id")
    val lastReadMessageId: Int?

)