package io.square1.limor.remote.services.search

import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST


const val SEARCH_TAG_PATH = "/api/v1/searches/tags"
const val SEARCH_LOCATION_PATH = "/api/v1/searches/locations"


interface SearchService {

    @POST(SEARCH_TAG_PATH)
    fun searchTag(
        @Body tagRequest: RequestBody
    ): Single<ResponseBody>


    @POST(SEARCH_LOCATION_PATH)
    fun searchLocation(
        @Body locationsRequest: RequestBody
    ): Single<ResponseBody>

}