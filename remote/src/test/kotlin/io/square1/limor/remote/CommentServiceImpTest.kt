package io.square1.limor.remote


import io.square1.limor.remote.entities.requests.NWCommentRequest
import io.square1.limor.remote.entities.requests.NWCreateCommentRequest
import io.square1.limor.remote.entities.requests.NWCreateReportRequest
import io.square1.limor.remote.services.RemoteServiceConfig
import io.square1.limor.remote.services.comment.CommentServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import org.junit.Test


@ImplicitReflectionSerializer
class CommentServiceImpTest{
    private lateinit var commentService: CommentServiceImp
    //    private val baseURL = "https://limor-api-staging.herokuapp.com/api/v1/podcasts/"
    private val baseURL = "https://limor-api-development.herokuapp.com"

    @Test
    fun should_like_comment_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            expiredIn = 0
        )

        commentService = CommentServiceImp(config)

        val idComment = 659

        val response = commentService.likeComment(idComment)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_dislike_comment_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            expiredIn = 0
        )

        commentService = CommentServiceImp(config)

        val idComment = 659

        val response = commentService.dislikeComment(idComment)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_get_comment_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = "4t6bOFXd3L89qqPLdXsBHtTOP4_-t_61Q2kl5R4dAdk",
            expiredIn = 0
        )

        commentService = CommentServiceImp(config)

        val idComment = 1552

        val response = commentService.getComments(idComment, 10, 0)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_create_comment_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = "4t6bOFXd3L89qqPLdXsBHtTOP4_-t_61Q2kl5R4dAdk",
            expiredIn = 0
        )

        commentService = CommentServiceImp(config)

        val idComment = 1552
        val request = NWCreateCommentRequest(
            NWCommentRequest(
                "Hi, I'm a new comment from the android client"
            )
        )

        val response = commentService.createComment(idComment, request).test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_report_comment_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = "9b1b2517ba88187cc8e50a2f40446a0ff10200b9353ef356441c751553dc33ce",
            expiredIn = 0
        )

        commentService = CommentServiceImp(config)

        val idComment = 659

        val request = NWCreateReportRequest("I don't like it")
        val response = commentService.reportComment(idComment, request)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }


}



