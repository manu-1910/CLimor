package com.limor.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.limor.app.events.PhoneCallEvent
import org.greenrobot.eventbus.EventBus

class PhoneCallReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.PHONE_STATE") {
            val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (phoneState == TelephonyManager.EXTRA_STATE_RINGING) {
                EventBus.getDefault().post(PhoneCallEvent())
            } else if (phoneState == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                EventBus.getDefault().post(PhoneCallEvent())
            } else if (phoneState == TelephonyManager.EXTRA_STATE_IDLE) {
            }
        }
    }
}