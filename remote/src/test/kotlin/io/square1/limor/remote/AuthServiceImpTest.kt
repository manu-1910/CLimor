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
    private val baseURL = "https://limor-api-staging.herokuapp.com/"
    private val testEmail = "juanjo@square1.io"
    private val testPassword = "123456789"
    /*private val testEmail = "alfocea23@gmail.com"
    private val testPassword = "123456"*/

    private val testUsername = "juanjo"



    @Test
    fun should_do_login_successfully() {

        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "2711e12535ceb15773fe87dc691bcb8c26295bb1434f7d8f4912006dff6a189c",
            client_secret = "af570a038fbb5e9eb7c5338068f57b5a22c119d5fd273909f38b1cf4f9acd996",
            token = "",
            expiredIn = 0
        )

        authservice = AuthServiceImp(config)

        val response = authservice.login(testEmail, testPassword).test()

        response.assertNoErrors()
        response.assertValue { it.message == "Success" }
    }

   @Test
   fun should_do_sign_up_successfully() {

       val config = RemoteServiceConfig(
           baseUrl = baseURL,
           debug = true,
           client_id = "2711e12535ceb15773fe87dc691bcb8c26295bb1434f7d8f4912006dff6a189c",
           client_secret = "af570a038fbb5e9eb7c5338068f57b5a22c119d5fd273909f38b1cf4f9acd996",
           token = "",
           expiredIn = 0
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



