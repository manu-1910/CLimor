package com.limor.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.limor.app.common.Constants

@Database(entities = [ChatUser::class, ChatMessage::class, ChatSession::class], version = 1)
abstract class DirectMessagingDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

    companion object {

        @Volatile
        private var instance: DirectMessagingDatabase? = null

        fun getInstance(context: Context): DirectMessagingDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): DirectMessagingDatabase {
            return Room.databaseBuilder(
                context,
                DirectMessagingDatabase::class.java,
                Constants.DIRECT_MESSAGING_DB_NAME
            ).build()
        }
    }
}