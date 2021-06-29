package com.limor.app.apollo

import com.limor.app.FollowUserMutation
import com.limor.app.UnFollowUserMutation
import javax.inject.Inject

class FollowRepository @Inject constructor(val apollo: Apollo) {

    suspend fun followUser(id: Int) {
        apollo.mutate(FollowUserMutation(id))
    }

    suspend fun unFollowUser(id: Int) {
        apollo.mutate(UnFollowUserMutation(id))
    }
}
