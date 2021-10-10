package com.limor.app.dm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow


@Dao
// for now only use one DAO for all DB access as the 3 models are tightly interrelated
interface ChatDao {

    @Query(
        "SELECT * FROM chat_session " +
                "INNER JOIN user ON user.id = chat_session.chat_user_id"
    )
    fun getSessions(): Flow<List<ChatSessionWithUser>>

    @Transaction
    @Query(
        "SELECT * FROM chat_session " +
                "INNER JOIN user ON user.id = chat_session.chat_user_id " +
                "WHERE chat_session.id = :sessionId LIMIT 1"
    )
    fun getChat(sessionId: Int): Flow<ChatWithData>

    @Query(
        "SELECT * from chat_session " +
                "INNER JOIN user ON user.id = chat_session.chat_user_id " +
                "WHERE user.chat_id = :chatId LIMIT 1"
    )
    fun getSessionByUserChatId(chatId: String): ChatSession

    @Insert
    fun insertMessage(chatMessage: ChatMessage)
}