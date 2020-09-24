package io.square1.limor.remote

import io.square1.limor.remote.entities.requests.NWSearchTermRequest
import io.square1.limor.remote.services.RemoteServiceConfig
import io.square1.limor.remote.services.search.SearchServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.Test

@ImplicitReflectionSerializer
class SearchServiceImpTest {
    private lateinit var searchService: SearchServiceImp

    private val URL_DEVELOPMENT = "https://limor-api-development.herokuapp.com"
    private val URL_STAGING = "https://limor-api-staging.herokuapp.com"
    private val CURRENT_URL = URL_DEVELOPMENT


    private val TOKEN_TEST_DEVELOPMENT = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce"
    private val TOKEN_USER_DEVELOPMENT = "4t6bOFXd3L89qqPLdXsBHtTOP4_-t_61Q2kl5R4dAdk"
    private val SECONDARY_TOKEN_USER_DEVELOPMENT = "M4G7QCmu0xkzylHP3JtnH9B_krxPOot9Z91mfsZPavM"
    private val TOKEN_STAGING = "36bf82e596dc582796508c09d050484181fa51278eb6b0c2bdbfb269c98a3992"
    private val CURRENT_TOKEN = SECONDARY_TOKEN_USER_DEVELOPMENT

    @Test
    fun should_get_podcasts_by_tag_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = CURRENT_URL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = CURRENT_TOKEN,
            expiredIn = 0
        )

        searchService = SearchServiceImp(config)

        val tag = "test"
        val response = searchService.podcastsByTag(0, 10, tag)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_get_locations_by_term_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = CURRENT_URL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = CURRENT_TOKEN,
            expiredIn = 0
        )

        searchService = SearchServiceImp(config)

        val term = ""
        val request = NWSearchTermRequest(term)
        val response = searchService.searchLocations(request)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_get_suggested_users_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = CURRENT_URL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = CURRENT_TOKEN,
            expiredIn = 0
        )

        searchService = SearchServiceImp(config)

        val response = searchService.getSuggestedUsers()?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }


}



