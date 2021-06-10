package com.limor.app.scenes.auth_new.util

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class JwtCheckerTest {
    @Before
    fun before() {
        TestTimberInstance.initTimber()
    }

    @Test
    fun shouldParseValidJWT() {
        val jwt = JwtChecker.createJwtObjectFromToken(testJWT)
        Timber.d("Issuer -> ${jwt?.issuer}")
        assertThat("Jwt Issuer is not null", jwt?.issuer != null)
    }

    @Test
    fun shouldParseInvalidJWT() {
        val jwt = JwtChecker.createJwtObjectFromToken("invalid_jwt")
        Timber.d("Issuer -> ${jwt?.issuer}")
        assertThat(jwt?.issuer, IsNull())
    }

    @Test
    fun shouldParseEmptyJWT() {
        val jwt = JwtChecker.createJwtObjectFromToken("")
        Timber.d("Issuer -> ${jwt?.issuer}")
        assertThat(jwt?.issuer, IsNull())
    }

    @Test
    fun shouldNotContainLuidFromValidJWT() {
        val containsLuid = JwtChecker.isJwtContainsLuid(testJWT)
        Timber.d("Contains LUID -> $containsLuid")
        assertThat(containsLuid, IsEqual(false))
    }

    @Test
    fun shouldNotContainLuidFromInvalidJWT() {
        val containsLuid = JwtChecker.isJwtContainsLuid("invalid_jwt")
        Timber.d("Contains LUID -> $containsLuid")
        assertThat(containsLuid, IsEqual(false))
    }

    @Test
    fun shouldNotContainLuidFromEmptyJWT() {
        val containsLuid = JwtChecker.isJwtContainsLuid("")
        Timber.d("Contains LUID -> $containsLuid")
        assertThat(containsLuid, IsEqual(false))
    }


    @Test
    fun shouldParseJWTForExpiredLogicPositive() {
        val isExpired = JwtChecker.isTokenExpired(testJWT)
        Timber.d("Expired  -> $isExpired")
        assertThat(isExpired, IsEqual(true))
    }

    companion object {
        const val testJWT =
            "eyJhbGciOiJSUzI1NiIsImtpZCI6ImFiMGNiMTk5Zjg3MGYyOGUyOTg5YWI0ODFjYzJlNDdlMGUyY2MxOWQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vbGltb3ItNTQzOWIiLCJhdWQiOiJsaW1vci01NDM5YiIsImF1dGhfdGltZSI6MTYyMzE2OTc0MSwidXNlcl9pZCI6IjNzT001NzNjMldidlRPekxyZk81aDhjaGc0MDMiLCJzdWIiOiIzc09NNTczYzJXYnZUT3pMcmZPNWg4Y2hnNDAzIiwiaWF0IjoxNjIzMTY5NzQxLCJleHAiOjE2MjMxNzMzNDEsInBob25lX251bWJlciI6IiszODA2NjExMjIzMzMiLCJmaXJlYmFzZSI6eyJpZGVudGl0aWVzIjp7InBob25lIjpbIiszODA2NjExMjIzMzMiXX0sInNpZ25faW5fcHJvdmlkZXIiOiJwaG9uZSJ9fQ.Xoxg96OpJkbjPY_e1lxMxC87sLEzUOUvY9OVVMycCY9wAKhdUd6LWVb0utpW1GrCZcJccv-jhE_xWvYltnIfAc67Y7tBpEMQJi0NcO4aU9rV3KBkxxdSpfSK97wA_3vVQ8xi6SUd4pRqCSnG3ykQ1zcesZYIA15v9tPSr-vT7SqeOgbavenYla34cDsY31NMCuICWWPXvB9Kk2s4Yro_7NBgiWbjB6quBv1W3AUhIJ5JdkIRY5y0Jp4vi8Y4nWi44OxSqVKjsNywlxZoxaSo2QD7XdLCLbNccWM45geT72xMkFFw4UC2BIgBHTIiyL9ln3BncSRpkGURXOcc7uchCg"
    }
}