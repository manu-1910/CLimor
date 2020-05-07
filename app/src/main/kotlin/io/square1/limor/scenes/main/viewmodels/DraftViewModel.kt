package io.square1.limor.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.common.ErrorMessageFactory
import io.square1.limor.common.SingleLiveEvent
import io.square1.limor.uimodels.UIDraft
import io.square1.limor.usecases.DraftDeleteRealmUseCase
import io.square1.limor.usecases.DraftInsertRealmUseCase
import io.square1.limor.usecases.DraftLoadRealmUseCase
import timber.log.Timber
import javax.inject.Inject


class DraftViewModel @Inject constructor(
    private val draftInsertRealmUseCase: DraftInsertRealmUseCase,
    private val draftLoadRealmUseCase: DraftLoadRealmUseCase,
    private val draftDeleteRealmUseCase: DraftDeleteRealmUseCase
) : ViewModel() {

    private val compositeDispose = CompositeDisposable()
    var uiDraft = UIDraft()

    /************************************
    INSERT A DRAFT CENTRE INTO REALM
    ************************************/
    data class InputInsert(
        val trigger: Observable<Unit>
    )

    data class OutPutInsert(
        val response: LiveData<Boolean>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<Int>
    )

    fun insertDraftRealm(input: InputInsert): OutPutInsert {
        val response = MutableLiveData<Boolean>()
        val errorTracker = SingleLiveEvent<Int>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()

        input.trigger.subscribe({
            draftInsertRealmUseCase.execute(uiDraft)
                .subscribe({ response.value = true }, { errorTracker.postValue(ErrorMessageFactory.create(it)) })
        }, {}).addTo(compositeDispose)
        return OutPutInsert(response, backgroundWorkingProgress, errorTracker)
    }

    /************************************
        DELETE A DRAFT IN REALM
     ************************************/
    data class InputDelete(
        val trigger: Observable<Unit>
    )

    data class OutPutDelete(
        val response: LiveData<Boolean>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<Int>
    )

    fun deleteDraftRealm(input: InputDelete): OutPutDelete {
        val response = MutableLiveData<Boolean>()
        val errorTracker = SingleLiveEvent<Int>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()

        input.trigger.subscribe({
            draftDeleteRealmUseCase.execute(uiDraft).subscribe(
                { response.value = true },
                { errorTracker.postValue(ErrorMessageFactory.create(it)) })
        }, {
            Timber.e(it.toString())
        }).addTo(compositeDispose)
        return OutPutDelete(response, backgroundWorkingProgress, errorTracker)
    }

    /************************************
        LOAD A DRAFT LIST IN REALM
     ************************************/
    fun loadDraftRealm(): LiveData<List<UIDraft>>? =
        draftLoadRealmUseCase.execute()

    override fun onCleared() {
        if (!compositeDispose.isDisposed) compositeDispose.dispose()
        super.onCleared()
    }
}