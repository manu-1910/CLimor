package io.square1.limor.remote

import io.square1.limor.remote.entities.requests.NWCreateFriendRequest
import io.square1.limor.remote.services.RemoteServiceConfig
import io.square1.limor.remote.services.user.UserServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.Test

@ImplicitReflectionSerializer
class UserServiceImpTest {
    private lateinit var userService: UserServiceImp
//    private val baseURL = "https://limor-api-staging.herokuapp.com/"
    private val baseURL = "https://limor-api-development.herokuapp.com"



    @Test
    fun should_get_feed_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            expiredIn = 0
        )

        userService = UserServiceImp(config)

        val response = userService.feedShow().test()

        response?.assertNoErrors()
        response?.assertValue {
            it.message == "Success"
        }

    }

    @Test
    fun should_get_feed_successfully_with_params() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            expiredIn = 0
        )

        userService = UserServiceImp(config)

        val response = userService.feedShow(10, 0).test()

        response?.assertNoErrors()
        response?.assertValue {
            it.message == "Success"
        }

    }

    @Test
    fun should_get_user_me_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            expiredIn = 0
        )

        userService = UserServiceImp(config)

        val response = userService.userMe().test()

        response?.assertNoErrors()
        response?.assertValue {
            it.message == "Success"
        }

    }

    @Test
    fun should_create_friend_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            expiredIn = 0
        )

        userService = UserServiceImp(config)

        val signUpRequest = NWCreateFriendRequest(
            141
        )

        val response = userService.createFriend(signUpRequest).test()

        response.assertNoErrors()
        response.assertValue { it.message == "Success" }
    }


}