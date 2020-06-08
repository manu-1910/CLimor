package io.square1.limor.mappers


import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<UserEntity>.asUIModel(): Single<UIUser> {
    return this.map { it.asUIModel() }
}
