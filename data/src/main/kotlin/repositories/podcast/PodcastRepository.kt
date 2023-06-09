package repositories.podcast


import entities.request.DataCreateCommentRequest
import entities.request.DataCreateReportRequestEntity
import entities.request.DataDropOffRequest
import entities.request.DataPublishRequest
import entities.response.*
import io.reactivex.Single

interface PodcastRepository {
    fun publishPodcast(dataPublishRequest: DataPublishRequest): Single<PublishResponseEntity>?
    fun likePodcast(id: Int): Single<CreatePodcastLikeResponseEntity>?
    fun dislikePodcast(id: Int): Single<DeleteResponseEntity>?
    fun recastPodcast(idPodcast: Int): Single<CreatePodcastRecastResponseEntity>?
    fun deleteRecast(idPodcast: Int): Single<DeleteResponseEntity>?
    fun deletePodcast(idPodcast: Int): Single<DeleteResponseEntity>?
    fun createComment(id: Int, request: DataCreateCommentRequest): Single<CreateCommentResponseEntity>?
    fun getComments(id: Int, limit: Int, offset: Int): Single<GetCommentsResponseEntity>?
    fun reportPodcast(id: Int, request: DataCreateReportRequestEntity): Single<CreateReportResponseEntity>?
    fun getPopularPodcasts(): Single<PopularPodcastsResponseEntity>?
    fun getFeaturedPodcasts(): Single<FeaturedPodcastsResponseEntity>?
    fun getPodcastById(id: Int): Single<GetPodcastResponseEntity>?
    fun createDropOff(id: Int, request: DataDropOffRequest): Single<UpdatedResponseEntity>?
}