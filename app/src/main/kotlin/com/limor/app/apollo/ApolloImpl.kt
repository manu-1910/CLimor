package com.limor.app.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

const val GRAPHQL_ENDPOINT = "https://apigateway.dev.limor.ie/graphql"

class ApolloImpl(val client: ApolloClient) : Apollo {

    override suspend fun <A : Operation.Data, B, C : Operation.Variables> launchQuery(query: Query<A, B, C>): Response<B>? =
        withContext(Dispatchers.IO) {
            val result = client.query(query).await()
            checkResultForErrors(result, query.javaClass.simpleName)
            result
        }

    override suspend fun <A : Operation.Data, B, C : Operation.Variables> mutate(query: Mutation<A, B, C>): Response<B>? =
        withContext(Dispatchers.IO) {
            val result = client.mutate(query).await()
            checkResultForErrors(result, query.javaClass.simpleName)
            result
        }

    private fun <T> checkResultForErrors(response: Response<T>, queryName: String) {
        if (response.hasErrors()) {
            val errorMessage = response.errors!!.joinToString("\n") { it.message }
            val errorWithQueryPrefix = StringBuilder(queryName).append("-> ").append(errorMessage)
            val apiException = GraphqlClientException(errorWithQueryPrefix.toString())
            Timber.e(apiException)
            throw apiException
        }
    }
}

class GraphqlClientException(message: String) : Exception(message)

fun showHumanizedErrorMessage(e: Exception): String {
    if (e is IOException || e.cause is IOException)
        return "Check internet"
    return e.message?.split("->")?.last() ?: "Network client error"
}