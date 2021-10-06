package com.limor.app.data

import androidx.room.Embedded

data class ChatSessionWithUser(
    @Embedded val session: ChatSession,
    @Embedded val user: ChatUser
)