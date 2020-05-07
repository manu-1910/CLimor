package io.square1.limor.storage.repository.draft


import io.reactivex.Completable
import io.reactivex.Flowable
import io.square1.limor.storage.entities.RLMDraft

interface StorageDraftRepository {
    fun insertRealmDraft(realmDraft: RLMDraft): Completable
    fun loadRealmDrafts(): Flowable<List<RLMDraft>>
    fun deleteRealmDraft(realmDraft: RLMDraft): Completable
}