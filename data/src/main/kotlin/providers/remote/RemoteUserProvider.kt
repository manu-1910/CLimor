package providers.remote


import entities.request.DataLogoutRequest
import entities.response.CreateFriendResponseEntity
import entities.response.ErrorResponseEntity
import entities.response.FeedResponseEntity
import entities.response.SignUpResponseEntity
import io.reactivex.Single


interface RemoteUserProvider {
    fun userMe(): Single<SignUpResponseEntity>
    fun logOut(dataLogoutRequest: DataLogoutRequest): Single<ErrorResponseEntity>
    fun feedShow(): Single<FeedResponseEntity>
    fun feedShow(limit : Int, offset: Int): Single<FeedResponseEntity>
    fun createFriend(id: Int) : Single<CreateFriendResponseEntity>
}