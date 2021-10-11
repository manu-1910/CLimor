package com.limor.app.dm

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_users")
data class ChatUser(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "user_id")  val id: Int = 0,
    @ColumnInfo(name = "limor_user_id") val limorUserId: Int,
    @ColumnInfo(name = "limor_profile_url") val limorProfileUrl: String?,
    @ColumnInfo(name = "limor_user_name") val limorUserName: String?,
    @ColumnInfo(name = "limor_display_name") val limorDisplayName: String?,
)