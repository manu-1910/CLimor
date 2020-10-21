package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UIGetCommentsResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.comment.CommentRepository
import javax.inject.Inject


class GetCommentCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(id: Int, limit: Int, offset: Int): Single<UIGetCommentsResponse> {
        return commentRepository.getComments(id, limit, offset)
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}