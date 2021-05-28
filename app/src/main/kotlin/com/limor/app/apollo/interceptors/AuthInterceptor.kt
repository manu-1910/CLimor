package com.limor.app.apollo.interceptors

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = if (IS_TESTING_AUTH_CASE) MOCKED_AUTH_TOKEN else getToken()

        Timber.d("Firebase auth token $token")
        val request = chain.request().newBuilder()
            .addHeader("authorization", "bearer $token")
            .build()

        return chain.proceed(request)
    }

    private fun getToken(): String {
        val tokenTask = FirebaseAuth.getInstance().currentUser?.getIdToken(false)
        return if (tokenTask == null)
            MOCKED_AUTH_TOKEN
        else
            try {
                Tasks.await(tokenTask)?.token ?: ""
            } catch (e: Exception) {
                Timber.e(e)
                ""
            }
    }

    companion object {
        val MOCKED_AUTH_TOKEN = "knsjdn232knfkn23fnwb"
        var IS_TESTING_AUTH_CASE = true
    }
}