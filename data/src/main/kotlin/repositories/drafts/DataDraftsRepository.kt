package repositories.drafts

import entities.response.DraftEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import providers.storage.StorageDraftProvider
import javax.inject.Inject

class DataDraftsRepository @Inject constructor(private val storageProvider: StorageDraftProvider) : DraftsRepository {

    //*****************
    //STORAGE
    //*****************
    override fun insertRealmDraft(draftEntity: DraftEntity): Completable {
        return storageProvider.insertRealmDraft(draftEntity)
    }

    override fun loadRealmDrafts(): Flowable<List<DraftEntity>> {
        return storageProvider.loadRealmDrafts()
    }

    override fun deleteRealmDraft(draftEntity: DraftEntity): Completable {
        return storageProvider.deleteRealmDraft(draftEntity)
    }
}