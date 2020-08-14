package io.square1.limor.remote

import io.square1.limor.remote.services.RemoteServiceConfig
import io.square1.limor.remote.services.user.UserServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.Test

@ImplicitReflectionSerializer
class UserServiceImpTest {
    private lateinit var feedService: UserServiceImp
//    private val baseURL = "https://limor-api-staging.herokuapp.com/api/v1/users/feed/"
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

        feedService = UserServiceImp(config)

        val response = feedService.feedShow().test()

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

        feedService = UserServiceImp(config)

        val response = feedService.feedShow(10, 0).test()

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

        feedService = UserServiceImp(config)

        val response = feedService.userMe().test()

        response?.assertNoErrors()
        response?.assertValue {
            it.message == "Success"
        }

    }


}