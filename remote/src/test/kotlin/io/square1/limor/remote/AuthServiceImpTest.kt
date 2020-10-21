package io.square1.limor.remote


import io.square1.limor.remote.entities.requests.NWSignUpRequest
import io.square1.limor.remote.entities.requests.NWSignUpUser
import io.square1.limor.remote.services.RemoteServiceConfig
import io.square1.limor.remote.services.auth.AuthServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.Test



@ImplicitReflectionSerializer
class AuthServiceImpTest{
    private lateinit var authservice: AuthServiceImp

    private val testEmail = "juanjo@square1.io"
    private val testPassword = "123456789"

//    private val testEmail = "jose6@square1.io"
//    private val testPassword = "123456"
    private val testUsername = "jose6"


    private val URL_DEVELOPMENT = "https://limor-api-development.herokuapp.com"
    private val URL_STAGING = "https://limor-api-staging.herokuapp.com"
    private val CURRENT_URL = URL_DEVELOPMENT

    private val TOKEN_DEVELOPMENT = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce"
    private val TOKEN_STAGING = "36bf82e596dc582796508c09d050484181fa51278eb6b0c2bdbfb269c98a3992"
    private val CURRENT_TOKEN = TOKEN_STAGING

    private val CLIENT_ID_DEVELOPMENT = "333ac452469f437fac9fe94de3dce1b6224ede744a38ceda1281cad7929319e3"
    private val CLIENT_ID_STAGING = "2711e12535ceb15773fe87dc691bcb8c26295bb1434f7d8f4912006dff6a189c"
    private val CURRENT_CLIENT_ID = CLIENT_ID_DEVELOPMENT

    private val CLIENT_SECRET_DEVELOPMENT = "0023dfe2893b107ae39f7106e9d4eddc715f1372053a8261ff8d539951c35cf1"
    private val CLIENT_SECRET_STAGING = "af570a038fbb5e9eb7c5338068f57b5a22c119d5fd273909f38b1cf4f9acd996"
    private val CURRENT_CLIENT_SECRET = CLIENT_SECRET_DEVELOPMENT



    @Test
    fun should_do_login_successfully() {

        val config = RemoteServiceConfig(
            baseUrl = CURRENT_URL,
            debug = true,
            client_id = CURRENT_CLIENT_ID,
            client_secret = CURRENT_CLIENT_SECRET,
            token = "",
            expiredIn = 90000000000
        )

        authservice = AuthServiceImp(config)

        val response = authservice.login("jose+@square1.io", "123456").test()

        response.assertNoErrors()
        response.assertValue { it.message == "Success" }
    }

   @Test
   fun should_do_sign_up_successfully() {

       val config = RemoteServiceConfig(
           baseUrl = CURRENT_URL,
           debug = true,
           client_id = CURRENT_CLIENT_ID,
           client_secret = CURRENT_CLIENT_SECRET,
           token = "",
           expiredIn = 90000000000
       )

       authservice = AuthServiceImp(config)

       val signUpRequest = NWSignUpRequest(
           config.client_id,
           config.client_secret,
           "user",
           NWSignUpUser(
               testEmail,
               testPassword,
               testUsername
           )
       )

       val response = authservice.register(signUpRequest).test()

       response.assertNoErrors()
       response.assertValue { it.message == "Success" }
   }

   //@Test
   //fun should_do_reset_pass_successfully() {

   //    val config = RemoteServiceConfig(
   //        apiKey = apiKey,
   //        sessionId = "",
   //        baseUrl = baseURL,
   //        debug = true
   //    )
   //    authservice = AuthServiceImp(config)

   //    val forgotPasswordRequest = ForgotPasswordRequest(email = testEmail)

   //    authservice.forgotPassword(forgotPasswordRequest).test()
   //}













//    @Test
//    fun should_fails_if_apiKey_is_not_valid() {
//
//        val config = RemoteServiceConfig(
//            apiKey = "fakeApiKey",
//            baseUrl = baseURL,
//            appVersion = appVersion,
//            debug = true
//        )
//        authservice = AuthServiceImp(config)
//
//        val loginRequest = LoginRequest(
//            Email = testEmail,
//            Password = testPassword
//        )
//
//        val response = authservice.login(loginRequest).test()
//
//        response.assertError(NWErrorResponse::class.java)
//    }
//
//    @Test
//    fun should_fails_if_email_is_not_valid() {
//
//        val config = RemoteServiceConfig(
//            apiKey = apiKey,
//            baseUrl = baseURL,
//            appVersion = appVersion,
//            debug = true
//        )
//        authservice = AuthServiceImp(config)
//
//        val loginRequest = LoginRequest(
//            Email = "fakeEmail",
//            Password = testPassword
//        )
//
//        val response = authservice.login(loginRequest).test()
//
//        response.assertError(HttpException::class.java)
//        response.assertError { (it as? HttpException)?.code() == 400 }
//    }
//
//    @Test
//    fun should_fails_if_password_is_not_valid() {
//
//        val config = RemoteServiceConfig(
//            apiKey = apiKey,
//            baseUrl = baseURL,
//            appVersion = appVersion,
//            debug = true
//        )
//        authservice = AuthServiceImp(config)
//
//        val loginRequest = LoginRequest(
//            Email = testEmail,
//            Password = "fakePassword"
//        )
//
//        val response = authservice.login(loginRequest).test()
//
//        response.assertError(HttpException::class.java)
//        response.assertError { (it as? HttpException)?.code() == 401 }
//    }
}



