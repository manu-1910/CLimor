package io.square1.limor.storage.repository.draft

import io.reactivex.Completable
import io.reactivex.Flowable
import io.realm.Realm
import io.square1.limor.storage.entities.RLMDraft
import timber.log.Timber
import javax.inject.Inject

class StorageDraftRepositoryImp @Inject constructor() : StorageDraftRepository{
    private val storageDraftService : StorageDraftService = StorageDraftService()

    override fun insertRealmDraft(realmDraft: RLMDraft): Completable {
        Timber.d("Categories -> ${realmDraft.caption} -- ${realmDraft.categories.isNullOrEmpty()}")
        return storageDraftService.save(realmDraft)
    }

    override fun loadRealmDrafts(): Flowable<List<RLMDraft>> {
        return storageDraftService.findAll()
    }

    override fun findByPrimaryKey(id : Int): Flowable<RLMDraft> {
        return storageDraftService.findByPrimaryKey(id)
    }

    override fun deleteRealmDraft(realmDraft: RLMDraft): Completable {
        return storageDraftService.delete(Realm.getDefaultInstance().where(RLMDraft::class.java).equalTo("id", realmDraft.id))
    }
}