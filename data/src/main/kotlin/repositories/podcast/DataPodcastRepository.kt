package repositories.podcast


import entities.request.DataCreateCommentRequest
import entities.request.DataCreateReportRequestEntity
import entities.request.DataDropOffRequest
import entities.request.DataPublishRequest
import entities.response.*
import io.reactivex.Single
import providers.remote.RemotePodcastProvider
import javax.inject.Inject


class DataPodcastRepository @Inject constructor(private val remoteProvider: RemotePodcastProvider): PodcastRepository {


    override fun publishPodcast(dataPublishRequest: DataPublishRequest): Single<PublishResponseEntity>? {
        return remoteProvider.publishPodcast(dataPublishRequest)
    }

    override fun likePodcast(id: Int): Single<CreatePodcastLikeResponseEntity>? {
        return remoteProvider.likePodcast(id)
    }

    override fun dislikePodcast(id: Int): Single<DeleteResponseEntity>? {
        return remoteProvider.dislikePodcast(id)
    }

    override fun recastPodcast(idPodcast: Int): Single<CreatePodcastRecastResponseEntity>? {
        return remoteProvider.recastPodcast(idPodcast)
    }

    override fun deleteRecast(idPodcast: Int): Single<DeleteResponseEntity>? {
        return remoteProvider.deleteRecast(idPodcast)
    }

    override fun deletePodcast(idPodcast: Int): Single<DeleteResponseEntity>? {
        return remoteProvider.deletePodcast(idPodcast)
    }

    override fun createComment(
        id: Int,
        request: DataCreateCommentRequest
    ): Single<CreateCommentResponseEntity>? {
        return remoteProvider.createComment(id, request)
    }

    override fun getComments(id: Int, limit: Int, offset: Int): Single<GetCommentsResponseEntity>? {
        return remoteProvider.getComments(id, limit, offset)
    }

    override fun reportPodcast(
        id: Int,
        request: DataCreateReportRequestEntity
    ): Single<CreateReportResponseEntity>? {
        return remoteProvider.reportPodcast(id, request)
    }

    override fun getFeaturedPodcasts(): Single<FeaturedPodcastsResponseEntity>? {
        return remoteProvider.getFeaturedPodcasts()
    }

    override fun getPodcastById(id: Int): Single<GetPodcastResponseEntity>? {
        return remoteProvider.getPodcastById(id)
    }

    override fun createDropOff(
        id: Int,
        request: DataDropOffRequest
    ): Single<UpdatedResponseEntity>? {
        return remoteProvider.createDropOff(id, request)
    }

    override fun getPopularPodcasts(): Single<PopularPodcastsResponseEntity>? {
        return remoteProvider.getPopularPodcasts()
    }

}