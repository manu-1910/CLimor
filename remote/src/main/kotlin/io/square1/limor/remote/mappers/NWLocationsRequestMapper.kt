package io.square1.limor.remote.mappers


import entities.request.DataSearchTermRequest
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWSearchTermRequest


//***** FROM REMOTE TO DATA
fun Single<NWSearchTermRequest>.asDataEntity(): Single<DataSearchTermRequest> {
    return this.map { it.asDataEntity() }
}

fun NWSearchTermRequest.asDataEntity(): DataSearchTermRequest {
    return DataSearchTermRequest(
        term
    )
}


//***** FROM DATA TO REMOTE
fun Single<DataSearchTermRequest>.asRemoteEntity(): Single<NWSearchTermRequest> {
    return this.map { it.asRemoteEntity() }
}

fun DataSearchTermRequest.asRemoteEntity() : NWSearchTermRequest {
    return NWSearchTermRequest(
        term
    )
}


