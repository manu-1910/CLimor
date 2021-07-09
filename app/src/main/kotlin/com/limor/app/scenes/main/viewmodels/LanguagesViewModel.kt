package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.scenes.auth_new.data.LanguageWrapper
import com.limor.app.scenes.auth_new.model.LanguagesProvider
import javax.inject.Inject

class LanguagesViewModel @Inject constructor(
    val languagesProvider: LanguagesProvider,
) : ViewModel() {

    fun downloadLanguages() = languagesProvider.downloadLanguages(viewModelScope)

    val languagesLiveData: LiveData<List<LanguageWrapper>>
        get() = Transformations.distinctUntilChanged(languagesProvider.languagesLiveData)

    val languagesSelectionDone: LiveData<Boolean>
        get() = Transformations.distinctUntilChanged(languagesProvider.languagesSelectionDone)

    val languagesLiveDataError: LiveData<String>
        get() = languagesProvider.languageLiveDataError

    fun updateLanguagesSelection() = languagesProvider.updateLanguagesSelection()

    fun onLanguageInputChanged(input: String?) = languagesProvider.onLanguageInputChanged(input)

}