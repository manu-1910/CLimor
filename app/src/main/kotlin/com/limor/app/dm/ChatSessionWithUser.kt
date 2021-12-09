package com.limor.app.dm

import androidx.room.Embedded

data class ChatSessionWithUser(
    @Embedded val session: ChatSession,
    @Embedded val user: ChatUser
)