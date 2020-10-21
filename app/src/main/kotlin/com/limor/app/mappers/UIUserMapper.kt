package com.limor.app.mappers


import com.limor.app.uimodels.UIUser
import entities.response.UserEntity
import io.reactivex.Single


fun Single<UserEntity>.asUIModel(): Single<UIUser> {
    return this.map { it.asUIModel() }
}
