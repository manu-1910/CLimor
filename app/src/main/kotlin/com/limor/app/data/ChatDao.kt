package com.limor.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction


@Dao
// for now only use one DAO for all DB access as the 3 models are tightly interrelated
interface ChatDao {

    @Query(
        "SELECT * FROM chat_session " +
                "INNER JOIN user ON user.id = chat_session.chat_user_id"
    )
    fun getSessions(): List<ChatSessionWithUser>

    @Transaction
    @Query(
        "SELECT * FROM chat_session " +
                "INNER JOIN user ON user.id = chat_session.chat_user_id " +
                "WHERE chat_session.id LIKE :sessionId LIMIT 1"
    )
    fun getChat(sessionId: Int): ChatWithData

    @Insert
    fun insertMessage(chatMessage: ChatMessage)
}