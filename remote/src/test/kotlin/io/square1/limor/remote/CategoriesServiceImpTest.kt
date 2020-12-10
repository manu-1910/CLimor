package io.square1.limor.remote


import io.square1.limor.remote.services.RemoteServiceConfig
import io.square1.limor.remote.services.categories.CategoriesServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.Test


@ImplicitReflectionSerializer
class CategoriesServiceImpTest{
    private lateinit var categoryService : CategoriesServiceImp
    //    private val baseURL = "https://limor-api-staging.herokuapp.com/api/v1/podcasts/"
    private val baseURL = "https://limor-api-development.herokuapp.com"

    private val URL_DEVELOPMENT = "https://limor-api-development.herokuapp.com"
    private val URL_STAGING = "https://limor-api-staging.herokuapp.com"
    private val CURRENT_URL = URL_DEVELOPMENT


    private val TOKEN_TEST_DEVELOPMENT = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce"
    private val TOKEN_USER_DEVELOPMENT = "4t6bOFXd3L89qqPLdXsBHtTOP4_-t_61Q2kl5R4dAdk"
    private val SECONDARY_TOKEN_USER_DEVELOPMENT = "0Lmp9DWYvOVQZJ-9eJ4lsQYjYlTZMD9MfBqwDs2_eTE"
    private val TOKEN_STAGING = "36bf82e596dc582796508c09d050484181fa51278eb6b0c2bdbfb269c98a3992"
    private val CURRENT_TOKEN = SECONDARY_TOKEN_USER_DEVELOPMENT

    @Test
    fun should_get_categories_successfully() {

        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "2711e12535ceb15773fe87dc691bcb8c26295bb1434f7d8f4912006dff6a189c",
            client_secret = "af570a038fbb5e9eb7c5338068f57b5a22c119d5fd273909f38b1cf4f9acd996",
//            token = "Bearer 9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            token = CURRENT_TOKEN,
            expiredIn = 90000000000
        )

        categoryService = CategoriesServiceImp(config)



        val responseCreate = categoryService.getCategories()?.test()

        responseCreate?.assertNoErrors()

        responseCreate?.assertValue {
            it.message == "Success"
        }
        responseCreate?.assertNoErrors()


    }

    @Test
    fun should_get_categories_successfully_with_limit_and_offset() {

        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "2711e12535ceb15773fe87dc691bcb8c26295bb1434f7d8f4912006dff6a189c",
            client_secret = "af570a038fbb5e9eb7c5338068f57b5a22c119d5fd273909f38b1cf4f9acd996",
//            token = "Bearer 9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            token = CURRENT_TOKEN,
            expiredIn = 90000000000
        )

        categoryService = CategoriesServiceImp(config)



        val responseCreate = categoryService.getCategories(2, 0)?.test()

        responseCreate?.assertNoErrors()

        responseCreate?.assertValue {
            it.message == "Success"
        }
        responseCreate?.assertNoErrors()


    }
}



