package com.limor.app.scenes.auth_new.model

import androidx.lifecycle.MutableLiveData
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.apollo.showHumanizedErrorMessage
import com.limor.app.scenes.auth_new.data.LanguageWrapper
import com.limor.app.scenes.auth_new.data.getLanguagesByInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LanguagesProvider(private val scope: CoroutineScope) {

    private var languages: List<LanguageWrapper> = mutableListOf()

    val languageLiveDataError =
        MutableLiveData<String>().apply { value = "" }

    val languagesLiveData =
        MutableLiveData<List<LanguageWrapper>>().apply { value = languages }

    val languagesSelectionDone =
        MutableLiveData<Boolean>().apply { value = false }

    fun downloadLanguages() {
        if (languages.isEmpty())
            loadLanguagesRepo()
    }

    private fun loadLanguagesRepo() {
        scope.launch {
            try {
                delay(500)
                val response = GeneralInfoRepository.fetchLanguages()
                languages = response!!.map { LanguageWrapper(it, false) }
                languagesLiveData.postValue(languages)
            } catch (e: Exception) {
                languageLiveDataError.postValue(showHumanizedErrorMessage(e))
            }
        }
    }

    fun updateLanguagesSelection() {
        val anySelected = languages.any { it.isSelected }
        languagesSelectionDone.postValue(anySelected)
    }

    fun onLanguageInputChanged(input: String?) {
        if (input == null || input.trim().isEmpty()) {
            languagesLiveData.postValue(languages)
            return
        }
        val filtered = getLanguagesByInput(input, languages)
        languagesLiveData.postValue(filtered)
    }

    fun getActiveLanguages():List<String?>{
        return languages.filter { it.isSelected }.map { it.language.code}
    }
}