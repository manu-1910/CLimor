package com.limor.app.scenes.auth_new.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.GendersQuery
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.apollo.showHumanizedErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class GendersProvider(private val scope: CoroutineScope) {
    private var genders: List<GendersQuery.Gender> = mutableListOf()
    private val _genderLiveData =
        MutableLiveData<List<GendersQuery.Gender>>().apply { value = genders }

    val gendersLiveData: LiveData<List<GendersQuery.Gender>>
        get() = _genderLiveData

    private val _gendersSelectionDone =
        MutableLiveData<Boolean>().apply { value = false }

    val gendersSelectionDone: LiveData<Boolean>
        get() = _gendersSelectionDone

    private val _gendersLiveDataError =
        MutableLiveData<String>().apply { value = "" }

    val gendersLiveDataError: LiveData<String>
        get() = _gendersLiveDataError

    fun downloadGenders() {
        _gendersLiveDataError.postValue("")
        if (genders.isEmpty())
            loadGendersRepo()
    }

    private fun loadGendersRepo() {
        scope.launch {
            try {
                delay(500)
                val response = GeneralInfoRepository.fetchGenders()
                genders = response!!
                _genderLiveData.postValue(genders)
                selectedGenderId = genders.first().id ?: 0
                _gendersSelectionDone.postValue(true)
            } catch (e: Exception) {
                Timber.e(e)
                _gendersLiveDataError.postValue(showHumanizedErrorMessage(e))
            }
        }
    }

    var selectedGenderId: Int = 0

    fun selectedGenderIndex(): Int {
        if (selectedGenderId == 0)
            return -1
        val selectedGender = genders.firstOrNull { it.id == selectedGenderId } ?: return -1
        return genders.indexOf(selectedGender)
    }
}

val MOCKED_GENDERS = listOf(
    GendersQuery.Gender(id = 1, gender = "Male", description = "Male"),
    GendersQuery.Gender(id = 2, gender = "Female", description = "Female"),
    GendersQuery.Gender(id = 3, gender = "Other1", description = "Other1")
)
