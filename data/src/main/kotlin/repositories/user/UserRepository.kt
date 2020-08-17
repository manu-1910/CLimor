package repositories.user


import entities.request.DataCreateFriendRequest
import entities.request.DataLogoutRequest
import entities.response.CreateFriendResponseEntity
import entities.response.ErrorResponseEntity
import entities.response.FeedResponseEntity
import entities.response.SignUpResponseEntity

import io.reactivex.Single

interface UserRepository {
    fun userMe(): Single<SignUpResponseEntity>
    fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity>
    fun feedShow(): Single<FeedResponseEntity>
    fun createFriend(dataCreateFriendRequest: DataCreateFriendRequest) : Single<CreateFriendResponseEntity>
}