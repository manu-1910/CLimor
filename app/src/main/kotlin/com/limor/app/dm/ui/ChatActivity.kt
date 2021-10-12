package com.limor.app.dm.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.limor.app.R
import dagger.android.AndroidInjection
import javax.inject.Inject

class ChatActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidInjection.inject(this)
        setContentView(R.layout.activity_chat)
    }

    companion object {
        const val KEY_LIMOR_USER_ID = "KEY_LIMOR_USER_ID"

        fun start(context: Context, limorUserId: Int) {
            Intent(context, ChatActivity::class.java).apply {
                putExtra(KEY_LIMOR_USER_ID, limorUserId)
            }.also {
                context.startActivity(it)
            }
        }
    }
}