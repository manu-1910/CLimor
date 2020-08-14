package providers.remote


import entities.request.DataLogoutRequest
import entities.response.ErrorResponseEntity
import entities.response.FeedResponseEntity
import entities.response.SignUpResponseEntity
import io.reactivex.Single


interface RemoteUserProvider {
    fun userMe(): Single<SignUpResponseEntity>
    fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity>
    fun feedShow(): Single<FeedResponseEntity>
}