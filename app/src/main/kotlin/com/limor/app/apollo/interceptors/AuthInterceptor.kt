package com.limor.app.apollo.interceptors

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.limor.app.scenes.auth_new.util.JwtChecker
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var token = getToken()
        val isExpired = JwtChecker.isTokenExpired(token)
        val hasLuid = JwtChecker.isJwtContainsLuid(token)
        if(isExpired || !hasLuid ) {
            Timber.d("Firebase token isExpired $isExpired | hasLuid $hasLuid -> forcing update")
            token = getToken(forceRefresh = true)
        }
        Timber.d("Firebase auth token $token")
        val request = chain.request().newBuilder()
            .addHeader("authorization", "bearer $token")
            .build()

        return chain.proceed(request)
    }

    companion object {
        var IS_TESTING_AUTH_CASE = false

        fun getToken(forceRefresh: Boolean = false): String {
            val tokenTask = FirebaseAuth.getInstance().currentUser?.getIdToken(forceRefresh)
            return if (tokenTask == null)
                ""
            else
                try {
                    Tasks.await(tokenTask)?.token ?: ""
                } catch (e: Exception) {
                    Timber.e(e)
                    ""
                }
        }
    }
}