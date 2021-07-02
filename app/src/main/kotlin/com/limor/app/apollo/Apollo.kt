package com.limor.app.apollo

import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response

interface Apollo {
    suspend fun <A : Operation.Data, B, C : Operation.Variables> launchQuery(query: Query<A, B, C>): Response<B>?
    suspend fun <A : Operation.Data, B, C : Operation.Variables> mutate(query: Mutation<A, B, C>): Response<B>?

    companion object {
        const val LOAD_PORTION: Int = 20
    }
}