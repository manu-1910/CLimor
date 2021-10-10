package com.limor.app.dm

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(private val chatDao: ChatDao) {

    fun getSessions() = chatDao.getSessions()

    fun getChat(sessionId: Int) = chatDao.getChat(sessionId)

    private fun addMessage(chatMessage: ChatMessage) = chatDao.insertMessage(chatMessage)

    fun addOtherMessage(content: String, otherChatId: String) {
        addOtherMessage(content, chatDao.getSessionByUserChatId(otherChatId))
    }

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
}