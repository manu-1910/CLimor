package io.square1.limor.remote


import io.square1.limor.remote.entities.requests.*
import io.square1.limor.remote.services.RemoteServiceConfig
import io.square1.limor.remote.services.comment.CommentServiceImp

import org.junit.Test

private const val CURRENT_TOKEN = "PkRbYfBMW7iTagfOvr7sNvnO4t5iC7OdjCzbbhijpuQ"


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
            token = CURRENT_TOKEN,
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
            token = CURRENT_TOKEN,
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
            token = CURRENT_TOKEN,
            expiredIn = 0
        )

        commentService = CommentServiceImp(config)

        val idComment = 659

        val response = commentService.getComments(idComment, 10, 0)?.test()

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
            token = CURRENT_TOKEN,
            expiredIn = 0
        )

        commentService = CommentServiceImp(config)

        val idComment = 659
        val createRequest = NWCreateCommentRequest(
            NWCommentRequest(
                "Hi, I'm a new comment from the android client"
            )
        )

        val createResponse = commentService.createComment(idComment, createRequest).test()

        createResponse?.assertNoErrors()
        var idCommentToDelete = 0
        createResponse?.assertValue {
            idCommentToDelete = it.data?.comment?.id ?: -1
            it.message == "Success"
        }

        val deleteRequest = NWContentRequest("useless content?")
        val deleteResponse = commentService.deleteComment(idCommentToDelete, deleteRequest).test()
        deleteResponse?.assertNoErrors()
        deleteResponse?.assertValue{
            it.code == 0
        }
    }

    @Test
    fun should_report_comment_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = CURRENT_TOKEN,
            expiredIn = 0
        )

        commentService = CommentServiceImp(config)

        val idComment = 659

        val request = NWCreateReportRequest("I don't like it")
        val response = commentService.reportComment(idComment, request)?.test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }

    @Test
    fun should_create_dropoff_comment_successfully() {
        val config = RemoteServiceConfig(
            baseUrl = baseURL,
            debug = true,
            client_id = "",
            client_secret = "",
            token = CURRENT_TOKEN,
            expiredIn = 0
        )

        commentService = CommentServiceImp(config)

        val idComment = 659

        val request = NWDropOffRequest(20f)
        val response = commentService.createDropOff(idComment, request).test()

        response?.assertNoErrors()
        response?.assertValue { it.message == "Success" }
    }


}



