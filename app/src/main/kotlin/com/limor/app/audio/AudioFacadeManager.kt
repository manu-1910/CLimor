package com.limor.app.audio

class AudioFacadeManager {

    private lateinit var audioFacade: AudioFacade
    fun getInstance(): AudioFacade {
        return audioFacade
    }

    fun initializeWav() {
        audioFacade = AudioFacadeWav()
    }

//    fun InitializeOther() {
//        audioFacade = AudioFacadeOther()
//    }

    fun getAudioFacade(): AudioFacade {
        return audioFacade
    }

}