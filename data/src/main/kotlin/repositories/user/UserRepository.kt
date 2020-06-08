package repositories.user


import entities.request.DataLogoutRequest
import entities.response.ErrorResponseEntity
import entities.response.SignUpResponseEntity

import io.reactivex.Single

interface UserRepository {
    fun userMe(): Single<SignUpResponseEntity>
    fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity>
}