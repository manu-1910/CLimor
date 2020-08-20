package io.square1.limor.remote.mappers


import entities.request.DataLocationsRequest
import io.reactivex.Single
import io.square1.limor.remote.entities.requests.NWLocationsRequest


//***** FROM REMOTE TO DATA
fun Single<NWLocationsRequest>.asDataEntity(): Single<DataLocationsRequest> {
    return this.map { it.asDataEntity() }
}

fun NWLocationsRequest.asDataEntity(): DataLocationsRequest {
    return DataLocationsRequest(
        term
    )
}


//***** FROM DATA TO REMOTE
fun Single<DataLocationsRequest>.asRemoteEntity(): Single<NWLocationsRequest> {
    return this.map { it.asRemoteEntity() }
}

fun DataLocationsRequest.asRemoteEntity() : NWLocationsRequest {
    return NWLocationsRequest(
        term
    )
}

