package com.limor.app.apollo

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.limor.app.App
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> ApolloCall<T>.await(): Response<T> = suspendCancellableCoroutine { continuation ->

    continuation.invokeOnCancellation {
        cancel()
    }

    enqueue(object : ApolloCall.Callback<T>() {

        private val hasBeenCalled = AtomicBoolean(false)

        override fun onResponse(response: Response<T>) {
            if (!hasBeenCalled.getAndSet(true)) {
                continuation.resume(response)
            }
        }

        override fun onFailure(e: ApolloException) {
            if (!hasBeenCalled.getAndSet(true)) {
                continuation.resumeWithException(e)
            }
        }

        override fun onNetworkError(e: ApolloNetworkException) {
            App.instance.showSomeThingWentWrongPopUp()
        }

    })
}