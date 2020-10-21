package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.mappers.asDataEntity
import com.limor.app.mappers.asUIModel
import com.limor.app.uimodels.UICreateReportRequest
import com.limor.app.uimodels.UICreateReportResponse
import io.reactivex.Single
import io.square1.limor.remote.executors.JobExecutor
import repositories.comment.CommentRepository
import javax.inject.Inject

class CreateCommentReportUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(id: Int, request: UICreateReportRequest): Single<UICreateReportResponse> {
        return commentRepository.reportComment(id, request.asDataEntity())
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}