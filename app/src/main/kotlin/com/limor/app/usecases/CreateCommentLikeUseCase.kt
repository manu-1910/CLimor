package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UICreateCommentLikeResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.comment.CommentRepository
import javax.inject.Inject


class CreateCommentLikeUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(id: Int): Single<UICreateCommentLikeResponse> {
        return commentRepository.likeComment(id)
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}