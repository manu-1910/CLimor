package com.limor.app.events

class PhoneCallEvent(state: String) {
    var state : String = state
    companion object{
        const val IDLE = "IDLE"
        const val RINGING = "RINGING"
        const val OFFHOOK = "OFFHOOK"
    }
}