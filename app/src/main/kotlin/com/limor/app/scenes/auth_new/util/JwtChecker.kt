package com.limor.app.scenes.auth_new.util

import com.auth0.android.jwt.JWT
import com.limor.app.apollo.interceptors.AuthInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

object JwtChecker {

    suspend fun isFirebaseJwtContainsLuid(): Boolean {
        return withContext(Dispatchers.IO) {
            val token = AuthInterceptor.getToken(forceRefresh = true)
            isJwtContainsLuid(token)
        }
    }

    fun isTokenExpired(token: String): Boolean {
        val jwt = createJwtObjectFromToken(token) ?: return true
        return jwt.isExpired(10)
    }

    fun createJwtObjectFromToken(token: String): JWT? {
        if (token.isEmpty()) return null
        return try {
            val jwt = JWT(token)
            jwt
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    fun isJwtContainsLuid(token: String): Boolean {
        val jwt = createJwtObjectFromToken(token) ?: return false
        val luid = jwt.claims["luid"]?.asString()
        return luid != null && luid.isNotEmpty()
    }

    fun isJwtContainsEmail(token: String): Boolean {
        val jwt = createJwtObjectFromToken(token) ?: return false
        jwt.claims["email"]?.asString()?.let {
            return it.isNotBlank()
        }
        return false
    }

    suspend fun getUserIdFromJwt(): Int? {
        return withContext(Dispatchers.IO) {
            val token = AuthInterceptor.getToken(forceRefresh = true)
            val jwt = createJwtObjectFromToken(token) ?: return@withContext null
            jwt.claims["luid"]?.asInt()
        }
    }
}