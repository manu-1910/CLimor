package io.square1.limor.remote.mappers



import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWUser>.asDataEntity(): Single<UserEntity> {
    return this.map { it.asDataEntity() }
}



