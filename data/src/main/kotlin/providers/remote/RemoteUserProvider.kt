package providers.remote


import entities.request.DataLogoutRequest
import entities.response.ErrorResponseEntity
import entities.response.SignUpResponseEntity
import io.reactivex.Single


interface RemoteUserProvider {
    fun userMe(): Single<SignUpResponseEntity>
    fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity>

}