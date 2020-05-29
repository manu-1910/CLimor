package io.square1.limor.remote.mappers


import entities.response.AuthResponseEntity
import entities.response.DataAuthResponseEntity
import entities.response.DataTokenEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWAuthResponse
import io.square1.limor.remote.entities.responses.NWDataAuthResponse
import io.square1.limor.remote.entities.responses.NWToken


fun Single<NWAuthResponse>.asDataEntity(): Single<AuthResponseEntity> {
    return this.map { it.asDataEntity() }
}

fun NWAuthResponse.asDataEntity(): AuthResponseEntity{
    return AuthResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}

fun NWDataAuthResponse.asDataEntity() : DataAuthResponseEntity {
    return DataAuthResponseEntity(
        token.asDataEntity()
    )
}

fun NWToken.asDataEntity(): DataTokenEntity{
    return DataTokenEntity(
        access_token,
        token_type,
        expires_in,
        scope,
        created_at
    )
}



