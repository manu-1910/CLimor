package io.square1.limor.remote.services.categories


import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


const val CATEGORIES_PATH = "/api/v1/categories"
const val PODCASTS_BY_CATEGORY_PATH = "/api/v1/categories/{id}/podcasts"


interface CategoriesService {

    @GET(CATEGORIES_PATH)
    fun getCategories(): Single<ResponseBody>

    @GET(CATEGORIES_PATH)
    fun getCategories(
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?
    ): Single<ResponseBody>

    @GET(PODCASTS_BY_CATEGORY_PATH)
    fun getPodcastByCategory(
        @Path("id") id: Int,
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?
    ): Single<ResponseBody>

}