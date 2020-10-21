package io.square1.limor.remote


import io.square1.limor.remote.entities.requests.*
import io.square1.limor.remote.services.RemoteServiceConfig
import io.square1.limor.remote.services.comment.CommentService
import io.square1.limor.remote.services.comment.CommentServiceImp
import io.square1.limor.remote.services.podcast.PodcastServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.Test



@ImplicitReflectionSerializer
class PodcastServiceImpTest{
    private lateinit var podcastService: PodcastServiceImp
//    private val baseURL = "https://limor-api-staging.herokuapp.com/api/v1/podcasts/"
    private val baseURL = "https://limor-api-development.herokuapp.com"

    private val URL_DEVELOPMENT = "https://limor-api-development.herokuapp.com"
    private val URL_STAGING = "https://limor-api-staging.herokuapp.com"
    private val CURRENT_URL = URL_DEVELOPMENT


    private val TOKEN_TEST_DEVELOPMENT = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce"
    private val TOKEN_USER_DEVELOPMENT = "4t6bOFXd3L89qqPLdXsBHtTOP4_-t_61Q2kl5R4dAdk"
    private val SECONDARY_TOKEN_USER_DEVELOPMENT = "PkRbYfBMW7iTagfOvr7sNvnO4t5iC7OdjCzbbhijpuQ"
    private val TOKEN_STAGING = "36bf82e596dc582796508c09d050484181fa51278eb6b0c2bdbfb269c98a3992"
    private val CURRENT_TOKEN = SECONDARY_TOKEN_USER_DEVELOPMENT

    @Test
    fun should_publish_and_delete_podcast_successfully() {

        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "2711e12535ceb15773fe87dc691bcb8c26295bb1434f7d8f4912006dff6a189c",
            client_secret = "af570a038fbb5e9eb7c5338068f57b5a22c119d5fd273909f38b1cf4f9acd996",
//            token = "Bearer 9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 90000000000
        )

        podcastService = PodcastServiceImp(config)

        val publishRequest = NWPublishRequest(
            NWPodcastRequest(
                NWAudio(
                    "https://limor-platform-development.s3.amazonaws.com/podcast_comment_audio_direct_upload/audioFile_3180_1530517744075.m4a",
                    "https://limor-platform-development.s3.amazonaws.com/podcast_comment_audio_direct_upload/audioFile_3180_1530517744075.m4a",
                    100,
                    1000.0,
                    13000.0
                ),
                NWMetaData(
                    "podcast",
                    "caption",
                    53.2,
                    -6.22,
                    "https://en.wikipedia.org/wiki/User_(computing)#/media/File:User_icon_2.png"
                )
            )
        )

        val responseCreate = podcastService.publishPodcast(publishRequest)?.test()

        responseCreate?.assertNoErrors()

        var idPodcast = 0
        responseCreate?.assertValue {
            idPodcast = it.data.podcast.id
            it.message == "Success"
        }

        val responseDelete = podcastService.deletePodcast(idPodcast).test()
        responseDelete?.assertNoErrors()

        responseCreate?.assertValue {
            it.message == "Success"
        }

    }

    @Test
    fun should_like_podcast_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 1004

        val response = podcastService.likePodcast(idPodcast)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_recast_podcast_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 1004

        val response = podcastService.recastPodcast(idPodcast)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_dislike_podcast_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 1004

        val response = podcastService.dislikePodcast(idPodcast)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_delete_recast_podcast_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 1004

        val response = podcastService.deleteRecast(idPodcast)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_get_comments_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 4

        val response = podcastService.getComments(idPodcast, 10, 0)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }



    @Test
    fun should_create_and_delete_comment_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 1004
        val createRequest = NWCreateCommentRequest(
            NWCommentRequest(
                "Hi, I'm a new comment from the android client"
            )
        )

        val response = podcastService.createComment(idPodcast, createRequest).test()

        var idDeleteComment = 0
        response?.assertNoErrors()
        response?.assertValue {
            idDeleteComment = it.data?.comment?.id ?: 0
            it.message == "Success"
        }

        val commentService = CommentServiceImp(config)
        val deleteRequest = NWContentRequest("useless content?")
        val deleteResponse = commentService.deleteComment(idDeleteComment, deleteRequest).test()

        deleteResponse?.assertNoErrors()
        deleteResponse?.assertValue{
            it.code == 0
        }
    }




    @Test
    fun should_report_podcast_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 1004

        val request = NWCreateReportRequest("I don't like it")
        val response = podcastService.reportPodcast(idPodcast, request)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }


    @Test
    fun should_get_featured_podcasts_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val response = podcastService.getFeaturedPodcasts()?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_get_popular_podcasts_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val response = podcastService.getPopularPodcasts()?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_get_podcast_by_id_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 1004
        val response = podcastService.getPodcastById(idPodcast)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }




    @Test
    fun should_create_dropoff_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = SECONDARY_TOKEN_USER_DEVELOPMENT,
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 1004

        val request = NWDropOffRequest(20f)
        val response = podcastService.createDropOff(idPodcast, request).test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

}



