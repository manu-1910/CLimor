package com.limor.app.dm

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(private val chatDao: ChatDao) {

    fun getSessions() = chatDao.getSessions()

    fun getChat(sessionId: Int) = chatDao.getChat(sessionId)

    private fun addMessage(chatMessage: ChatMessage) = chatDao.insertMessage(chatMessage)

    fun addOtherMessage(content: String, session: ChatSession) {
        ChatMessage(
            0,
            session.id,
            session.chatUserId,
            content
        ).also {
            addMessage(it)
        }
    }

    fun addMyMessage(content: String, session: ChatSession) {
        ChatMessage(
            0,
            session.id,
            null,
            content
        ).also {
            addMessage(it)
        }
    }

    fun getSessionByUserChatId(peerId: String): ChatSession? {
        if (peerId.isEmpty() || !peerId.contains('_')) {
            return null
        }
        val limorUserId = peerId.split('_').last().toInt()
        return chatDao.getSessionByLimorUserId(limorUserId)
    }

    fun insertSession(session: ChatSession) = chatDao.insertSession(session)
    fun insertChatUser(chatUser: ChatUser) = chatDao.insertChatUser(chatUser)
    fun getChatUserByLimorId(userId: Int) = chatDao.getChatUserByLimorId(userId)
}