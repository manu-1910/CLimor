package io.square1.limor.remote

import io.square1.limor.remote.services.RemoteServiceConfig
import io.square1.limor.remote.services.search.SearchServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.Test

@ImplicitReflectionSerializer
class SearchServiceImpTest {
    private lateinit var searchService: SearchServiceImp

    //    private val baseURL = "https://limor-api-staging.herokuapp.com/api/v1/podcasts/"
    private val baseURL = "https://limor-api-development.herokuapp.com"

    @Test
    fun should_get_podcasts_by_tag_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            expiredIn = 0
        )

        searchService = SearchServiceImp(config)

        val tag = "test"
        val response = searchService.podcastsByTag(0, 10, tag)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }


}



