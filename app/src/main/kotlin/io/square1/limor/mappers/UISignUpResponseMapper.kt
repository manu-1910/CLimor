package io.square1.limor.mappers


import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<SignUpResponseEntity>.asUIModel(): Single<UISignUpResponse> {
    return this.map { it.asUIModel() }
}

fun SignUpResponseEntity.asUIModel(): UISignUpResponse{
    return UISignUpResponse(
        code,
        data.asUIModel(),
        message

    )
}

fun DataEntity.asUIModel(): UIData {
    return UIData(
        access_token.asUIModel(),
        user.asUIModel()
    )
}


fun AccessTokenEntity.asUIModel(): UIAccessToken {
    return UIAccessToken(
        token.asUIModel()
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


fun UserEntity.asUIModel(): UIUser {
    return UIUser(
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
        images.asUIModel(),
        last_name,
        links.asUIModel(),
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


fun ImagesEntity.asUIModel(): UIImages {
    return UIImages(
        small_url,
        medium_url,
        large_url,
        original_url
    )
}


fun LinksEntity.asUIModel(): UILinks {
    return UILinks(
        getAllUIWebsites(website),
        getAllUIContents(content),
        getAllUICaptions(caption)
    )
}

fun WebsiteEntity.asUIModel(): UIWebsite {
    return UIWebsite(id, link, startIndex, endIndex)
}

fun ContentEntity.asUIModel(): UIContent {
    return UIContent(id, link, startIndex, endIndex)
}

fun CaptionEntity.asUIModel(): UICaption {
    return UICaption(id, link, startIndex, endIndex)
}

fun getAllUIWebsites(entityList: ArrayList<WebsiteEntity>?): ArrayList<UIWebsite> {
    val uiList = ArrayList<UIWebsite>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}

fun getAllUIContents(entityList: ArrayList<ContentEntity>?): ArrayList<UIContent> {
    val uiList = ArrayList<UIContent>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}

fun getAllUICaptions(entityList: ArrayList<CaptionEntity>?): ArrayList<UICaption> {
    val uiList = ArrayList<UICaption>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}


