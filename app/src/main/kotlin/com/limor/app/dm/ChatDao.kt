package com.limor.app.dm

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
// for now only use one DAO for all DB access as the 3 models are tightly interrelated
interface ChatDao {

    @Query(
        "SELECT * FROM chat_session INNER JOIN chat_users ON chat_users.user_id = chat_session.chat_user_id where chat_session.session_id = :sessionId"
    )
    fun getSessionWithUserId(sessionId: Int): ChatSessionWithUser

    @Query(
        """
            SELECT * FROM chat_session 
                INNER JOIN chat_users ON chat_users.user_id = chat_session.chat_user_id
            ORDER BY last_message_timestamp DESC
        """
    )
    fun getSessions(): Flow<List<ChatSessionWithUser>>

    @Transaction
    @Query(
        "SELECT * FROM chat_session " +
                "INNER JOIN chat_users ON chat_users.user_id = chat_session.chat_user_id " +
                "WHERE chat_session.session_id = :sessionId LIMIT 1"
    )
    fun getChat(sessionId: Int): Flow<ChatWithData>

    @Query(
        "SELECT * from chat_session " +
                "INNER JOIN chat_users ON chat_users.user_id = chat_session.chat_user_id " +
                "WHERE chat_users.limor_user_id = :limorUserId LIMIT 1"
    )
    fun getSessionByLimorUserId(limorUserId: Int): ChatSession?

    @Query("SELECT * from chat_users where chat_users.limor_user_id = :limorUserId LIMIT 1")
    fun getChatUserByLimorId(limorUserId: Int): ChatUser?

    @Query("""
        SELECT * FROM chat_session 
            INNER JOIN chat_users ON chat_users.user_id = chat_session.chat_user_id 
            WHERE
                LOWER(chat_users.limor_user_name)  LIKE '%' || :term || '%' OR
                LOWER(chat_users.limor_display_name)  LIKE '%' || :term || '%'
        """)
    suspend fun searchSession(term: String): List<ChatSessionWithUser>

    @Query("""
        SELECT count(*) from chat_message 
        WHERE 
            chat_session_id = :sessionId 
            and 
            message_id > :lastReadMessageId
            and 
            chat_user_id is not null
        """)
    fun getUnreadCount(sessionId: Int, lastReadMessageId: Int): Int

    @Insert
    fun insertMessage(chatMessage: ChatMessage): Long

    @Insert
    fun insertSession(chatSession: ChatSession): Long

    @Insert
    fun insertChatUser(chatUser: ChatUser): Long

    @Update
    fun updateSession(session: ChatSession)

}