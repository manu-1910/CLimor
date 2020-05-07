package providers.storage

import entities.response.DraftEntity
import io.reactivex.Completable
import io.reactivex.Flowable

interface StorageDraftProvider {
    fun insertRealmDraft(centreEntity: DraftEntity): Completable
    fun loadRealmDrafts(): Flowable<List<DraftEntity>>
    fun deleteRealmDraft(centreEntity: DraftEntity): Completable
}