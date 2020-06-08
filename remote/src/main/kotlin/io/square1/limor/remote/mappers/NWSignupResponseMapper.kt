package io.square1.limor.remote.mappers


import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWSignUpResponse>.asDataEntity(): Single<SignUpResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWSignUpResponse.asDataEntity(): SignUpResponseEntity{
    return SignUpResponseEntity(
        code,
        data.asDataEntity(),
        message

    )
}

fun NWData.asDataEntity(): DataEntity {
    return DataEntity(
        access_token.asDataEntity(),
        user.asDataEntity()
    )
}


fun NWAccessToken.asDataEntity(): AccessTokenEntity {
    return AccessTokenEntity(
        token.asDataEntity()
    )
}

//fun NWToken.asDataEntity(): TokenEntity {
//    return TokenEntity(
//        access_token,
//        token_type,
//        expires_in,
//        scope,
//        created_at
//    )
//}


fun NWUser.asDataEntity(): UserEntity {
    return UserEntity(
        active,
        autoplay_enabled,
        blocked,
        blocked_by,
        date_of_birth,
        description,
        email,
        first_name,
        followed,
        followed_by,
        followers_count,
        following_count,
        gender,
        id,
        images.asDataEntity(),
        last_name,
        links.asDataEntity(),
        notifications_enabled,
        phone_number,
        sharing_url,
        suspended,
        unread_conversations,
        unread_conversations_count,
        unread_messages,
        unread_messages_count,
        username,
        verified,
        website
    )
}


fun NWImages.asDataEntity(): ImagesEntity {
    return ImagesEntity(
        small_url,
        medium_url,
        large_url,
        original_url
    )
}


fun NWLinks.asDataEntity(): LinksEntity {
    return LinksEntity(
        links
    )
}



