package repositories.user


import entities.request.DataLogoutRequest
import entities.response.ErrorResponseEntity
import entities.response.FeedResponseEntity
import entities.response.SignUpResponseEntity

import io.reactivex.Single

interface UserRepository {
    fun userMe(): Single<SignUpResponseEntity>
    fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity>
    fun feedShow(): Single<FeedResponseEntity>
}