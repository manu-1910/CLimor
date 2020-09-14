package io.square1.limor.remote

import io.square1.limor.remote.services.RemoteServiceConfig
import io.square1.limor.remote.services.user.UserServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.Test

@ImplicitReflectionSerializer
class UserServiceImpTest {
    private lateinit var userService: UserServiceImp

    private val URL_DEVELOPMENT = "https://limor-api-development.herokuapp.com"
    private val URL_STAGING = "https://limor-api-staging.herokuapp.com"
    private val CURRENT_URL = URL_DEVELOPMENT


    private val TOKEN_DEVELOPMENT = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce"
    private val TOKEN_STAGING = "36bf82e596dc582796508c09d050484181fa51278eb6b0c2bdbfb269c98a3992"
    private val CURRENT_TOKEN = TOKEN_DEVELOPMENT



    @Test
    fun should_get_feed_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = CURRENT_URL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = CURRENT_TOKEN,
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
            baseUrl = CURRENT_URL,
            debug = false,
            client_id = "",
            client_secret = "",
            token = CURRENT_TOKEN,
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
            baseUrl = CURRENT_URL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = CURRENT_TOKEN,
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
            baseUrl = CURRENT_URL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = CURRENT_TOKEN,
            expiredIn = 0
        )

        userService = UserServiceImp(config)

        val idNewFriend = 8

        val response = userService.createFriend(idNewFriend).test()

        response.assertNoErrors()
        response.assertValue { it.message == "Success" }
    }

    @Test
    fun should_get_notifications_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = CURRENT_URL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = "KGFkiVN8SXyNLs8-28_aTBOd96YqOu_PQXRMGSwL4v8",
            expiredIn = 0
        )

        userService = UserServiceImp(config)

        val response = userService.getNotifications(10, 0).test()

        response.assertNoErrors()
        response.assertValue { it.message == "Success" }
    }


}