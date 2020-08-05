package io.square1.limor.remote.mappers


import entities.request.*
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.*


//***** FROM REMOTE TO DATA
fun Single<NWTagsRequest>.asDataEntity(): Single<DataTagsRequest> {
    return this.map { it.asDataEntity() }
}

fun NWTagsRequest.asDataEntity(): DataTagsRequest {
    return DataTagsRequest(
        tag
    )
}


//***** FROM DATA TO REMOTE
fun Single<DataTagsRequest>.asRemoteEntity(): Single<NWTagsRequest> {
    return this.map { it.asRemoteEntity() }
}

fun DataTagsRequest.asRemoteEntity() : NWTagsRequest {
    return NWTagsRequest(
        tag
    )
}

