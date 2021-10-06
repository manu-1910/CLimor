package com.limor.app.data

import androidx.room.Embedded
import androidx.room.Relation

data class ChatWithData(
    @Embedded val sessionWithUser: ChatSessionWithUser,

    @Relation(
        parentColumn = "id",
        entityColumn = "chat_session_id"
    )
    val messages: List<ChatMessage>
)