package io.square1.limor.storage

import io.reactivex.Completable
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.square1.limor.storage.entities.RLMDraft
import io.square1.limor.storage.extensions.*

abstract class RealmService<T : RealmObject, ID> {

    inline fun <reified T : RealmObject> findByPrimaryKey(id: ID): Flowable<T> =
        Flowable.defer {
            getPrimaryKeyFieldName(T::class.java)
                ?.let {
                    with(Realm.getDefaultInstance()) {
                        where(T::class.java)
                            .equalTo(it, "$id")
                            .findFirstAsync()
                            .asFlowable<T>()
                            .filter { it.isLoaded }
                    }
                } ?: throw IllegalArgumentException("object.not.have.primary.key")
        }.addRealmSchedulers()

    inline fun <reified T : RealmObject> findAll(): Flowable<List<T>> =
        Flowable.defer {
            with(Realm.getDefaultInstance()) {
                where(T::class.java)
                    .findAllAsync()
                    .toFlowableList(this)
            }
        }.addRealmSchedulers()

    fun findByQuery(query: RealmQuery<T>): Flowable<List<T>> =
        Flowable.defer {
            query.findAll()
                .toFlowableList(Realm.getDefaultInstance())
        }.addRealmSchedulers()

    fun save(entity: T): Completable =
        Completable.defer {
            entity.saveManaged()
        }.addRealmSchedulers()

    fun save(entities: List<T>): Completable =
        Completable.defer {
            entities.saveAllManaged()
        }.addRealmSchedulers()

    fun delete(entity: T): Completable =
        Completable.defer {
            entity.deleteManaged()
        }.addRealmSchedulers()

    fun delete(query: RealmQuery<T>): Completable =
        Completable.defer {
            query.findAll().deleteAllManaged()
        }.addRealmSchedulers()
}