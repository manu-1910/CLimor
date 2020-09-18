package io.square1.limor.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseViewModel
import io.square1.limor.common.SingleLiveEvent
import io.square1.limor.remote.extensions.parseSuccessResponse
import io.square1.limor.uimodels.UIErrorData
import io.square1.limor.uimodels.UIErrorResponse
import io.square1.limor.uimodels.UIGetNotificationsResponse
import io.square1.limor.uimodels.UINotificationItem
import io.square1.limor.usecases.GetNotificationsUseCase
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class NotificationsViewModel @Inject constructor(private val getNotificationsUseCase: GetNotificationsUseCase) :
    BaseViewModel<NotificationsViewModel.Input, NotificationsViewModel.Output>() {

    private val compositeDispose = CompositeDisposable()

    var limit: Int = 15
    var offset: Int = 0
    var notificationMap: LinkedHashMap<DateTime, ArrayList<UINotificationItem>> = LinkedHashMap()
    var notificationList: ArrayList<Notification> = ArrayList()
    var oldLength: Int = 0
    var newLength: Int = 0

    data class Input(
        val getNotificationsTrigger: Observable<Unit>
    )

    data class Output(
        val response: LiveData<UIGetNotificationsResponse>,
        val backgroundWorkingProgress: LiveData<Boolean>,
        val errorMessage: SingleLiveEvent<UIErrorResponse>
    )

    data class Notification(
        val isHeader: Boolean = false,
        val headerText: String = "",
        val notificationItem: UINotificationItem?
    )

    fun addItems(items: ArrayList<UINotificationItem>): ArrayList<Notification> {

        oldLength = notificationList.size

        for (uiNotificationItem in items) {

            val dateTime = DateTime(
                uiNotificationItem.createdAt,
                DateTimeZone.getDefault()
            ).withTimeAtStartOfDay()

            if (notificationMap.containsKey(dateTime)) {

                var containsItem = false
                for (notificationItem in notificationMap[dateTime]!!){
                    if(notificationItem.id == uiNotificationItem.id){
                        containsItem = true
                        break
                    }
                }

                if(!containsItem){
                    notificationMap[dateTime]?.add(uiNotificationItem)
                }

            } else {
                val newList = arrayListOf<UINotificationItem>()
                newList.add(uiNotificationItem)
                notificationMap[dateTime] = newList
            }
        }

        notificationList.clear()
        for ((dateTime, value) in notificationMap) {

            val humanReadable = dateTime.monthOfYear().getAsText(
                Locale.getDefault()
            ) + " " + dateTime.dayOfMonth().getAsText(Locale.getDefault()) + ", " + dateTime.year()
                .getAsText(
                    Locale.getDefault()
                )

            val notificationHeader = Notification(true, humanReadable, null)
            notificationList.add(notificationHeader)

            for (uiNotificationItem in value) {
                val notification = Notification(false, "", uiNotificationItem)
                notificationList.add(notification)
            }
        }

        newLength = notificationList.size

        return notificationList

    }

    override fun transform(input: Input): Output {
        val errorTracker = SingleLiveEvent<UIErrorResponse>()
        val backgroundWorkingProgress = MutableLiveData<Boolean>()
        val response = MutableLiveData<UIGetNotificationsResponse>()

        input.getNotificationsTrigger.subscribe({

            getNotificationsUseCase.execute(limit, offset).subscribe({
                response.value = it

            }, {
                try {
                    val error = it as HttpException
                    val errorResponse: UIErrorResponse? =
                        error.response().errorBody()?.parseSuccessResponse(
                            UIErrorResponse.serializer()
                        )
                    errorTracker.postValue(errorResponse)
                } catch (e: Exception) {
                    val dataError =
                        UIErrorData(arrayListOf(App.instance.getString(R.string.some_error)))
                    val errorResponse = UIErrorResponse(99, dataError.toString())
                    errorTracker.postValue(errorResponse)
                }

            })
        }, {}).addTo(compositeDispose)

        return Output(response, backgroundWorkingProgress, errorTracker)
    }

    override fun onCleared() {
        if (!compositeDispose.isDisposed) compositeDispose.dispose()
        super.onCleared()
    }
}