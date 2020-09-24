package io.square1.limor.remote.services.search

import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

const val SEARCH_TAG_PATH = "/api/v1/searches/tags"
const val SEARCH_LOCATION_PATH = "/api/v1/searches/locations"
const val TRENDING_TAGS_PATH = "/api/v1/tags/trending"
const val PROMOTED_TAGS_PATH = "/api/v1/tags/promoted_tags"
const val PODCASTS_TAG_PATH = "/api/v1/tags/podcasts"
const val SUGGESTED_USERS_PATH = "/api/v1/users/suggested"
const val SEARCH_USERS_PATH = "/api/v1/searches/users"

interface SearchService {

    @POST(SEARCH_TAG_PATH)
    fun searchTag(
        @Query ("tag") tag: String
    ): Single<ResponseBody>

    @POST(SEARCH_LOCATION_PATH)
    fun searchLocation(
        @Body locationsRequest: RequestBody
    ): Single<ResponseBody>

    @POST(SEARCH_USERS_PATH)
    fun searchUsers(
        @Body usersRequest: RequestBody
    ): Single<ResponseBody>

    @GET(TRENDING_TAGS_PATH)
    fun trendingTags(): Single<ResponseBody>

    @GET(PROMOTED_TAGS_PATH)
    fun promotedTags(): Single<ResponseBody>

    @GET(PODCASTS_TAG_PATH)
    fun podcastsByTag(@Query ("limit") limit : Int?,
                      @Query("offset") offset: Int?,
                      @Query("tag") tag: String): Single<ResponseBody>

    @GET(SUGGESTED_USERS_PATH)
    fun getSuggestedUsers(): Single<ResponseBody>
}