package com.limor.app.dm

import androidx.room.Embedded
import androidx.room.Relation

data class ChatWithData(
    @Embedded val sessionWithUser: ChatSessionWithUser,

    @Relation(
        parentColumn = "session_id",
        entityColumn = "chat_session_id"
    )
    val messages: List<ChatMessage>
)