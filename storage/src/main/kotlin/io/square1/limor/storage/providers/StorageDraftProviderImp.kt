package io.square1.limor.storage.providers


import entities.response.DraftEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.square1.limor.storage.mappers.asDataEntity
import io.square1.limor.storage.mappers.asStorageEntity
import io.square1.limor.storage.repository.draft.StorageDraftRepositoryImp
import providers.storage.StorageDraftProvider
import javax.inject.Inject


class StorageDraftProviderImp @Inject constructor(
    private val storageDraftRepositoryImp: StorageDraftRepositoryImp
) : StorageDraftProvider {
    override fun insertRealmDraft(draftEntity: DraftEntity): Completable {
        return storageDraftRepositoryImp.insertRealmDraft(draftEntity.asStorageEntity())
    }

    override fun loadRealmDrafts(): Flowable<List<DraftEntity>> {
        return storageDraftRepositoryImp.loadRealmDrafts()
            .map { draftsList -> draftsList.map { it.asDataEntity() } }
    }

    override fun deleteRealmDraft(draftEntity: DraftEntity): Completable {
        return storageDraftRepositoryImp.deleteRealmDraft(draftEntity.asStorageEntity())
    }

}