package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.GetUserNotificationsQuery
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotiUIMode(
    val id: Int?,
    val read: Boolean?,
    val createdAt: String?,
    val notificationType:String?,
    val redirectTarget: TargetUIModel?,
    val initiator: InitiatorUIModel?,
    val receiver: ReceiverUIModel?,
    val message: String?
): Parcelable

fun GetUserNotificationsQuery.Notification?.mapToUiModel(): NotiUIMode{
    return NotiUIMode(id = this?.id,read = this?.read,createdAt = this?.createdAt,notificationType = this?.notificationType,
    redirectTarget = this?.redirectTarget?.mapToUIModel(),
        initiator = this?.initiator?.mapToUIModel(),
        receiver = this?.receiver?.mapToUIModel(), message = this?.message
    )
}

private fun GetUserNotificationsQuery.Receiver?.mapToUIModel(): ReceiverUIModel {
    return ReceiverUIModel(userId = this?.userId,username = this?.username)
}

private fun GetUserNotificationsQuery.Initiator?.mapToUIModel(): InitiatorUIModel {
    return InitiatorUIModel(userId = this?.userId, username = this?.username, firstName = this?.firstName, lastName = this?.lastName, imageUrl = this?.imageUrl)
}

private fun GetUserNotificationsQuery.RedirectTarget?.mapToUIModel(): TargetUIModel {
    return  TargetUIModel(id = this?.id,type = this?.type)
}

@Parcelize
data class TargetUIModel(
    val id: Int?,
    val type: String?,
): Parcelable

@Parcelize
data class InitiatorUIModel(
    val userId: Int?,
    val username: String?,
    val firstName: String?,
    val lastName:String?,
    val imageUrl:String?,
): Parcelable

@Parcelize
data class ReceiverUIModel(
    val userId: Int?,
    val username:String?
): Parcelable
