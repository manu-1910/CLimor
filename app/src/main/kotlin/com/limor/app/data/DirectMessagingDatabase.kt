package com.limor.app.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.limor.app.common.Constants

abstract class DirectMessagingDatabase : RoomDatabase() {

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