package io.square1.limor.remote


import io.square1.limor.remote.entities.requests.*
import io.square1.limor.remote.services.RemoteServiceConfig
import io.square1.limor.remote.services.podcast.PodcastServiceImp
import io.square1.limor.remote.services.user.UserServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.Test



@ImplicitReflectionSerializer
class PodcastServiceImpTest{
    private lateinit var podcastService: PodcastServiceImp
//    private val baseURL = "https://limor-api-staging.herokuapp.com/api/v1/podcasts/"
    private val baseURL = "https://limor-api-development.herokuapp.com"


    @Test
    fun should_publish_podcast_successfully() {

        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "2711e12535ceb15773fe87dc691bcb8c26295bb1434f7d8f4912006dff6a189c",
            client_secret = "af570a038fbb5e9eb7c5338068f57b5a22c119d5fd273909f38b1cf4f9acd996",
//            token = "Bearer 9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
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
                    13000.0,
                    0.0,
                    ArrayList<String>()
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

        val response = podcastService.publishPodcast(publishRequest)?.test()

        response?.assertNoErrors()
        response?.assertValue {
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
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 659

        val response = podcastService.likePodcast(idPodcast)?.test()

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
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 659

        val response = podcastService.dislikePodcast(idPodcast)?.test()

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
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            expiredIn = 0
        )

        podcastService = PodcastServiceImp(config)

        val idPodcast = 4

        val response = podcastService.getComments(idPodcast, 10, 0)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }


}



