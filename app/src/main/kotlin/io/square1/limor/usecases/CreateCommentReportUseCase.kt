package io.square1.limor.usecases

import io.reactivex.Single
import io.square1.limor.common.executors.PostExecutionThread
import io.square1.limor.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import io.square1.limor.uimodels.UICreateCommentReportResponse
import repositories.comment.CommentRepository
import javax.inject.Inject

class CreateCommentReportUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(id: Int): Single<UICreateCommentReportResponse> {
        return commentRepository.reportComment(id)
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}