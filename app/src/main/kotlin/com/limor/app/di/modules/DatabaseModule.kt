package com.limor.app.di.modules

import android.content.Context
import com.limor.app.dm.ChatDao
import com.limor.app.dm.DirectMessagingDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDirectMessagingDatabase(context: Context): DirectMessagingDatabase {
        return DirectMessagingDatabase.getInstance(context)
    }

    @Provides
    fun providePlantDao(appDatabase: DirectMessagingDatabase): ChatDao {
        return appDatabase.chatDao()
    }
}