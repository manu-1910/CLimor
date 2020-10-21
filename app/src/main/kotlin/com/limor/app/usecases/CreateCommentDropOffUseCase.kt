package com.limor.app.usecases

import com.limor.app.common.executors.PostExecutionThread
import io.reactivex.Single
import com.limor.app.mappers.asDataEntity
import com.limor.app.mappers.asUIModel
import io.square1.limor.remote.executors.JobExecutor
import com.limor.app.uimodels.UIDropOffRequest
import com.limor.app.uimodels.UIUpdatedResponse
import repositories.comment.CommentRepository
import javax.inject.Inject

class CreateCommentDropOffUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val postExecutionThread: PostExecutionThread,
    private val jobExecutor: JobExecutor
) {
    fun execute(id: Int, request: UIDropOffRequest): Single<UIUpdatedResponse> {
        return commentRepository.createDropOff(id, request.asDataEntity())
            ?.asUIModel()
            ?.observeOn(postExecutionThread.getScheduler())
            ?.subscribeOn(jobExecutor.getScheduler())!!
    }
}