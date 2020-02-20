package io.square1.limor.extensions

import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.square1.limor.common.ErrorMessageFactory
import io.square1.limor.common.SingleLiveEvent

fun<T: Any> Observable<T>.trackError(errorTracker: SingleLiveEvent<Int>) : Observable<T> {
    return this.doOnError{
        errorTracker.postValue(ErrorMessageFactory.create(it)) }
}

fun<T: Any> Observable<T>.trackProgress(progressTracker: MutableLiveData<Boolean>) : Observable<T> {
    var count = 0
    return this.doOnSubscribe { progressTracker.postValue(true) }
        .doOnNext { if (count == 0) {
            progressTracker.postValue(false)
            count++
        } }
        .doOnError{
            progressTracker.postValue(false)
        }
}

fun<T: Any> Single<T>.trackError(errorTracker: SingleLiveEvent<Int>) : Single<T> {
    return this.doOnError{ errorTracker.postValue(ErrorMessageFactory.create(it)) }
}

fun<T: Any> Single<T>.trackProgress(progressTracker: MutableLiveData<Boolean>) : Single<T> {
    return this.doOnSubscribe { progressTracker.postValue(true) }
        .doOnSuccess { progressTracker.postValue(false)  }
        .doOnError{ progressTracker.postValue(false) }
}

fun Completable.trackProgress(progressTracker: MutableLiveData<Boolean>) : Completable {
    return this.doOnSubscribe { progressTracker.postValue(true) }
        .doOnComplete {
            progressTracker.postValue(false) }
        .doOnError{
            progressTracker.postValue(false) }
}

fun Completable.trackError(errorTracker: SingleLiveEvent<Int>) : Completable {
    return this.doOnError{
        errorTracker.postValue(ErrorMessageFactory.create(it)) }
}

fun<T: Any> Flowable<T>.trackProgress(progressTracker: MutableLiveData<Boolean>) : Flowable<T> {
    var count = 0
    return this.doOnSubscribe { progressTracker.postValue(true) }
        .doOnNext { if (count == 0) {
            progressTracker.postValue(false)
            count++
        } }
        .doOnError{ progressTracker.postValue(false) }
}

fun<T: Any> Flowable<T>.trackError(errorTracker: SingleLiveEvent<Int>) : Flowable<T> {
    return this.doOnError{ errorTracker.postValue(ErrorMessageFactory.create(it)) }
}

//************* New Extensions For Track UIErrorResponse
/*fun<T: Any> Single<T>.trackErrorResponse(errorTracker: SingleLiveEvent<UIErrorResponse>) : Single<T> {
    return this.doOnError{

        var error = it as HttpException
        var errorResponse:UIErrorResponse? = error.response().errorBody()?.parseSuccessResponse(UIErrorResponse.serializer())

        errorTracker.postValue(errorResponse)
    }
}

fun<T: Any> Observable<T>.trackErrorResponse(errorTracker: SingleLiveEvent<UIErrorResponse>) : Observable<T> {
    return this.doOnError{
        errorTracker.postValue(errorTracker.value)
    }
}*/
