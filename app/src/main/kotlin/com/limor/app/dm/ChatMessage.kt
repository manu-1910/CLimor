package com.limor.app.dm

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "chat_message",
    foreignKeys = [
        ForeignKey(entity = ChatSession::class, parentColumns = ["session_id"], childColumns = ["chat_session_id"]),
        ForeignKey(entity = ChatUser::class, parentColumns = ["user_id"], childColumns = ["chat_user_id"]),
    ],
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "message_id") val id: Int = 0,
    @ColumnInfo(name = "chat_session_id", index = true) val chatSessionId: Int,
    @ColumnInfo(name = "chat_user_id", index = true) val chatUserId: Int?,
    @ColumnInfo(name = "message_content") val messageContent: String,
    @ColumnInfo(name = "timestamp") val date: Calendar = Calendar.getInstance()
)