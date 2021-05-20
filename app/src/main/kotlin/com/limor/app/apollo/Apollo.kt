package com.limor.app.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

const val GRAPHQL_ENDPOINT = "https://apigateway.dev.limor.ie/graphql"

object Apollo {

    private val client = ApolloClient.builder()
        .serverUrl(GRAPHQL_ENDPOINT)
        .build()

    suspend fun <A : Operation.Data, B, C : Operation.Variables> launchQuery(query: Query<A, B, C>): Response<B>? =
        withContext(Dispatchers.IO) {
            val result = client.query(query).await()
            checkResultForErrors(result)
            result
        }

    private fun <T> checkResultForErrors(response: Response<T>) {
        if (response.hasErrors()) {
            throw GraphqlClientException(response.errors!![0].message)
        }
    }
}

class GraphqlClientException(message: String) : Exception(message)

fun showHumanizedErrorMessage(e: Exception): String {
    if (e is IOException || e.cause is IOException)
        return "Check internet"
    return e.message ?: "Network client error"
}